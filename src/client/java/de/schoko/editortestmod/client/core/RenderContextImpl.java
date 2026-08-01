package de.schoko.editortestmod.client.core;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.core.QuadObtainer;
import de.schoko.editortestmod.core.RenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class RenderContextImpl implements RenderContext {
	public static final RenderPipeline LINE_BOXES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "pipeline/debug_filled_box_through_walls"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.LINES)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		//.withDepthTestFunction(CompareOp.LESS_THAN)
		.build()
	);

	public static final RenderPipeline FILLED_BOXES = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "pipeline/debug_filled_box_through_walls"))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(true)
			//.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
			//.withDepthWrite(true)
			//.withDepthTestFunction(CompareOp.LESS_THAN)
			.build()
	);

	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
	private static final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(() -> EditorTestMod.MOD_ID + " editor objects buffer", RenderType.SMALL_BUFFER_SIZE);

	private final List<QuadRenderState> submittedQuadRenderStates;

	public RenderContextImpl() {
		this.submittedQuadRenderStates = new ArrayList<>();
	}

	@Override
	public void renderAndDraw(LevelRenderContext context) {
		RenderPipeline renderPipeline = FILLED_BOXES;
		VertexFormat formatBinding = renderPipeline.getVertexFormatBinding(0);
		PrimitiveTopology primitive = renderPipeline.getPrimitiveTopology();
		StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive, VertexSorting.ORTHOGRAPHIC_Z);

		assert primitive == PrimitiveTopology.QUADS;

		// render
		PoseStack matrices = context.poseStack();
		Vec3 camera = context.levelState().cameraRenderState.pos;

		matrices.pushPose();
		matrices.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f pose = matrices.last().pose();

		VertexConsumer builder = stagedBuffer.getVertexBuilder(draw);
		for (QuadRenderState state : submittedQuadRenderStates) {
			builder.addVertex(pose, state.aX(), state.aY(), state.aZ()).setColor(state.r(), state.g(), state.b(), state.a());
			builder.addVertex(pose, state.bX(), state.bY(), state.bZ()).setColor(state.r(), state.g(), state.b(), state.a());
			builder.addVertex(pose, state.cX(), state.cY(), state.cZ()).setColor(state.r(), state.g(), state.b(), state.a());
			builder.addVertex(pose, state.dX(), state.dY(), state.dZ()).setColor(state.r(), state.g(), state.b(), state.a());
		}

		matrices.popPose();

		// draw
		stagedBuffer.upload();

		StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);

		if (info != null) {
			performDrawCall(Minecraft.getInstance(), info, renderPipeline);
		}

		stagedBuffer.endFrame();

		submittedQuadRenderStates.clear();
	}

	private void performDrawCall(Minecraft instance, StagedVertexBuffer.ExecuteInfo info, RenderPipeline renderPipeline) {
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

		RenderTarget mainTarget = instance.gameRenderer.mainRenderTarget();
		GpuTextureView colorTexture = mainTarget.getColorTextureView();

		try (RenderPass renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(() -> EditorTestMod.MOD_ID + " render pipeline pass", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(renderPipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
			renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

			renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
		}
	}

	public static void close() {
		stagedBuffer.close();
	}


	private record QuadRenderState(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float r, float g, float b, float a) {

	}

	private void submitQuad(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float r, float g, float b, float a) {
		submittedQuadRenderStates.add(new QuadRenderState(aX, aY, aZ, bX, bY, bZ, cX, cY, cZ, dX, dY, dZ, r, g, b, a));
	}

	@Override
	public void drawQuads(Iterable<QuadObtainer.Quad> quads, float r, float g, float b, float a) {
		quads.forEach(quad -> submitQuad(
			quad.a().x, quad.a().y, quad.a().z,
			quad.b().x, quad.b().y, quad.b().z,
			quad.c().x, quad.c().y, quad.c().z,
			quad.d().x, quad.d().y, quad.d().z,
			r, g, b, a
			));
	}

	private void submitAABox(float aX, float aY, float aZ, float bX, float bY, float bZ, float r, float g, float b, float a) {
		submitQuad(
			aX, aY, aZ,
			aX, bY, aZ,
			bX, bY, aZ,
			bX, aY, aZ,
			r, g, b, a);
		submitQuad(
			bX, aY, aZ,
			bX, bY, aZ,
			bX, bY, bZ,
			bX, aY, bZ,
			r, g, b, a);
		submitQuad(
			bX, aY, bZ,
			bX, bY, bZ,
			aX, bY, bZ,
			aX, aY, bZ,
			r, g, b, a);
		submitQuad(
			aX, aY, aZ,
			aX, aY, bZ,
			aX, bY, bZ,
			aX, bY, aZ,
			r, g, b, a);
		submitQuad(
			aX, bY, aZ,
			aX, bY, bZ,
			bX, bY, bZ,
			bX, bY, aZ,
			r, g, b, a);
		submitQuad(
			aX, aY, aZ,
			bX, aY, aZ,
			bX, aY, bZ,
			aX, aY, bZ,
			r, g, b, a);
	}

	@Override
	public void drawAABox(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, float r, float g, float b, float a) {
		submitAABox((float) fromX, (float) fromY, (float) fromZ, (float) toX, (float) toY, (float) toZ, r, g, b, a);
	}

	private void drawCuboid(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float eX, float eY, float eZ, float fX, float fY, float fZ, float gX, float gY, float gZ, float hX, float hY, float hZ, float r, float g, float b, float a) {
		submitQuad(
			aX, aY, aZ,
			bX, bY, bZ,
			cX, cY, cZ,
			dX, dY, dZ,
			r, g, b, a);
		submitQuad(
			aX, aY, aZ,
			eX, eY, eZ,
			fX, fY, fZ,
			bX, bY, bZ,
			r, g, b, a);
		submitQuad(
			bX, bY, bZ,
			fX, fY, fZ,
			gX, gY, gZ,
			cX, cY, cZ,
			r, g, b, a);
		submitQuad(
			cX, cY, cZ,
			gX, gY, gZ,
			hX, hY, hZ,
			dX, dY, dZ,
			r, g, b, a);
		submitQuad(
			aX, aY, aZ,
			dX, dY, dZ,
			hX, hY, hZ,
			eX, eY, eZ,
			r, g, b, a);
		submitQuad(
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
