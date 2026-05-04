package de.schoko.editortestmod.client;

import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.RenderContextImpl;
import de.schoko.editortestmod.client.editor.EditorCommands;
import de.schoko.editortestmod.client.mixininterfaces.ExtendedMouseHandler;
import de.schoko.editortestmod.client.renderer.EndPointRenderer;
import de.schoko.editortestmod.client.renderer.LineRenderer;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.Line;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorTestModClient implements ClientModInitializer {
	public static EditorTestModClient instance;
	private static final List<String> debugStrings = new ArrayList<>();
	public static Vec3 lastResult;
	private static boolean draggingCamera;
	private static long cameraDragBeginTime;

	private Matrix4f lastProjectionMatrix;
	private Camera lastCamera;

	private RenderContextImpl renderCtx;
	private boolean grabbedMouse;

	private double prevX;
	private double prevY;


	@Override
	public void onInitializeClient() {
		Line.rendererGetter = LineRenderer::new;
		EndPoint.rendererGetter = EndPointRenderer::new;

		instance = this;
		renderCtx = new RenderContextImpl();
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(context -> {
			if (Minecraft.getInstance().screen instanceof EditorDataScreen screen) {
				renderCtx.update(context, RenderContextImpl.FILLED_BOXES);
				screen.render(renderCtx);
//				renderCtx.drawAABox(1, 1, 1, 2, 2, 2, 1, 0.5f, 1, 1);
//				renderCtx.drawRhomboid(new Vector3f(-1, 1, 1), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), new Vector4f(0, 1, 0, 1));
				if (lastResult != null)
					renderCtx.drawAABox(lastResult.x - 0.1, lastResult.y - 0.1, lastResult.z - 0.1, lastResult.x + 0.1, lastResult.y + 0.1, lastResult.z + 0.1, Colors.WHITE);
				renderCtx.endCall();

				renderCtx.executeDraw(Minecraft.getInstance());
			}
			if (isDraggingCamera())
				if (!grabbedMouse) {
					grabbedMouse = true;
					Minecraft.getInstance().mouseHandler.grabMouse();
				}
			else grabbedMouse = false;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.screen instanceof EditorScreen screen) {
				screen.endClientTick();
			} else if (client.screen instanceof TrainViewScreen screen) {
				screen.endClientTick();
			}
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "testelement"), (context, tickCounter) -> {
			if (Minecraft.getInstance().screen instanceof EditorDataScreen) context.text(Minecraft.getInstance().font, "Editor Mode", 5, 5, 0xFFFFFFFF);
			for (int i = 0; i < debugStrings.size(); i++) {
				context.text(Minecraft.getInstance().font, debugStrings.get(i), 5, 25 + i * 15, 0xE0E0FFFF);
			}
			debugStrings.clear();
		});

		EditorClientPackets.registerPackets();
	}

	public void processKeyEvent(KeyEvent event) {
		if (Minecraft.getInstance().screen instanceof EditorDataScreen screen) {
			if (Minecraft.getInstance().options.keyChat.matches(event)) {
				while (Minecraft.getInstance().options.keyChat.consumeClick()) {}
				Minecraft.getInstance().setScreen(new EditorChatScreen(screen, "", false));
			}
			if (Minecraft.getInstance().options.keyCommand.matches(event)) {
				while (Minecraft.getInstance().options.keyCommand.consumeClick()) {}
				Minecraft.getInstance().setScreen(new EditorChatScreen(screen, "/", false));
			}
		}
	}

	public void close() {
		renderCtx.destroy();
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
		Minecraft.getInstance().setScreen(new EditorScreen(track));
	}

	public static boolean handleAttack() {
		if (Minecraft.getInstance().screen instanceof EditorScreen screen) return screen.handleAttack();
		return false;
	}

	public static boolean handleContinuedAttack() {
		if (Minecraft.getInstance().screen instanceof EditorScreen screen) return screen.handleDraggedAttack();
		return false;
	}

	public static void leftMouseReleased() {
		if (Minecraft.getInstance().screen instanceof EditorScreen screen) screen.leftmouseReleased();
	}

	public static boolean shouldRenderBlockOutline() {
		return !(Minecraft.getInstance().screen instanceof EditorDataScreen);
	}

	public static boolean blockSpectatorAccess() {
		return (Minecraft.getInstance().screen instanceof EditorDataScreen);
	}

	public static boolean forceRenderCrosshair(HitResult result) {
		return (Minecraft.getInstance().screen instanceof EditorDataScreen) && isDraggingCamera() && false;
	}

	public static boolean shouldFreeMouse() {
		return (Minecraft.getInstance().screen instanceof EditorDataScreen) && !isDraggingCamera();
	}

	public static void setLastProjectionMatrix(Matrix4f lastProjectionMatrix) {
		instance.lastProjectionMatrix = lastProjectionMatrix;
	}

	public Matrix4f getLastProjectionMatrix() {
		return lastProjectionMatrix;
	}

	public static void setLastCamera(Camera camera) {
		EditorTestModClient.instance.lastCamera = camera;
	}

	public Camera getLastCamera() {
		return lastCamera;
	}

	public static void setDraggingCamera(boolean camera) {
		if (EditorTestModClient.draggingCamera == camera) return;
		EditorTestModClient.draggingCamera = camera;
		if (!(Minecraft.getInstance().screen instanceof EditorScreen)) return;
		if (draggingCamera) {
			cameraDragBeginTime = System.currentTimeMillis();
			instance.prevX = Minecraft.getInstance().mouseHandler.xpos();
			instance.prevY = Minecraft.getInstance().mouseHandler.ypos();
			Minecraft.getInstance().mouseHandler.grabMouse();
		} else {
			((ExtendedMouseHandler) Minecraft.getInstance().mouseHandler).editorTestMod$releaseMouse(instance.prevX, instance.prevY);
		}
	}

	public static boolean isDraggingCamera() {
		return draggingCamera && (System.currentTimeMillis() - cameraDragBeginTime > 75);
	}

	public static boolean shouldCancelPlayerMovement() {
		return Minecraft.getInstance().screen instanceof EditorScreen && !draggingCamera;
	}

	public static void addDebugString(String name, String value) {
		EditorTestModClient.debugStrings.add(name + ": " + value);
	}

	public static <T> void addDebugString(String name, T value) {
		EditorTestModClient.debugStrings.add(name + ": " + String.valueOf(value));
	}

	public static void addDebugString(String name, Vector3f vec) {
		if (vec == null) {
			addDebugString(name, "null");
			return;
		}
		addDebugString(name, vec.toString(NumberFormat.getNumberInstance()));
	}
}