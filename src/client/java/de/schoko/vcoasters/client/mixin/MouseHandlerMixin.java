package de.schoko.vcoasters.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.mixininterfaces.ExtendedMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
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

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;onInputReceived()V"), method = "onButton")
	public void mouseReleased(long handle, MouseButtonInfo rawButtonInfo, int i, CallbackInfo ci) {
		if (i == 0 && rawButtonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			VCoastersClient.setDraggingCamera(false);
			VCoastersClient.leftMouseReleased();
		}
	}

	@Unique
	public void vcoasters$releaseMouse(double x, double y) {
		this.mouseGrabbed = false;
		this.xpos = x;
		this.ypos = y;

		InputConstants.releaseMouse(this.minecraft.getWindow(), x, y);
		InputConstants.grabMouse(this.minecraft.getWindow(), x, y);

//		GLFW.glfwSetInputMode(this.minecraft.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
//		GLFW.glfwSetCursorPos(this.minecraft.getWindow().handle(), x, y);
	}
}
