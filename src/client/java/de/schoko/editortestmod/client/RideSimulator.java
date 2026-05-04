package de.schoko.editortestmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Axis;
import de.schoko.editortestmod.TrainMeta;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.core.RenderContextImpl;
import de.schoko.editortestmod.codecs.LineCodecs;
import de.schoko.editortestmod.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RideSimulator {
	private final Track track;
	private final Supplier<EditorObject> selectedObjectSupplier;
	private final TrainMeta trainMeta;
	private Train train;

	private final ItemModel itemModel;

	private boolean paused;
	private long lastUpdate;

	private final List<InterpolatedPoint> oldPositions;

	public RideSimulator(Track track, Supplier<EditorObject> getSelectedObject) {
		this.trainMeta = track.getTrainMeta();
		this.track = track;
		selectedObjectSupplier = getSelectedObject;
		//this.train = new Train(track.getFriction(), this.trainMeta.getSegmentAmount(), trainMeta.getCarDistance());
		this.itemModel = Minecraft.getInstance().getModelManager().getItemModel(trainMeta.getModelId());
		this.oldPositions = new ArrayList<>();
	}

	public void putTrainOnLine(Line line) {
		train = new Train(line, track.getFriction(), trainMeta.getSegmentAmount(), trainMeta.getCarDistance());
	}

	public void tick() {

		long delta = System.currentTimeMillis() - lastUpdate;
		if (delta < 50) {
			return;
		} else if (delta > 15000) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		lastUpdate += 50;

		if (paused || train == null) return;

		oldPositions.clear();
		train.extractRenderedPositions(oldPositions::add);
		train.update();
	}

	public void extract(RenderContext context) {
		if (train != null) ((RenderContextImpl) context).registerStandaloneCall(ctx -> {
			long delta = System.currentTimeMillis() - lastUpdate;
			if (paused) delta = 0;
			List<InterpolatedPoint> currentPositions = new ArrayList<>();
			train.extractRenderedPositions(currentPositions::add);
			for (int i = 0; i < currentPositions.size() && i < oldPositions.size(); i++) {
				renderItemModel(ctx, InterpolatedPoint.lerp(delta / 50.0f, oldPositions.get(i), currentPositions.get(i)));
			}
		});
	}

	public void renderImGui(ImGuiIO imGui) {
		if (ImGui.begin("Simulation")) {
			ImGui.text("Paused: ");
			ImGui.sameLine();
			ImBoolean imBoolean = new ImBoolean(paused);
			if (ImGui.checkbox("##PausedCheckbox", imBoolean)) {
				paused = imBoolean.get();
			}
			ImGui.separatorText("Train");
			if (selectedObjectSupplier.get() instanceof Line line) {
				if (ImGui.button("Move to selected line")) {
					putTrainOnLine(line);
				}
			} else if (selectedObjectSupplier.get() instanceof EndPoint endPoint) {
				if (ImGui.button("Move to selected endpoint")) {
					putTrainOnLine((endPoint.isOutputEndPoint() && endPoint.getCorrespondingEndpoint() != null) ? endPoint.getCorrespondingEndpoint().getLine() : endPoint.getLine());
				}
			} else {
				ImGui.beginDisabled();
				ImGui.button("Move to selected line");
				ImGui.endDisabled();
			}
			ImGui.beginDisabled(paused || train == null);
			ImGui.text("Velocity:");
			ImGui.sameLine();
			if (train == null) {
				ImGui.textColored(0xFFAAFFAA, "-");
				ImGui.sameLine();
				ImGui.text("B/S (");
				ImGui.sameLine();
				ImGui.textColored(0xFFAACCFF, "-");
			} else {
				double velocity = (train.getVelocity() * 20.0 / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				ImGui.textColored(0xFFAAFFAA, String.format("%.3f", velocity));
				ImGui.sameLine();
				ImGui.text("B/S (");
				ImGui.sameLine();
				ImGui.textColored(0xFF77AAFF, String.format("%.3f", velocity * 3.6));
			}
			ImGui.sameLine();
			ImGui.text("KB/H)");
			ImGui.endDisabled();
		}
		ImGui.end();
	}

	public void renderItemModel(LevelRenderContext context, InterpolatedPoint point) {
		PoseStack stack = context.poseStack();
		stack.pushPose();
		Vec3 camera = context.levelState().cameraRenderState.pos;
		stack.translate(-camera.x, -camera.y, -camera.z);
		stack.translate(point.x(), point.y(), point.z());
		stack.translate(new Vec3(trainMeta.getOffset()));
		stack.last().rotateAround(Axis.YP.rotation(-point.yaw() + trainMeta.getYawOffset()), trainMeta.getPivot().x, trainMeta.getPivot().y, trainMeta.getPivot().z);
		stack.last().rotateAround(Axis.XP.rotation(point.pitch() + trainMeta.getPitchOffset()), trainMeta.getPivot().x, trainMeta.getPivot().y, trainMeta.getPivot().z);
		stack.last().rotateAround(Axis.ZP.rotation(point.roll() + trainMeta.getRollOffset()), trainMeta.getPivot().x, trainMeta.getPivot().y, trainMeta.getPivot().z);
		List<BakedQuad> quads = null;
		if (itemModel instanceof CuboidItemModelWrapper wrapper) quads = wrapper.quads.getAll();
		else if (itemModel instanceof MissingItemModel missing) quads = missing.quads;
		if (quads != null) {
			for (BakedQuad quad : quads) {
				Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.solidMovingBlock()).putBakedQuad(stack.last(), quad, new QuadInstance());
			}
		}
		stack.popPose();
	}


	public TrainMeta getTrainMeta() {
		return trainMeta;
	}
}
