package de.schoko.vcoasters.client.mixin;

import de.schoko.vcoasters.client.VCoastersClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
	@Shadow
	@Final
	private Projection projection;

	@Inject(at = @At("RETURN"), method = "extractRenderState")
	public void vcoasters$extractRenderState(CameraRenderState cameraState, float cameraEntityPartialTicks, CallbackInfo ci) {
		VCoastersClient.setLastProjectionMatrix(this.projection.getMatrix(new Matrix4f()));
		VCoastersClient.setLastCamera((Camera) ((Object) this));
	}
}
