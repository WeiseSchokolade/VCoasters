package de.schoko.vcoasters.client.mixin;

import de.schoko.vcoasters.client.VCoastersClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	public void handleSetScreen(Screen screen, CallbackInfo ci) {
		if (VCoastersClient.isDraggingCamera()) ci.cancel();
	}
}
