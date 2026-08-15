package de.schoko.vcoasters.client.core;

import de.schoko.vcoasters.client.EditorMode;
import de.schoko.vcoasters.client.EditorScreen;
import de.schoko.vcoasters.core.RenderContext;

public abstract class View<T extends EditorMode<T>> implements RenderInterface {
	private final T mode;

	public View(T mode) {
		this.mode = mode;
	}

	public abstract void load();

	public abstract void render(RenderContext renderContext);

	public abstract boolean handleAttack();
	public abstract boolean handleDraggedAttack();

	public abstract void leftMouseReleased();

	public T getMode() {
		return mode;
	}

	public abstract void endClientTick();
}
