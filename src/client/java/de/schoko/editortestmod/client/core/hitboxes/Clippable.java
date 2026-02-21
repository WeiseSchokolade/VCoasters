package de.schoko.editortestmod.client.core.hitboxes;

import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.Optional;

public interface Clippable extends EditorObject {
	Optional<Vec3> clip(Vec3 from, Vec3 to);

	void draw(RenderContext context, Vector4f color);
}
