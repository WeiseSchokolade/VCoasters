package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorGui.class)
public class SpectatorGuiMixin {
	@Inject(at = @At("HEAD"), method = "onHotbarSelected", cancellable = true)
	public void handleHotbarKey(int slot, CallbackInfo ci) {
		if (EditorTestModClient.blockSpectatorAccess()) {
			ci.cancel();
		}
	}
}
