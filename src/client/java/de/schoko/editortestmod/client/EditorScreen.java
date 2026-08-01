package de.schoko.editortestmod.client;

import com.mojang.serialization.DataResult;
import de.schoko.editortestmod.TrainMeta;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.lines.CreateFirstLineView;
import de.schoko.editortestmod.client.points.LineEndPointView;
import de.schoko.editortestmod.codecs.TrackCodecs;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import de.schoko.editortestmod.packets.ApplyLineChangesC2S;
import de.schoko.editortestmod.packets.ApplyTrackMetaChangesC2S;
import de.schoko.editortestmod.packets.SaveDataC2S;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public non-sealed class EditorScreen extends Screen implements EditorDataScreen {
	private View view;
	private final Track editedTrack;
	private EditorObject selectedObject;
	private boolean mouseGrabbed;
	private boolean keyboardGrabbed;

	private boolean requestSaving;

	private boolean newlyOpen;

	private Identifier itemModelId;
	private ItemModel itemModel;
	private String inputtedItemModel;
	private long inputTime;

	private boolean renderItemModel;
	private boolean requestClosing;
	private float[] trackTransformation;

	private RideSimulator simulator;


	public EditorScreen(Track editedTrack) {
		super(Component.literal("Editor"));
		this.editedTrack = editedTrack;
		if (editedTrack.getLines().isEmpty()) {
			this.view = new CreateFirstLineView(this);
		} else this.view = new LineEndPointView(this);
		Minecraft.getInstance().player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));

		if (!editedTrack.getTrainMeta().getModelId().equals(itemModelId)) {
			itemModelId = editedTrack.getTrainMeta().getModelId();
			itemModel = Minecraft.getInstance().getModelManager().getItemModel(itemModelId);
		}
		this.renderItemModel = true;
		this.newlyOpen = true;

		this.simulator = new RideSimulator(editedTrack, this::getSelectedObject);
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (newlyOpen) {
			newlyOpen = false;
			io.clearEventsQueue();
			inputtedItemModel = null;
		}
		io.setWantCaptureKeyboard(!EditorTestModClient.isDraggingCamera());
		mouseGrabbed = io.getWantCaptureMouse();
		keyboardGrabbed = io.getWantCaptureKeyboard();
		view.renderImGui(io);
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
			ImGui.inputInt("##TicksPerSecondInput", imInt);
			editedTrack.setTicksInHertz(imInt.get());

			if (ImGui.button("Save")) {
				requestSaving = true;
			}
			ImGui.sameLine();
			if (ImGui.button("Save and close")) {
				requestSaving = true;
				requestClosing = true;
			}
			ImGui.sameLine();
			if (ImGui.button("Close without saving")) {
				requestClosing = true;
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

			if (ImGui.button("Print track string to console")) {
				editedTrack.bakeAcceleration();
				DataResult<Tag> result = TrackCodecs.CURRENT_CODEC.encodeStart(NbtOps.INSTANCE, editedTrack);

				if (result.result().isPresent()) {
					System.out.println(result.result().get());
				}
			}

			ImGui.separatorText("Cart Model");
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
			if (ImGui.inputFloat("##CarDistanceInput", imFloat)) trainMeta.setDirty(true);
			trainMeta.setCarDistance(imFloat.get());

			ImGui.text("Segments: ");
			ImGui.sameLine();
			imInt.set(trainMeta.getSegmentAmount());
			if (ImGui.inputInt("##SegmentAmountInput", imInt)) trainMeta.setDirty(true);
			trainMeta.setSegmentAmount(imInt.get());

			ImGui.text("Offset: ");
			ImGui.sameLine();
			float[] floatInputArray = new float[] {trainMeta.getOffset().x, trainMeta.getOffset().y, trainMeta.getOffset().z};
			if (ImGui.inputScalarN("##OffsetInput", floatInputArray, 3)) trainMeta.setDirty(true);
			trainMeta.getOffset().set(floatInputArray);

			ImGui.text("Pivot: ");
			ImGui.sameLine();
			floatInputArray = new float[] {trainMeta.getPivot().x, trainMeta.getPivot().y, trainMeta.getPivot().z};
			if (ImGui.inputScalarN("##PivotOffsetInput", floatInputArray, 3)) trainMeta.setDirty(true);
			trainMeta.getPivot().set(floatInputArray);

			ImGui.text("Yaw/Pitch/Roll: ");
			ImGui.sameLine();
			floatInputArray = new float[] {(float) Math.toDegrees(trainMeta.getYawOffset()), (float) Math.toDegrees(trainMeta.getPitchOffset()), (float) Math.toDegrees(trainMeta.getRollOffset())};
			if (ImGui.inputScalarN("##RotationOffsetInput", floatInputArray, 3)) {
				trainMeta.setDirty(true);
				trainMeta.setYawOffset((float) Math.toRadians(floatInputArray[0]));
				trainMeta.setPitchOffset((float) Math.toRadians(floatInputArray[1]));
				trainMeta.setRollOffset((float) Math.toRadians(floatInputArray[2]));
			}
		}
		ImGui.end();
	}



	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor gui, int i, int j, float f) {
		if (EditorTestModClient.isDraggingCamera()) return;
		super.extractRenderState(gui, i, j, f);
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {
		this.view.render(renderContext);
		this.simulator.extract(renderContext);
	}

	public EditorObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(EditorObject selectedObject) {
		this.selectedObject = selectedObject;
	}

	public Track getTrack() {
		return editedTrack;
	}

	public void endClientTick() {
		view.endClientTick();
		simulator.tick();
		List<Line> changedLines = new ArrayList<>();
		editedTrack.getLines().forEach(line -> {
			if (line.getRenderer().isDirty()) {
				changedLines.add(line);
				line.getRenderer().setDirty(false);
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
				simulator = new RideSimulator(editedTrack, this::getSelectedObject);
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
		if (requestClosing) {
			onClose();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if (mouseGrabbed) return true;
		if (super.mouseClicked(mouseButtonEvent, bl)) {
			return true;
		}
		if (view.handleAttack()) return true;
		EditorTestModClient.setDraggingCamera(true);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (keyboardGrabbed) return true;
		if (keyEvent.isEscape() && selectedObject != null) {
			setSelectedObject(null);
			return true;
		}
		EditorTestModClient.instance.processKeyEvent(keyEvent);
		return true;
	}

	@Override
	public void tick() {
		if (EditorTestModClient.isDraggingCamera() && Minecraft.getInstance().player != null) {
			KeyMapping.setAll();
			Minecraft.getInstance().player.input.tick();
			Minecraft.getInstance().player.applyInput();
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.connection.send(new ServerboundChangeGameModePacket(GameType.CREATIVE));
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return mouseGrabbed;
	}

	public boolean handleAttack() {
		if (mouseGrabbed) return true;
		return view.handleAttack();
	}

	public boolean handleDraggedAttack() {
		return this.view.handleDraggedAttack();
	}

	public void leftmouseReleased() {
		this.view.leftMouseReleased();
	}

	public void setView(LineEndPointView view) {
		this.view = view;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public ItemModel getItemModel() {
		return itemModel;
	}

	public boolean shouldRenderItemModel() {
		return renderItemModel;
	}

	public void setRenderItemModel(boolean renderItemModel) {
		this.renderItemModel = renderItemModel;
	}

	public RideSimulator getSimulator() {
		return simulator;
	}
}
