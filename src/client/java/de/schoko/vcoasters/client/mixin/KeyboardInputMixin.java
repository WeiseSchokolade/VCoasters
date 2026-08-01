package de.schoko.vcoasters.client.mixin;

import de.schoko.vcoasters.client.VCoastersClient;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

	@Inject(method = "calculateImpulse", at = @At("HEAD"), cancellable = true)
	private static void calculateImpulse(boolean positive, boolean negative, CallbackInfoReturnable<Float> cir) {
		if (VCoastersClient.shouldCancelPlayerMovement()) {
			cir.setReturnValue(0.0f);
		}
	}
}
