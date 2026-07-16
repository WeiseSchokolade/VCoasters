package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
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
		if (EditorTestModClient.isDraggingCamera()) ci.cancel();
	}
}
