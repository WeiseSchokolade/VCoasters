package de.schoko.vcoasters.client;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.TrainMeta;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.export.DefaultExporter;
import de.schoko.vcoasters.client.lines.CreateFirstLineView;
import de.schoko.vcoasters.client.points.LineEndPointView;
import de.schoko.vcoasters.client.renderer.EndpointBoxComponent;
import de.schoko.vcoasters.client.renderer.LineBoxComponent;
import de.schoko.vcoasters.client.renderer.LineRenderImGuiComponent;
import de.schoko.vcoasters.core.DirtContainer;
import de.schoko.vcoasters.core.InterpolatedPoint;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
import de.schoko.vcoasters.packets.ApplyLineChangesC2S;
import de.schoko.vcoasters.packets.ApplyTrackMetaChangesC2S;
import de.schoko.vcoasters.packets.SaveDataC2S;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TrackEditorMode extends EditorMode<TrackEditorMode> {
	private final Track editedTrack;
	private TrackRideSimulator simulator;

	private Identifier itemModelId;
	private ItemModel itemModel;
	private String inputtedItemModel;
	private long inputTime;

	private String requestedFilePath;
	private String majorNamespace;
	private String minorNamespace;
	private List<String> stationNames;

	private boolean renderItemModel;
	private boolean requestSaving;
	private float[] trackTransformation;

	public TrackEditorMode(Track editedTrack) {
		this.editedTrack = editedTrack;
		editedTrack.getLines().forEach(this::addComponentsToLine);

		setDefaultView();

		if (!editedTrack.getTrainMeta().getModelId().equals(itemModelId)) {
			itemModelId = editedTrack.getTrainMeta().getModelId();
			itemModel = Minecraft.getInstance().getModelManager().getItemModel(itemModelId);
		}
		this.renderItemModel = true;

		this.stationNames = new ArrayList<>();

		this.simulator = new TrackRideSimulator(editedTrack, this::setViewToTrainView, this::getSelectedObject);
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {

	}

	@Override
	public void submitWorldModels(LevelExtractionContext context) {
		simulator.submitWorldModels(context);
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		simulator.renderImGui(io);

		if (ImGui.begin("Track Settings")) {
			ImGui.text("Id: ");
			ImGui.sameLine();
			ImGui.text(editedTrack.getId());

			ImGui.text("Export version: ");
			ImGui.sameLine();
			ImGui.text(String.valueOf(editedTrack.getExportVersion()));

			ImGui.text("Data version: ");
			ImGui.sameLine();
			ImGui.text(String.valueOf(editedTrack.getDataVersion()));

			ImGui.text("Lines: ");
			ImGui.sameLine();
			ImGui.textColored(0xFFAAFFAA, String.valueOf(editedTrack.getLines().size()));


			ImGui.text("Name: ");
			ImGui.sameLine();
			ImString inputString = new ImString();
			inputString.set(editedTrack.getTrackName());
			ImGui.inputText("##NameInputLabel", inputString);
			editedTrack.setTrackName(inputString.get());

			ImGui.text("Comment: ");
			ImGui.sameLine();
			inputString.set(editedTrack.getTrackComment());
			ImGui.inputTextMultiline("##CommentInputBox", inputString);
			editedTrack.setTrackComment(inputString.get());

			ImGui.text("Gravity: ");
			if (ImGui.beginItemTooltip()) {
				ImGui.text("In meters per second²");
				ImGui.endTooltip();
			}
			ImGui.sameLine();
			ImDouble imDouble = new ImDouble();
			imDouble.set(editedTrack.getGravity());
			ImGui.inputDouble("##GravityInput", imDouble);
			editedTrack.setGravity(imDouble.get());
			if (ImGui.button("No gravity")) {
				editedTrack.setGravity(0);
			}

			ImGui.text("Friction: ");
			ImGui.sameLine();
			ImInt imInt = new ImInt();
			imInt.set(editedTrack.getFriction());
			ImGui.inputInt("##FrictionInput", imInt);
			editedTrack.setFriction(imInt.get());

			ImGui.text("Ticks per second: ");
			ImGui.sameLine();
			imInt = new ImInt();
			imInt.set(editedTrack.getTicksInHertz());
			if (ImGui.inputInt("##TicksPerSecondInput", imInt) && imInt.get() > -1) {
				editedTrack.setTicksInHertz(imInt.get());
			}

			if (ImGui.button("Save")) {
				requestSaving = true;
			}
			ImGui.sameLine();
			if (ImGui.button("Save and close")) {
				requestSaving = true;
				close();
			}
			ImGui.sameLine();
			if (ImGui.button("Close without saving")) {
				close();
			}
			if (ImGui.button("Bake velocities")) {
				editedTrack.setAcceleration(editedTrack.getGravity(), 1.0 / editedTrack.getTicksInHertz());
			}

			ImBoolean bool = new ImBoolean(trackTransformation != null);
			if (ImGui.checkbox("Track transformations", bool)) {
				trackTransformation = new float[] {0, 0, 0};
			}
			if (bool.get()) {
				ImGui.inputScalarN("##TrackTranslation", trackTransformation, trackTransformation.length);
				ImGui.sameLine();
				if (ImGui.button("Apply")) {
					editedTrack.getLines().forEach(line -> line.shift(trackTransformation[0], trackTransformation[1], trackTransformation[2]));
				}
			} else {
				trackTransformation = null;
			}


			if (ImGui.collapsingHeader("Export##ExportHeader")) {
				//TinyFileDialogs.tinyfd_colorChooser("Colors?", "#FF0077", ByteBuffer.wrap(new byte[] {0, 0, 0}), ByteBuffer.wrap(new byte[] {0, 0, 0}));
				//TinyFileDialogs.tinyfd_messageBox("Heya!", "How you doin?", "ok", "info", 0);
				//TinyFileDialogs.tinyfd_notifyPopup("Uh, oh!", "They call me hermit, the frog", "warning");
				ImGui.text("File:");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(requestedFilePath);
				if (ImGui.inputText("##FilePathInput", inputString)) {
					this.requestedFilePath = inputString.get();
				}
				ImGui.sameLine();
				if (ImGui.button("Choose")) {
					requestedFilePath = TinyFileDialogs.tinyfd_saveFileDialog("Choose file location", requestedFilePath, PointerBuffer.allocateDirect(0), "This is a description");
				}

				if (majorNamespace == null) majorNamespace = editedTrack.getId().split(":")[0];
				if (minorNamespace == null) minorNamespace = editedTrack.getId().contains(":") ? editedTrack.getId().split(":")[1] : editedTrack.getTrackName();

				ImGui.text("Major namespace");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(majorNamespace);
				if (ImGui.inputText("##MajorNameInput", inputString)) {
					majorNamespace = inputString.get();
				}

				ImGui.text("Minor namespace");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(minorNamespace);
				if (ImGui.inputText("##MinorNameInput", inputString)) {
					minorNamespace = inputString.get();
				}

				boolean allIdsExist = true;

				if (ImGui.beginTable("Trains", 3)) {
					ImGui.tableNextRow();
					ImGui.tableSetColumnIndex(0);
					ImGui.text("Train name");
					ImGui.tableSetColumnIndex(1);
					ImGui.text("Line ID");

					for (int i = 0; i < stationNames.size(); i++) {
						String name = stationNames.get(i);

						ImGui.tableNextRow();
						ImGui.tableSetColumnIndex(0);
						ImGui.text("t" + (i + 1));

						ImGui.tableSetColumnIndex(1);
						inputString = new ImString();
						inputString.set(name);
						ImGui.inputText("##InputStationName" + i, inputString);
						name = inputString.get();
						stationNames.set(i, name);

						if (editedTrack.getLine(name) == null) allIdsExist = false;

						ImGui.tableSetColumnIndex(2);
						if (ImGui.button("Remove")) {
							stationNames.remove(i);
							i--;
						}
					}
					ImGui.endTable();
					if (ImGui.button("Add train")) {
						String id = !editedTrack.getLines().isEmpty() ? editedTrack.getLines().getFirst().getId() : "Line";
						stationNames.add(id);
					}
				}

				ImGui.beginDisabled(stationNames.isEmpty() || !allIdsExist);
				if (ImGui.button("Export")) {
					try {
						DefaultExporter.getExporter().exportToZip(editedTrack, stationNames, majorNamespace, minorNamespace, new File(requestedFilePath));
						TinyFileDialogs.tinyfd_messageBox("Export", editedTrack.getTrackName() + " was exported!", "ok", "info", 0);
					} catch (IOException e) {
						TinyFileDialogs.tinyfd_messageBox("Export", "An error occurred while trying to export your track!\n" + e.getMessage(), "ok", "error", 0);
						e.printStackTrace();
					}
				}
				ImGui.endDisabled();
			}

			if (ImGui.collapsingHeader("Train Cars##TrainMetaHeader")) {
				TrainMeta trainMeta = editedTrack.getTrainMeta();

				ImGui.text("Id: ");
				ImGui.sameLine();
				if (inputTime != 0 && System.currentTimeMillis() - inputTime > 15000) {
					inputtedItemModel = null;
					inputTime = 0;
				}
				inputString.set(inputtedItemModel != null ? inputtedItemModel : trainMeta.getModelId());
				if (ImGui.inputText("##InputCartModelId", inputString)) {
					editedTrack.setDirty(true);
					Identifier identifier = Identifier.tryParse(inputString.get());
					if (identifier == null) {
						ImGui.textColored(0xFFFF0000, "Invalid identifier!");
						inputtedItemModel = inputString.get();
						if (inputTime == 0) inputTime = System.currentTimeMillis();
					} else {
						trainMeta.setModelId(identifier);
						inputtedItemModel = null;
						inputTime = 0;
					}
				}

				ImGui.text("Car Distance: ");
				ImGui.sameLine();
				ImFloat imFloat = new ImFloat(trainMeta.getCarDistance());
				if (ImGui.inputFloat("##CarDistanceInput", imFloat) && imFloat.get() > 0) {
					trainMeta.setCarDistance(imFloat.get());
				}

				ImGui.text("Segments: ");
				ImGui.sameLine();
				imInt.set(trainMeta.getSegmentAmount());
				if (ImGui.inputInt("##SegmentAmountInput", imInt) && imInt.get() > 0) {
					trainMeta.setSegmentAmount(imInt.get());
				}

				ImGui.text("Offset: ");
				ImGui.sameLine();
				float[] floatInputArray = new float[]{trainMeta.getOffset().x, trainMeta.getOffset().y, trainMeta.getOffset().z};
				if (ImGui.inputScalarN("##OffsetInput", floatInputArray, 3)) trainMeta.setDirty(true);
				trainMeta.getOffset().set(floatInputArray);

				ImGui.text("Pivot: ");
				ImGui.sameLine();
				floatInputArray = new float[]{trainMeta.getPivot().x, trainMeta.getPivot().y, trainMeta.getPivot().z};
				if (ImGui.inputScalarN("##PivotOffsetInput", floatInputArray, 3)) trainMeta.setDirty(true);
				trainMeta.getPivot().set(floatInputArray);

				ImGui.text("Yaw/Pitch/Roll: ");
				ImGui.sameLine();
				floatInputArray = new float[]{(float) Math.toDegrees(trainMeta.getYawOffset()), (float) Math.toDegrees(trainMeta.getPitchOffset()), (float) Math.toDegrees(trainMeta.getRollOffset())};
				if (ImGui.inputScalarN("##RotationOffsetInput", floatInputArray, 3)) {
					trainMeta.setDirty(true);
					trainMeta.setYawOffset((float) Math.toRadians(floatInputArray[0]));
					trainMeta.setPitchOffset((float) Math.toRadians(floatInputArray[1]));
					trainMeta.setRollOffset((float) Math.toRadians(floatInputArray[2]));
				}
			}
		}
		ImGui.end();
	}

	public Track getEditedTrack() {
		return editedTrack;
	}

	public TrackRideSimulator getSimulator() {
		return simulator;
	}

	@Override
	public void endClientTick() {
		simulator.tick();

		List<Line> changedLines = new ArrayList<>();
		editedTrack.getLines().forEach(line -> {
			if (line.getComponent(DirtContainer.class).isDirty()) {
				changedLines.add(line);
				line.getComponent(DirtContainer.class).setDirty(false);
			}
		});
		List<Line> removedLines = editedTrack.getRemovedLines();
		if (!changedLines.isEmpty() || !removedLines.isEmpty()) {
			removedLines.forEach(changedLines::remove);

			ClientPlayNetworking.send(new ApplyLineChangesC2S(editedTrack.getId(), changedLines, List.copyOf(removedLines)));
			editedTrack.getRemovedLines().clear();

		}
		if (editedTrack.isDirty()) {
			ClientPlayNetworking.send(new ApplyTrackMetaChangesC2S(editedTrack));
			if (editedTrack.getTrainMeta().isDirty()) {
				simulator = new TrackRideSimulator(editedTrack, this::setViewToTrainView, this::getSelectedObject);
			}
			editedTrack.setDirty(false);

			if (!editedTrack.getTrainMeta().getModelId().equals(itemModelId)) {
				itemModelId = editedTrack.getTrainMeta().getModelId();
				itemModel = Minecraft.getInstance().getModelManager().getItemModel(itemModelId);
			}
		}
		if (requestSaving) {
			requestSaving = false;
			ClientPlayNetworking.send(new SaveDataC2S(editedTrack.getId()));
		}
	}

	public void setDefaultView() {
		if (editedTrack.getLines().isEmpty()) {
			setView(new CreateFirstLineView(this));
		} else {
			setView(new LineEndPointView(this));
		}
	}

	public void setViewToTrainView() {
		setView(new View<TrackEditorMode>(this) {
			@Override
			public void load() {

			}

			@Override
			public void render(RenderContext renderContext) {

			}

			@Override
			public boolean handleAttack() {
				return false;
			}

			@Override
			public boolean handleDraggedAttack() {
				return false;
			}

			@Override
			public void leftMouseReleased() {

			}

			@Override
			public void endClientTick() {
				simulator.tick();
				if (simulator.getTrain() == null) return;
				InterpolatedPoint point = simulator.getTrainCarPoint(0);

				LocalPlayer player = Minecraft.getInstance().player;
				if (player == null) return;

				player.teleportSetPosition(new PositionMoveRotation(new Vec3(point.x(), point.y() - player.getEyeHeight(), point.z()), new Vec3(0, 0, 0), (float) Math.toDegrees(point.yaw()), (float) Math.toDegrees(point.pitch())), Set.of());
			}

			@Override
			public void renderImGui(ImGuiIO io) {
				if (ImGui.begin("Train view")) {
					if (ImGui.button("Exit")) {
						setDefaultView();
					}
				}
				ImGui.end();
			}
		});
	}

	@Override
	public boolean handleMouseClicked() {
		return getView().handleAttack();
	}

	@Override
	public boolean handleDraggedMouseClick() {
		return getView().handleDraggedAttack();
	}

	@Override
	public void handleLeftClickReleased() {
		getView().leftMouseReleased();
	}

	@Override
	public boolean handleKeyPressed(KeyEvent keyEvent) {
		if (keyEvent.isEscape() && !isNothingSelected()) {
			select(null);
			return true;
		}
		return false;
	}

	public void addLine(Line line) {
		editedTrack.getLines().add(line);
		addComponentsToLine(line);
	}

	public void addComponentsToLine(Line line) {
		line.addComponent(new DirtContainer());
		line.addComponent(new LineBoxComponent(line));
		line.addComponent(new LineRenderImGuiComponent(line, editedTrack));

		line.getInputEndPoint().addComponent(new DirtContainer());
		line.getInputEndPoint().addComponent(new EndpointBoxComponent(line.getInputEndPoint()));

		line.getOutputEndPoint().addComponent(new DirtContainer());
		line.getOutputEndPoint().addComponent(new EndpointBoxComponent(line.getOutputEndPoint()));
	}
}
