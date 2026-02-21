package de.schoko.editortestmod.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import de.schoko.editortestmod.client.EditorTestModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "renderBlockOutline", cancellable = true)
	public void editortestmod$renderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (!EditorTestModClient.shouldRenderBlockOutline()) {
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void editortestmod$renderLevel(CallbackInfo ci, @Local(argsOnly = true, ordinal = 1) Matrix4f projection, @Local(argsOnly = true) Camera camera) {
		EditorTestModClient.setLastProjectionMatrix(projection);
		EditorTestModClient.setLastCamera(camera);
	}
}
