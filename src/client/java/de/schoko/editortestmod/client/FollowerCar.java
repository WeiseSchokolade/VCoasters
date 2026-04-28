package de.schoko.editortestmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Axis;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.RenderContextImpl;
import de.schoko.editortestmod.core.InterpolatedPoint;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class FollowerCar {
	private Line currentLine;
	private final EditorScreen screen;
	private float distanceTravelled;
	private float speed;
	private long lastUpdate;

	private boolean renderModel;

	private InterpolatedPoint point;
	private final Vector3f offset;
	private final Vector3f pivot;

	public FollowerCar(Line line, EditorScreen screen) {
		this.currentLine = line;
		this.screen = screen;
		speed = 0f;
		lastUpdate = System.currentTimeMillis();
		offset = screen.getTrack().getCartModel().getOffset();
		pivot = screen.getTrack().getCartModel().getPivot();
		this.renderModel = screen.shouldRenderItemModel();
	}

	public void tick() {
		long update = System.currentTimeMillis();
		float dt = (update - lastUpdate) / 1000f;
		lastUpdate = update;

		distanceTravelled += speed * dt;
		float length = currentLine.getLength();
		if (distanceTravelled > length) {
			Line nextLine = currentLine.getOutputLine();
			if (nextLine != null) {
				distanceTravelled -= length;
				currentLine = nextLine;
			} else {
				distanceTravelled = length;
				speed = 0;
			}
		} else if (distanceTravelled < 0) {
			Line nextLine = currentLine.getInputLine();
			if (nextLine != null) {
				distanceTravelled += nextLine.getLength();
				currentLine = nextLine;
			} else {
				distanceTravelled = 0;
				speed = 0;
			}
		}
		point = currentLine.lerp(distanceTravelled / currentLine.getLength());
	}

	public void draw(RenderContext context) {
		if (renderModel) {
			InterpolatedPoint renderedPoint = point;
			((RenderContextImpl) context).registerStandaloneCall(ctx -> renderItemModel(ctx, renderedPoint));
		} else {
			context.drawRotatedBox(point.posToVector3f(), point.yaw(), point.pitch(), point.roll(), new Vector3f(0.125f, 0.0625f, 0.25f), 0.25f, 0.125f, 0.5f, Colors.LIGHT_GRAY);
		}
	}

	public void renderItemModel(LevelRenderContext context, InterpolatedPoint point) {
		PoseStack stack = context.poseStack();
		stack.pushPose();
		Vec3 camera = context.levelState().cameraRenderState.pos;
		stack.translate(-camera.x, -camera.y, -camera.z);
		stack.translate(point.x(), point.y(), point.z());
		stack.translate(offset.x, offset.y(), offset.z());
		stack.last().rotateAround(Axis.YP.rotation(-point.yaw() + screen.getTrack().getCartModel().getYawOffset()), pivot.x, pivot.y, pivot.z);
		stack.last().rotateAround(Axis.XP.rotation(point.pitch() + screen.getTrack().getCartModel().getPitchOffset()), pivot.x, pivot.y, pivot.z);
		stack.last().rotateAround(Axis.ZP.rotation(point.roll() + screen.getTrack().getCartModel().getRollOffset()), pivot.x, pivot.y, pivot.z);
		List<net.minecraft.client.resources.model.geometry.BakedQuad> quads = null;
		ItemModel model = screen.getItemModel();
		if (model instanceof CuboidItemModelWrapper wrapper) quads = wrapper.quads.getAll();
		else if (model instanceof MissingItemModel missing) quads = missing.quads;
		if (quads != null) {
			for (BakedQuad quad : quads) {
				Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.solidMovingBlock()).putBakedQuad(stack.last(), quad, new QuadInstance());
			}
			//ItemDisplayContext.NONE, stack, Minecraft.getInstance().renderBuffers().bufferSource(), 255, 0, new int[0], quads, RenderTypes.solidMovingBlock(), ItemStackRenderState.FoilType.NONE.;

			//context.gameRenderer().item
		}
		stack.popPose();
	}

	public void addToSpeed(float speed) {
		this.speed += speed;
	}

	public InterpolatedPoint getPoint() {
		return point;
	}

	public void setPoint(InterpolatedPoint point) {
		this.point = point;
	}

	public void setRenderModel(boolean renderModel) {
		this.renderModel = renderModel;
	}

	public boolean isRenderModel() {
		return renderModel;
	}

	public void toggleRenderModel() {
		this.renderModel = !renderModel;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setDistanceTravelled(float distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}

	public void setCurrentLine(Line currentLine) {
		this.currentLine = currentLine;
	}

	public float getSpeed() {
		return speed;
	}

	public EditorScreen getScreen() {
		return screen;
	}
}
