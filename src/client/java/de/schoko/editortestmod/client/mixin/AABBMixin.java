package de.schoko.editortestmod.client.mixin;

import de.schoko.editortestmod.client.core.hitboxes.Clippable;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.AABB;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AABB.class)
public abstract class AABBMixin implements EditorObject, Clippable {

	@Override
	public void draw(RenderContext context, Vector4f color) {
		context.drawAABB((AABB) ((Object) this), color);
	}
}
