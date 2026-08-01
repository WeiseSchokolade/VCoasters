package de.schoko.vcoasters.client.mixin;

import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.mixininterfaces.ExtendedMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements ExtendedMouseHandler {
	@Shadow private boolean mouseGrabbed;
	@Shadow private double xpos;
	@Shadow private double ypos;
	@Final
	@Shadow private Minecraft minecraft;

	@Shadow public abstract void releaseMouse();

	@Inject(at = @At("HEAD"), method = "grabMouse", cancellable = true)
	public void freeMouse(CallbackInfo ci) {
		if (VCoastersClient.shouldFreeMouse()) {
			if (mouseGrabbed) {
				releaseMouse();
			}
			ci.cancel();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;simulateRightClick(Lnet/minecraft/client/input/MouseButtonInfo;Z)Lnet/minecraft/client/input/MouseButtonInfo;"), method = "onButton")
	public void mouseReleased(long handle, MouseButtonInfo rawButtonInfo, int i, CallbackInfo ci) {
		if (i == 0 && rawButtonInfo.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			VCoastersClient.setDraggingCamera(false);
			VCoastersClient.leftMouseReleased();
		}
	}

	@Unique
	public void vcoasters$releaseMouse(double x, double y) {
		this.mouseGrabbed = false;
		this.xpos = x;
		this.ypos = y;

		GLFW.glfwSetInputMode(this.minecraft.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
		GLFW.glfwSetCursorPos(this.minecraft.getWindow().handle(), x, y);
	}
}
