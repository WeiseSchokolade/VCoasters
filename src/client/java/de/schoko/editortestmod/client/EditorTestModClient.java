package de.schoko.editortestmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.EditorContext;
import de.schoko.editortestmod.client.core.EditorContextImpl;
import de.schoko.editortestmod.client.core.RenderContextImpl;
import de.schoko.editortestmod.client.editor.EditorCommands;
import de.schoko.editortestmod.client.editor.EditorState;
import de.schoko.editortestmod.client.lines.LineEditorView;
import de.schoko.editortestmod.client.points.LineEndPointView;
import de.schoko.editortestmod.client.renderer.EndPointRenderer;
import de.schoko.editortestmod.client.renderer.LineRenderer;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.Line;
import imgui.gl3.ImGuiImplGl3;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class EditorTestModClient implements ClientModInitializer {
	public static EditorTestModClient instance;
	public static KeyMapping toggleEditorKeybind;
	private static final List<String> debugStrings = new ArrayList<>();
	public static Vec3 lastResult;
	private static boolean draggingCamera;
	private static long cameraDragBeginTime;

	private Matrix4f lastProjectionMatrix;
	private Camera lastCamera;

	private EditorContextImpl editorCtx;
	private RenderContextImpl renderCtx;
	private boolean grabbedMouse;

	@Override
	public void onInitializeClient() {
		Line.rendererGetter = LineRenderer::new;
		EndPoint.rendererGetter = EndPointRenderer::new;

		instance = this;
		editorCtx = new EditorContextImpl();
		editorCtx.setCurrentScreen(new ButtonScreen());
		editorCtx.setView(new LineEditorView()); // Only used to load sample points
		editorCtx.setView(new LineEndPointView());
		renderCtx = new RenderContextImpl();
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
			if (getEditorCtx().editorActive()) {
				renderCtx.update(context, RenderContextImpl.FILLED_BOXES);
				getEditorCtx().getView().upload(renderCtx);
				renderCtx.drawAABox(1, 1, 1, 2, 2, 2, 1, 0.5f, 1, 1);
				renderCtx.drawRhomboid(new Vector3f(-1, 1, 1), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), new Vector4f(0, 1, 0, 1));
				if (lastResult != null)
					renderCtx.drawAABox(lastResult.x - 0.1, lastResult.y - 0.1, lastResult.z - 0.1, lastResult.x + 0.1, lastResult.y + 0.1, lastResult.z + 0.1, Colors.WHITE);
				renderCtx.endCall();

				renderCtx.executeDraw(Minecraft.getInstance());

				//renderCtx.update(context, RenderPipelines.TRANSLUCENT_MOVING_BLOCK);
				//renderCtx.endCall();
				//renderCtx.executeDraw(Minecraft.getInstance());
			}
			if (isDraggingCamera())
				if (!grabbedMouse) {
					grabbedMouse = true;
					Minecraft.getInstance().mouseHandler.grabMouse();
				}
			else grabbedMouse = false;
		});

		toggleEditorKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.editortestmod.editor.toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_J,
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "editor"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tickToggleEditorKeybind();
			if (editorCtx.editorActive()) {
				editorCtx.collectChanges();
			}
			if (EditorState.rideCar != null) {
				EditorState.rideCar.update();
			}
		});
		ClientLoginConnectionEvents.DISCONNECT.register((listener, minecraft) -> {
			editorCtx.setEditorActive(false);
			editorCtx.load(null);
		});
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((minecraft, level) -> editorCtx.setEditorActive(false));
		ServerLifecycleEvents.SERVER_STARTING.register(server -> editorCtx.setEditorActive(false));

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, "testelement"), (context, tickCounter) -> {
			if (editorCtx.editorActive()) context.drawString(Minecraft.getInstance().font, "Editor Mode", 5, 5, 0xFFFFFFFF);
			for (int i = 0; i < debugStrings.size(); i++) {
				context.drawString(Minecraft.getInstance().font, debugStrings.get(i), 5, 25 + i * 15, 0xE0E0FFFF);
			}
			debugStrings.clear();
			EditorState.doesImGuiCaptureMouseEvents = false;
		});

		EditorCommands.register();
		EditorClientPackets.registerPackets();
	}

	public void tickToggleEditorKeybind() {
		while (toggleEditorKeybind.consumeClick()) {
			if (Minecraft.getInstance().canSwitchGameMode() && Minecraft.getInstance().player.canUseGameMasterBlocks()) {
				editorCtx.setEditorActive(!editorCtx.editorActive());
			} else editorCtx.setEditorActive(false);
		}
	}

	public void processKeyEvent(KeyEvent event) {
		if (toggleEditorKeybind.matches(event)) {
			if (Minecraft.getInstance().canSwitchGameMode() && Minecraft.getInstance().player.canUseGameMasterBlocks()) {
				editorCtx.setEditorActive(!editorCtx.editorActive());
			} else editorCtx.setEditorActive(false);
		}
		if (Minecraft.getInstance().options.keyChat.matches(event)) {
			while (Minecraft.getInstance().options.keyChat.consumeClick()) {};
			Minecraft.getInstance().setScreen(new EditorChatScreen(Minecraft.getInstance().screen, "", false));
		}
		if (Minecraft.getInstance().options.keyCommand.matches(event)) {
			while (Minecraft.getInstance().options.keyCommand.consumeClick()) {};
			Minecraft.getInstance().setScreen(new EditorChatScreen(Minecraft.getInstance().screen, "/", false));
		}
	}

	public void close() {
		renderCtx.destroy();
	}

	public EditorContext getEditorCtx() {
		return editorCtx;
	}

	public static boolean handleAttack() {
		if (EditorState.doesImGuiCaptureMouseEvents) {
			return true;
		}
		if (instance.editorCtx.editorActive()) {
			instance.getEditorCtx().getView().handleAttack();
			return true;
		}
		return false;
	}

	public static boolean handleContinuedAttack() {
		if (instance.editorCtx.editorActive()) {
			instance.getEditorCtx().getView().handleDraggedAttack();
			return true;
		}
		return false;
	}

	public static void leftMouseReleased() {
		if (instance.editorCtx.editorActive()) {
			instance.getEditorCtx().getView().leftMouseReleased();
		}
	}

	public static boolean shouldRenderBlockOutline() {
		return !instance.editorCtx.editorActive();
	}

	public static boolean blockSpectatorAccess() {
		return instance.editorCtx.editorActive();
	}

	public static boolean forceRenderCrosshair(HitResult result) {
		return instance.editorCtx.editorActive() && isDraggingCamera();
	}

	public static boolean shouldFreeMouse() {
		return instance.editorCtx.editorActive() && !isDraggingCamera();
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
		if (!instance.editorCtx.editorActive()) return;
		if (draggingCamera) {

			cameraDragBeginTime = System.currentTimeMillis();
		} else {
			Minecraft.getInstance().mouseHandler.releaseMouse();
			if (instance.editorCtx.editorActive() && Minecraft.getInstance().screen == null) {
				Minecraft.getInstance().setScreen(instance.editorCtx.getCurrentScreen());
			}
		}
	}

	public static boolean isDraggingCamera() {
		return draggingCamera && (System.currentTimeMillis() - cameraDragBeginTime > 75);
	}

	public static boolean shouldCancelPlayerMovement() {
		return instance.editorCtx.editorActive() && !draggingCamera;
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