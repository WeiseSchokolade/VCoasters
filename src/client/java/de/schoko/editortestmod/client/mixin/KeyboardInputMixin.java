package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

	@Inject(method = "calculateImpulse", at = @At("HEAD"), cancellable = true)
	private static void calculateImpulse(boolean bl, boolean bl2, CallbackInfoReturnable<Float> cir) {
		if (EditorTestModClient.shouldCancelPlayerMovement()) {
			cir.setReturnValue(0.0f);
		}
	}
}
