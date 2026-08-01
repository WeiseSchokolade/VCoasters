package de.schoko.vcoasters.client.gizmo;

import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface Gizmo {
	void draw(RenderContext context, EditorObject target);

	Optional<Vec3> clip(int i, Vec3 from, Vec3 to);
	EditorObject getEditorObject(int index);

	void release();

	int getHitboxAmount();
}
