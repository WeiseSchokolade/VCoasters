package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	public void handleAttack(CallbackInfoReturnable<Boolean> cir) {
		if (EditorTestModClient.handleAttack()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	public void handleContinueAttack(boolean bl, CallbackInfo ci) {
		if (EditorTestModClient.handleContinuedAttack()) {
			ci.cancel();
		}
	}

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	public void handleSetScreen(Screen screen, CallbackInfo ci) {
		if (EditorTestModClient.isDraggingCamera()) ci.cancel();
	}
}
