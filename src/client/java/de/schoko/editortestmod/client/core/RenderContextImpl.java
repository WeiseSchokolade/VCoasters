package de.schoko.editortestmod.client.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.core.QuadObtainer;
import de.schoko.editortestmod.core.RenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class RenderContextImpl implements RenderContext {
	public static final RenderPipeline LINE_BOXES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "pipeline/debug_filled_box_through_walls"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		//.withDepthTestFunction(CompareOp.LESS_THAN)
		.build()
	);

	public static final RenderPipeline FILLED_BOXES = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "pipeline/debug_filled_box_through_walls"))
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			//.withDepthWrite(true)
			//.withDepthTestFunction(CompareOp.LESS_THAN)
			.build()
	);



	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
	private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private PoseStack matrices;
	private RenderPipeline pipeline;
	private BufferBuilder buffer;
	private MappableRingBuffer vertexBuffer;
	private final List<Consumer<LevelRenderContext>> standaloneCalls;
	private LevelRenderContext context;

	public RenderContextImpl() {
		this.standaloneCalls = new ArrayList<>();
	}

	public void update(LevelRenderContext context, RenderPipeline pipeline) {
		this.context = context;
		matrices = context.poseStack();
		this.pipeline = pipeline;
		Vec3 camera = context.levelState().cameraRenderState.pos;

		matrices.pushPose();
		matrices.translate(-camera.x, -camera.y, -camera.z);

		if (buffer == null) {
			buffer = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
		}
	}

	public void executeDraw(Minecraft client) {
		MeshData builtBuffer = buffer.buildOrThrow();
		MeshData.DrawState drawParameters = builtBuffer.drawState();
		VertexFormat format = drawParameters.format();

		GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

		draw(client, pipeline, allocator, builtBuffer, drawParameters, vertices, format);

		vertexBuffer.rotate();
		buffer = null;
	}

	private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
		int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

		if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
			vertexBuffer = new MappableRingBuffer(() -> EditorTestMod.MOD_ID + " render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		return vertexBuffer.currentBuffer();
	}


	private static void draw(Minecraft client, RenderPipeline pipeline, ByteBufferBuilder allocator, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
		GpuBuffer indices;
		VertexFormat.IndexType indexType;

		if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
			// Sort the quads if there is translucency
			builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
			// Upload the index buffer
			indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
			indexType = builtBuffer.drawState().indexType();
		} else {
			// Use the general shape index buffer for non-quad draw modes
			RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
			indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
			indexType = shapeIndexBuffer.type();
		}

		// Actually execute the draw
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
		try (RenderPass renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(() -> EditorTestMod.MOD_ID + " example render pipeline rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			renderPass.setVertexBuffer(0, vertices);
			renderPass.setIndexBuffer(indices, indexType);
			renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
		}

		builtBuffer.close();
	}

	public void endCall() {
		matrices.popPose();
		standaloneCalls.forEach(consumer -> consumer.accept(context));
		standaloneCalls.clear();
	}

	public void destroy() {
		allocator.close();
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}
	}

	public void registerStandaloneCall(Consumer<LevelRenderContext> renderCall) {
		this.standaloneCalls.add(renderCall);
	}

	private void drawQuad(Matrix4fc pose, float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float r, float g, float b, float a) {
		buffer.addVertex(pose, aX, aY, aZ).setColor(r, g, b, a);
		buffer.addVertex(pose, bX, bY, bZ).setColor(r, g, b, a);
		buffer.addVertex(pose, cX, cY, cZ).setColor(r, g, b, a);
		buffer.addVertex(pose, dX, dY, dZ).setColor(r, g, b, a);
	}

	@Override
	public void drawQuads(Iterable<QuadObtainer.Quad> quads, float r, float g, float b, float a) {
		Matrix4f pose = matrices.last().pose();
		quads.forEach(quad -> drawQuad(pose,
			quad.a().x, quad.a().y, quad.a().z,
			quad.b().x, quad.b().y, quad.b().z,
			quad.c().x, quad.c().y, quad.c().z,
			quad.d().x, quad.d().y, quad.d().z,
			r, g, b, a
			));
	}

	private void drawAABox(float aX, float aY, float aZ, float bX, float bY, float bZ, float r, float g, float b, float a) {
		Matrix4f pose = matrices.last().pose();
		drawQuad(pose,
			aX, aY, aZ,
			aX, bY, aZ,
			bX, bY, aZ,
			bX, aY, aZ,
			r, g, b, a);
		drawQuad(pose,
			bX, aY, aZ,
			bX, bY, aZ,
			bX, bY, bZ,
			bX, aY, bZ,
			r, g, b, a);
		drawQuad(pose,
			bX, aY, bZ,
			bX, bY, bZ,
			aX, bY, bZ,
			aX, aY, bZ,
			r, g, b, a);
		drawQuad(pose,
			aX, aY, aZ,
			aX, aY, bZ,
			aX, bY, bZ,
			aX, bY, aZ,
			r, g, b, a);
		drawQuad(pose,
			aX, bY, aZ,
			aX, bY, bZ,
			bX, bY, bZ,
			bX, bY, aZ,
			r, g, b, a);
		drawQuad(pose,
			aX, aY, aZ,
			bX, aY, aZ,
			bX, aY, bZ,
			aX, aY, bZ,
			r, g, b, a);
	}

	@Override
	public void drawAABox(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, float r, float g, float b, float a) {
		drawAABox((float) fromX, (float) fromY, (float) fromZ, (float) toX, (float) toY, (float) toZ, r, g, b, a);
	}

	private void drawCuboid(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float eX, float eY, float eZ, float fX, float fY, float fZ, float gX, float gY, float gZ, float hX, float hY, float hZ, float r, float g, float b, float a) {
		Matrix4f pose = matrices.last().pose();
		drawQuad(pose,
			aX, aY, aZ,
			bX, bY, bZ,
			cX, cY, cZ,
			dX, dY, dZ,
			r, g, b, a);
		drawQuad(pose,
			aX, aY, aZ,
			eX, eY, eZ,
			fX, fY, fZ,
			bX, bY, bZ,
			r, g, b, a);
		drawQuad(pose,
			bX, bY, bZ,
			fX, fY, fZ,
			gX, gY, gZ,
			cX, cY, cZ,
			r, g, b, a);
		drawQuad(pose,
			cX, cY, cZ,
			gX, gY, gZ,
			hX, hY, hZ,
			dX, dY, dZ,
			r, g, b, a);
		drawQuad(pose,
			aX, aY, aZ,
			dX, dY, dZ,
			hX, hY, hZ,
			eX, eY, eZ,
			r, g, b, a);
		drawQuad(pose,
			hX, hY, hZ,
			gX, gY, gZ,
			fX, fY, fZ,
			eX, eY, eZ,
			r, g, b, a);
	}


	@Override
	public void drawRhomboid(Vector3f o, Vector3f x, Vector3f y, Vector3f z, Vector4f color) {
		drawCuboid(
			o.x, o.y, o.z,
			o.x + x.x, o.y + x.y, o.z + x.z,
			o.x + x.x + z.x, o.y + x.y + z.y, o.z + x.z + z.z,
			o.x + z.x, o.y + z.y, o.z + z.z,
			o.x + y.x, o.y + y.y, o.z + y.z,
			o.x + x.x + y.x, o.y + x.y + y.y, o.z + x.z + y.z,
			o.x + x.x + y.x + z.x, o.y + x.y + y.y + z.y, o.z + x.z + y.z + z.z,
			o.x + y.x + z.x, o.y + y.y + z.y, o.z + y.z + z.z,
			color.x, color.y, color.z, color.w
		);
	}
}
