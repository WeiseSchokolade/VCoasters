package de.schoko.editortestmod.core;

import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public abstract class Renderer<T extends EditorObject> {
	private final T object;
	private boolean dirty;

	public Renderer(T object) {
		this.object = object;
		this.dirty = false;
		updateHitbox(object);
	}

	public abstract void upload(RenderContext renderContext, EditorObject target, EditorObject selected);

	public abstract void updateHitbox(T object);

	// Returns: Intersection point, if any
	public abstract Optional<Vec3> clip(Vec3 from, Vec3 to);

	public T getObject() {
		return object;
	}

	public boolean isRendered(EditorObject object) {
		return object == this.object;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		if (dirty) updateHitbox(object);
	}

	public boolean isDirty() {
		return dirty;
	}
}
