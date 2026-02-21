package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "close", at = @At("RETURN"))
	private void init(CallbackInfo info) {
		EditorTestModClient.instance.close();
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
		if (!EditorTestModClient.shouldRenderBlockOutline()) {
			cir.setReturnValue(false);
		}
	}
}