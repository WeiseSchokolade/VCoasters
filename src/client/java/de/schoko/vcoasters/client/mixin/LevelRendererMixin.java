package de.schoko.vcoasters.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.schoko.vcoasters.client.VCoastersClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "submitBlockOutline", cancellable = true)
	public void vcoasters$renderBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (!VCoastersClient.shouldRenderBlockOutline()) {
			ci.cancel();
		}
	}
}
