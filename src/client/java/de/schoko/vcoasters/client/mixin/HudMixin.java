package de.schoko.vcoasters.client.mixin;

import de.schoko.vcoasters.client.VCoastersClient;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public class HudMixin {
	@Inject(at = @At("HEAD"), method = "canRenderCrosshairForSpectator", cancellable = true)
	private void canRenderCrosshairForSpectator(HitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
		if (VCoastersClient.forceRenderCrosshair(hitResult)) {
			cir.setReturnValue(true);
		}
	}
}
