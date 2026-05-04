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
import imgui.type.ImInt;
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

	private FloatRecorder velocityRecorder;
	private FloatRecorder accelerationRecorder;

	public RideSimulator(Track track, Supplier<EditorObject> getSelectedObject) {
		this.trainMeta = track.getTrainMeta();
		this.track = track;
		selectedObjectSupplier = getSelectedObject;
		//this.train = new Train(track.getFriction(), this.trainMeta.getSegmentAmount(), trainMeta.getCarDistance());
		this.itemModel = Minecraft.getInstance().getModelManager().getItemModel(trainMeta.getModelId());
		this.oldPositions = new ArrayList<>();
		this.velocityRecorder = new FloatRecorder(1000);
		this.accelerationRecorder = new FloatRecorder(1000);
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
		velocityRecorder.add(train.getVelocity());
		accelerationRecorder.add(train.getAcceleration());
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

			ImGui.beginDisabled(paused || train == null);
			if (train == null) {
				showVelocity("Velocity", "-", "-");
				showVelocity("Max velocity: ", "-", "-");
			} else {
				double velocity = (train.getVelocity() * 20.0 / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				showVelocity("Velocity", String.format("%.3f", velocity), String.format("%.3f", velocity * 3.6));
				double maxVelocity = velocityRecorder.getMax() * 20.0 / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER;
				showVelocity("Max velocity", String.format("%.3f", maxVelocity), String.format("%.3f", maxVelocity * 3.6));
			}

			if (train == null) {
				showAcceleration("Max acceleration", "-");
			} else {
				showAcceleration("Max acceleration", String.format("%.3f", moreAbs(accelerationRecorder.getMax(), accelerationRecorder.getMin()) * 20.0 / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER));
			}

			ImGui.text("Last " + String.format("%.3f", velocityRecorder.getLength() * 0.05) + " seconds:");

			float[] values = velocityRecorder.getValues();
			ImGui.plotLines("##Velocity", values, values.length, 0, "Velocity", Math.min(velocityRecorder.getMin(), 0), velocityRecorder.getMax(), 300, 60);
			values = accelerationRecorder.getValues();
			ImGui.plotLines("##Acceleration", values, values.length, 0, "Acceleration", Math.min(accelerationRecorder.getMin(), 0), accelerationRecorder.getMax(), 300, 60);
			if (ImGui.button("Reset")) {
				velocityRecorder.reset();
				accelerationRecorder.reset();
			}
			ImGui.endDisabled();

			ImGui.separatorText("Controls");
			ImGui.beginDisabled(train == null && Minecraft.getInstance().screen instanceof EditorDataScreen);
			if (ImGui.button("Enter train view")) {
				EditorScreen screen = (EditorScreen) Minecraft.getInstance().screen;
				Minecraft.getInstance().setScreen(new TrainViewScreen(screen));
			}
			ImGui.endDisabled();
			ImGui.sameLine();

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

			if (ImGui.beginTable("##TrainControls", 3)) {
				List<Line> labelledLines = track.getLabelledLines();
				ImBoolean fullStop = new ImBoolean();
				for (int i = 0; i < labelledLines.size(); i++) {
					ImGui.tableNextRow();
					Line station = labelledLines.get(i);
					ImGui.tableSetColumnIndex(0);
					ImGui.text(station.getLabel());
					if (station.getPhysicsType().supportsFullstop()) {
						ImGui.tableSetColumnIndex(1);
						fullStop.set(station.isFullStop());
						if (ImGui.checkbox("##TrainFullstopControlFullstopCheckbox" + i, fullStop)) {
							station.setFullStop(fullStop.get());
						}
					}
					ImGui.tableSetColumnIndex(2);
					if (ImGui.button("Move here##TrainControlMovement" + i)) {
						putTrainOnLine(station);
					}
				}
				ImGui.endTable();
			}
		}
		ImGui.end();
	}

	public double moreAbs(double a, double b) {
		return Math.abs(a) >= Math.abs(b) ? a : b;
	}

	public void showVelocity(String label, String velocityText, String velocityConvertedText) {
		ImGui.text(label);
		ImGui.sameLine();
		ImGui.textColored(0xFFAAFFAA, velocityText);
		ImGui.sameLine();
		ImGui.text("B/S (");
		ImGui.sameLine();
		ImGui.textColored(0xFFAACCFF, velocityConvertedText);
		ImGui.sameLine();
		ImGui.text("KB/H)");
	}

	public void showAcceleration(String label, String text) {
		ImGui.text(label);
		ImGui.sameLine();
		ImGui.textColored(0xFFAAFFAA, text);
		ImGui.sameLine();
		ImGui.text("B/S²");
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

	public Train getTrain() {
		return train;
	}

	public InterpolatedPoint getTrainCarPoint(int index) {
		if (train == null) return null;
		long delta = System.currentTimeMillis() - lastUpdate;
		if (paused) delta = 0;
		return InterpolatedPoint.lerp(delta / 50.0f, oldPositions.get(index), train.getCarPosition(index));
	}
}
