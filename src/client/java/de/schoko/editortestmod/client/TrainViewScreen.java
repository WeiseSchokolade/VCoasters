package de.schoko.editortestmod.client;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.InterpolatedPoint;
import de.schoko.editortestmod.core.RenderContext;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public non-sealed class TrainViewScreen extends Screen implements EditorDataScreen {
	private final EditorScreen previousScreen;

	protected TrainViewScreen(EditorScreen previousScreen) {
		super(Component.literal("Train view"));
		this.previousScreen = previousScreen;
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().gui.setScreen(previousScreen);
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {
		renderContext.drawAABox(0, 0, 0, 1, 1, 1, 1, 1, 1, 0);
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		previousScreen.getSimulator().renderImGui(io);
		if (ImGui.begin("Train view")) {
			if (ImGui.button("Exit")) {
				onClose();
			}
		}
		ImGui.end();
	}

	@Override
	public EditorObject getSelectedObject() {
		return previousScreen.getSelectedObject();
	}

	@Override
	public Track getTrack() {
		return previousScreen.getTrack();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public void endClientTick() {
		RideSimulator simulator = previousScreen.getSimulator();
		simulator.tick();
		if (simulator.getTrain() == null) return;
		InterpolatedPoint point = simulator.getTrainCarPoint(0);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		player.teleportSetPosition(new PositionMoveRotation(new Vec3(point.x(), point.y() - player.getEyeHeight(), point.z()), new Vec3(0, 0, 0), (float) Math.toDegrees(point.yaw()), (float) Math.toDegrees(point.pitch())), Set.of());
	}
}
