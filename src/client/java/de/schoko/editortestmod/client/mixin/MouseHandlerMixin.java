package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow private boolean mouseGrabbed;

	@Shadow public abstract void releaseMouse();

	@Inject(at = @At("HEAD"), method = "grabMouse", cancellable = true)
	public void freeMouse(CallbackInfo ci) {
		if (EditorTestModClient.shouldFreeMouse()) {
			if (mouseGrabbed) {
				releaseMouse();
			}
			ci.cancel();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;simulateRightClick(Lnet/minecraft/client/input/MouseButtonInfo;Z)Lnet/minecraft/client/input/MouseButtonInfo;"), method = "onButton")
	public void mouseReleased(long l, MouseButtonInfo mouseButtonInfo, int i, CallbackInfo ci) {
		if (i == 0 && mouseButtonInfo.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			EditorTestModClient.setDraggingCamera(false);
			EditorTestModClient.leftMouseReleased();
		}
	}
}
