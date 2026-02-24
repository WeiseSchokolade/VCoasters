package de.schoko.editortestmod.client;

import de.florianreuth.imguiexample.imgui.RenderInterface;
import de.schoko.editortestmod.CartModel;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.lines.CreateFirstLineView;
import de.schoko.editortestmod.client.points.LineEndPointView;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import de.schoko.editortestmod.packets.ApplyLineChangesC2S;
import de.schoko.editortestmod.packets.ApplyTrackMetaChangesC2S;
import de.schoko.editortestmod.packets.SaveDataC2S;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImDouble;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EditorScreen extends Screen implements RenderInterface, EditorDataScreen {
	private View view;
	private Track editedTrack;
	private EditorObject selectedObject;
	private boolean mouseGrabbed;
	private boolean keyboardGrabbed;

	private boolean requestSaving;

	private String itemModelId;
	private ItemModel itemModel;
	private boolean requestClosing;

	public EditorScreen(Track editedTrack) {
		super(Component.literal("Editor"));
		this.editedTrack = editedTrack;
		if (editedTrack.getLines().isEmpty()) {
			this.view = new CreateFirstLineView(this);
		} else this.view = new LineEndPointView(this);
		Minecraft.getInstance().player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));

		if (!editedTrack.getCartModel().getModelId().equals(itemModelId)) {
			itemModelId = editedTrack.getCartModel().getModelId();
			itemModel = Minecraft.getInstance().getModelManager().getItemModel(Identifier.parse(itemModelId));
		}
	}

	@Override
	public void render(ImGuiIO io) {
		mouseGrabbed = io.getWantCaptureMouse();
		keyboardGrabbed = io.getWantCaptureKeyboard();
		view.render(io);
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

			ImGui.separatorText("Cart Model");
			CartModel model = editedTrack.getCartModel();

			ImGui.text("Id: ");
			ImGui.sameLine();
			inputString.set(model.getModelId());
			if (ImGui.inputText("##InputCartModelId", inputString)) editedTrack.setDirty(true);
			model.setModelId(inputString.get());

			ImGui.text("Segments: ");
			ImGui.sameLine();
			imInt.set(model.getSegmentAmount());
			if (ImGui.inputInt("##SegmentAmountInput", imInt)) editedTrack.setDirty(true);
			model.setSegmentAmount(imInt.get());

			ImGui.text("Offset: ");
			ImGui.sameLine();
			float[] floatInputArray = new float[] {model.getOffset().x, model.getOffset().y, model.getOffset().z};
			if (ImGui.inputScalarN("##OffsetInput", floatInputArray, 3)) editedTrack.setDirty(true);
			model.getOffset().set(floatInputArray);

			ImGui.text("Pivot: ");
			ImGui.sameLine();
			floatInputArray = new float[] {model.getPivot().x, model.getPivot().y, model.getPivot().z};
			if (ImGui.inputScalarN("##PivotOffsetInput", floatInputArray, 3)) editedTrack.setDirty(true);
			model.getPivot().set(floatInputArray);

			ImGui.text("Yaw/Pitch/Roll: ");
			ImGui.sameLine();
			floatInputArray = new float[] {(float) Math.toDegrees(model.getYawOffset()), (float) Math.toDegrees(model.getPitchOffset()), (float) Math.toDegrees(model.getRollOffset())};
			if (ImGui.inputScalarN("##RotationOffsetInput", floatInputArray, 3)) {
				editedTrack.setDirty(true);
				model.setYawOffset((float) Math.toRadians(floatInputArray[0]));
				model.setPitchOffset((float) Math.toRadians(floatInputArray[1]));
				model.setRollOffset((float) Math.toRadians(floatInputArray[2]));
			}
		}
		ImGui.end();
	}

	@Override
	public void render(@NotNull GuiGraphics gui, int i, int j, float f) {
		if (EditorTestModClient.isDraggingCamera()) return;
		super.render(gui, i, j, f);
	}

	@Override
	public void render(RenderContext renderContext) {
		this.view.render(renderContext);
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
			editedTrack.setDirty(false);

			if (!editedTrack.getCartModel().getModelId().equals(itemModelId)) {
				itemModelId = editedTrack.getCartModel().getModelId();
				itemModel = Minecraft.getInstance().getModelManager().getItemModel(Identifier.parse(itemModelId));
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
		if (EditorTestModClient.isDraggingCamera()) {
			KeyMapping.setAll();
			Minecraft.getInstance().player.input.tick();
			Minecraft.getInstance().player.applyInput();
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		Minecraft.getInstance().player.connection.send(new ServerboundChangeGameModePacket(GameType.CREATIVE));
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
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public ItemModel getItemModel() {
		return itemModel;
	}
}
