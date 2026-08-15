package de.schoko.vcoasters.client;

import de.schoko.vcoasters.VCoasters;
import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.core.RenderContextImpl;
import de.schoko.vcoasters.client.mixininterfaces.ExtendedMouseHandler;
import de.schoko.vcoasters.client.trackmode.TrackEditorMode;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
import foundry.imgui.api.ImGuiMCEvents;
import imgui.ImGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCoastersClient implements ClientModInitializer {
	private static final Logger log = LoggerFactory.getLogger(VCoastersClient.class);
	public static VCoastersClient instance;
	private static final List<String> debugStrings = new ArrayList<>();
	public static Vec3 lastResult;
	private static boolean draggingCamera;
	private static long cameraDragBeginTime;

	private Matrix4f lastProjectionMatrix;
	private Camera lastCamera;

	private RenderContext renderCtx;
	private boolean grabbedMouse;

	private double prevX;
	private double prevY;


	@Override
	public void onInitializeClient() {

		instance = this;
		renderCtx = new RenderContextImpl();

		ImGuiMCEvents.INSTANCE.preRenderImGuiEvent(() -> {
			if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) {
				ImGui.pushFont(ImGui.getIO().getFontDefault(), 15);
				screen.renderImGui(ImGui.getIO());
				ImGui.popFont();
			}
			//ImGui.showDemoWindow();
		});

		//LevelRenderEvents.COLLECT_SUBMITS.register(context -> context.submitNodeCollector().submitItem());

		LevelExtractionEvents.END_EXTRACTION.register(context -> {
			if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) {
				screen.submitWorldObjects(renderCtx);
				screen.submitWorldModels(context);

				renderCtx.drawAABox(1, 1, 1, 2, 2, 2, 1, 0.5f, 1, 0.25f);
//				renderCtx.drawRhomboid(new Vector3f(-1, 1, 1), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), new Vector4f(0, 1, 0, 1));
				//if (lastResult != null)
				//	renderCtx.drawAABox(lastResult.x - 0.1, lastResult.y - 0.1, lastResult.z - 0.1, lastResult.x + 0.1, lastResult.y + 0.1, lastResult.z + 0.1, Colors.WHITE);
			}
		});

		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(renderCtx::renderAndDraw);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.gui.screen() instanceof EditorScreen screen) {
				screen.endClientTick();
			}
			//if (ImGuiMC.isImguiLoaded()) ImGui.showDemoWindow();
			if (isDraggingCamera()) {
				if (!grabbedMouse) {
					grabbedMouse = true;
					Minecraft.getInstance().mouseHandler.grabMouse();
				} else {
					grabbedMouse = false;
				}
			}
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(VCoasters.MOD_ID, "testelement"), (context, tickCounter) -> {
			if (Minecraft.getInstance().gui.screen() instanceof EditorScreen) context.text(Minecraft.getInstance().font, "Editor Mode", 5, 5, 0xFFFFFFFF);
			for (int i = 0; i < debugStrings.size(); i++) {
				context.text(Minecraft.getInstance().font, debugStrings.get(i), 5, 25 + i * 15, 0xE0E0FFFF);
			}
			debugStrings.clear();
		});

		EditorClientPackets.registerPackets();
	}

	public void close() {
		RenderContextImpl.close();
	}

	public void openTo(Track track) {
		String outputLineId;
		Map<String, Line> idLineMap = new HashMap<>();
		track.getLines().forEach(line -> idLineMap.put(line.getId(), line));
		for (Line line : track.getLines()) {
			if ((outputLineId = line.getOutputLineId()) != null) {
				line.setOutputLine(idLineMap.get(outputLineId));
			}
		}
		EditorScreen screen = new EditorScreen();
		Minecraft.getInstance().gui.setScreen(screen);
		screen.setMode(new TrackEditorMode(track));
	}

	public static boolean handleAttack() {
		if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) return screen.handleAttack();
		return false;
	}

	public static boolean handleContinuedAttack() {
		if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) return screen.handleDraggedAttack();
		return false;
	}

	public static void leftMouseReleased() {
		if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) screen.leftMouseReleased();
	}

	public static boolean cancelRenderBlockOutline() {
		return Minecraft.getInstance().gui.screen() instanceof EditorScreen;
	}

	public static boolean blockSpectatorAccess() {
		return (Minecraft.getInstance().gui.screen() instanceof EditorScreen);
	}

	public static boolean forceRenderCrosshair(HitResult result) {
		return (Minecraft.getInstance().gui.screen() instanceof EditorScreen) && isDraggingCamera() && false;
	}

	public static boolean shouldFreeMouse() {
		return (Minecraft.getInstance().gui.screen() instanceof EditorScreen) && !isDraggingCamera();
	}

	public static void setLastProjectionMatrix(Matrix4f lastProjectionMatrix) {
		instance.lastProjectionMatrix = lastProjectionMatrix;
	}

	public Matrix4f getLastProjectionMatrix() {
		return lastProjectionMatrix;
	}

	public static void setLastCamera(Camera camera) {
		VCoastersClient.instance.lastCamera = camera;
	}

	public Camera getLastCamera() {
		return lastCamera;
	}

	public static void setDraggingCamera(boolean camera) {
		if (VCoastersClient.draggingCamera == camera) return;
		VCoastersClient.draggingCamera = camera;
		if (!(Minecraft.getInstance().gui.screen() instanceof EditorScreen)) return;
		if (draggingCamera) {
			cameraDragBeginTime = System.currentTimeMillis();
			instance.prevX = Minecraft.getInstance().mouseHandler.xpos();
			instance.prevY = Minecraft.getInstance().mouseHandler.ypos();
			Minecraft.getInstance().mouseHandler.grabMouse();
		} else {
			((ExtendedMouseHandler) Minecraft.getInstance().mouseHandler).vcoasters$releaseMouse(instance.prevX, instance.prevY);
		}
	}

	public static boolean isDraggingCamera() {
		return draggingCamera && (System.currentTimeMillis() - cameraDragBeginTime > 75);
	}

	public static boolean shouldCancelPlayerMovement() {
		return Minecraft.getInstance().gui.screen() instanceof EditorScreen && !draggingCamera;
	}

	public static void addDebugString(String name, String value) {
		VCoastersClient.debugStrings.add(name + ": " + value);
	}

	public static <T> void addDebugString(String name, T value) {
		VCoastersClient.debugStrings.add(name + ": " + String.valueOf(value));
	}

	public static void addDebugString(String name, Vector3f vec) {
		if (vec == null) {
			addDebugString(name, "null");
			return;
		}
		addDebugString(name, vec.toString(NumberFormat.getNumberInstance()));
	}
}