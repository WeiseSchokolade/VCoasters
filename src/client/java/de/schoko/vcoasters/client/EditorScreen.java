package de.schoko.vcoasters.client;

import de.schoko.vcoasters.TrainMeta;
import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.export.DefaultExporter;
import de.schoko.vcoasters.client.lines.CreateFirstLineView;
import de.schoko.vcoasters.client.points.LineEndPointView;
import de.schoko.vcoasters.core.EditorObject;
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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public non-sealed class EditorScreen extends Screen implements EditorDataScreen {
	private EditorMode<?> editorMode;

	private boolean mouseGrabbed;
	private boolean keyboardGrabbed;

	private boolean newlyOpen;


	public EditorScreen() {
		super(Component.literal("Editor"));

		Minecraft.getInstance().player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));

		this.newlyOpen = true;

	}

	public void setMode(EditorMode<?> editorMode) {
		this.editorMode = editorMode;
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (newlyOpen) {
			newlyOpen = false;
			io.clearEventsQueue();
		}
		io.setWantCaptureKeyboard(!VCoastersClient.isDraggingCamera());
		mouseGrabbed = io.getWantCaptureMouse();
		keyboardGrabbed = io.getWantCaptureKeyboard();
		editorMode.renderCompleteImGui(io);
	}

	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor gui, int i, int j, float f) {
		if (VCoastersClient.isDraggingCamera()) return;
		super.extractRenderState(gui, i, j, f);
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {
		editorMode.submitCompleteWorldObjects(renderContext);
	}

	@Override
	public void submitWorldModels(LevelExtractionContext context) {
		editorMode.submitCompleteWorldModels(context);
	}

	public void endClientTick() {
		editorMode.endCompleteClientTick();
		if (editorMode.isClosed()) {
			onClose();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if (mouseGrabbed) return true;
		if (super.mouseClicked(mouseButtonEvent, bl)) {
			return true;
		}
		if (editorMode.handleMouseClicked()) return true;
		VCoastersClient.setDraggingCamera(true);
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (keyboardGrabbed) return true;
		return editorMode.handleKeyPressed(keyEvent);
	}

	@Override
	public void tick() {
		if (VCoastersClient.isDraggingCamera() && Minecraft.getInstance().player != null) {
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
		return editorMode.handleMouseClicked();
	}

	public boolean handleDraggedAttack() {
		return editorMode.handleDraggedMouseClick();
	}

	public void leftMouseReleased() {
		editorMode.handleLeftClickReleased();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
