package de.schoko.vcoasters.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.schoko.vcoasters.TrainMeta;
import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.codecs.LineCodecs;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RideSimulator {
	private final Track track;
	private final Supplier<EditorObject> selectedObjectSupplier;
	private final TrainMeta trainMeta;
	private Train train;

	private final ItemModel itemModel;

	private boolean showTrackAttachmentPoints;

	private boolean paused;
	private long lastUpdate;

	private long stepDuration;

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
		this.stepDuration = 50;
	}

	public void putTrainOnLine(Line line) {
		train = new Train(line, track.getFriction(), trainMeta.getSegmentAmount(), trainMeta.getCarDistance());
	}

	public void tick() {

		long tickTime = System.currentTimeMillis();
		long delta = tickTime - lastUpdate;
		if (delta < stepDuration) {
			return;
		} else if (delta > 15000) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		while (lastUpdate < tickTime) {
			lastUpdate += stepDuration;

			if (paused || train == null) return;

			oldPositions.clear();
			train.extractRenderedPositions(oldPositions::add);
			train.update();
			velocityRecorder.add(train.getVelocity());
			accelerationRecorder.add(train.getAcceleration());
		}
	}

	public void extract(RenderContext context) {
		if (train != null) {
			long delta = System.currentTimeMillis() - lastUpdate;
			if (paused) delta = stepDuration;
			List<InterpolatedPoint> currentPositions = new ArrayList<>();
			train.extractRenderedPositions(currentPositions::add);
			if (currentPositions.size() != oldPositions.size()) return;
			for (int i = 0; i < currentPositions.size(); i++) {
				currentPositions.set(i, InterpolatedPoint.lerp(((float) delta) / stepDuration, oldPositions.get(i), currentPositions.get(i)));
			}

			if (showTrackAttachmentPoints) {
				for (InterpolatedPoint position : currentPositions) {
					renderTrackAttachmentPoint(context, position);
				}
			}/*
			((RenderContextImpl) context).registerStandaloneCall(ctx -> {
				for (InterpolatedPoint position : currentPositions) {
					renderItemModel(ctx, position);
				}
			});*/
		}
	}

	public void submitWorldModels(LevelExtractionContext context) {
		if (train != null) {
			long delta = System.currentTimeMillis() - lastUpdate;
			if (paused) delta = stepDuration;
			List<InterpolatedPoint> currentPositions = new ArrayList<>();
			train.extractRenderedPositions(currentPositions::add);
			if (currentPositions.size() != oldPositions.size()) return;
			for (int i = 0; i < currentPositions.size(); i++) {
				currentPositions.set(i, InterpolatedPoint.lerp(((float) delta) / stepDuration, oldPositions.get(i), currentPositions.get(i)));
			}

			for (InterpolatedPoint position : currentPositions) {
				submitItemModel(context, position);
			}
		}
	}

	public void renderImGui(ImGuiIO imGui) {
		if (ImGui.begin("Simulation")) {
			ImGui.text("Paused:");
			ImGui.sameLine();
			ImBoolean imBoolean = new ImBoolean(paused);
			if (ImGui.checkbox("##PausedCheckbox", imBoolean)) {
				paused = imBoolean.get();
			}
			ImInt imInt = new ImInt((int) stepDuration);
			ImGui.text("Time step duration:");
			ImGui.sameLine();
			if (ImGui.inputInt("##PlaybackSpeedInput", imInt)) {
				if (imInt.get() <= 0) {
					imInt.set(1);
				}
				if (imInt.get() > 500) {
					imInt.set(500);
				}
				stepDuration = imInt.get();
			}

			ImGui.separatorText("Train");

			ImGui.beginDisabled(!paused || train == null);
			if (ImGui.button("-0.1")) {
				train.move(-0.1);
			}
			ImGui.sameLine();
			if (ImGui.button("+0.1")) {
				train.move(0.1);
			}
			ImGui.endDisabled();
			ImGui.beginDisabled(paused || train == null);
			if (ImGui.button("> 100")) {
				train.addToVelocity(100);
			}
			ImGui.sameLine();
			if (ImGui.button("> -100")) {
				train.addToVelocity(-100);
			}
			ImGui.endDisabled();

			ImGui.beginDisabled(train == null);
			imBoolean.set(showTrackAttachmentPoints);
			ImGui.text("Attachment points:");
			ImGui.sameLine();
			if (ImGui.checkbox("##ShowTrackAttachmentPointsCheckbox", imBoolean)) {
				showTrackAttachmentPoints = imBoolean.get();
			}
			ImGui.endDisabled();

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
			ImGui.beginDisabled(train == null || !(Minecraft.getInstance().gui.screen() instanceof EditorScreen));
			if (ImGui.button("Enter train view")) {
				EditorScreen screen = (EditorScreen) Minecraft.getInstance().gui.screen();
				Minecraft.getInstance().gui.setScreen(new TrainViewScreen(screen));
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
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);
				ImGui.text("Line ID");
				ImGui.tableSetColumnIndex(1);
				ImGui.text("Fullstop");

				List<Line> labelledLines = track.getLabelledLines();
				ImBoolean fullStop = new ImBoolean();
				for (int i = 0; i < labelledLines.size(); i++) {
					ImGui.tableNextRow();
					Line station = labelledLines.get(i);
					ImGui.tableSetColumnIndex(0);
					ImGui.text(station.getLabel());
					if (station.getPhysicsType() != null && station.getPhysicsType().supportsFullstop()) {
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

	public void submitItemModel(LevelExtractionContext context, InterpolatedPoint point) {
		PoseStack stack = new PoseStack();
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
			context.levelRenderer().submitNodeStorage.submitItem(stack, ItemDisplayContext.NONE, 0xFFFFFF, OverlayTexture.NO_OVERLAY, 0, new int[0], quads, ItemStackRenderState.FoilType.NONE);
		}
		stack.popPose();
	}

	public void renderTrackAttachmentPoint(RenderContext context, InterpolatedPoint point) {
		context.drawRotatedBox(point.point(), point.yaw(), point.pitch(), point.roll(), new Vector3f(0.125f, 0.075f, 0.2f), 0.25f, 0.15f, 0.4f, Colors.LIGHT_GRAY);
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
		return InterpolatedPoint.lerp(((float) delta) / stepDuration, oldPositions.get(index), train.getCarPosition(index));
	}
}
