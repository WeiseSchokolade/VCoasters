package de.schoko.vcoasters.client.core;

import de.schoko.vcoasters.client.EditorScreen;
import de.schoko.vcoasters.core.RenderContext;

public abstract class View implements RenderInterface {
	private final EditorScreen screen;

	public View(EditorScreen screen) {
		this.screen = screen;
	}

	public abstract void load();

	public abstract void render(RenderContext renderContext);

	public abstract boolean handleAttack();
	public abstract boolean handleDraggedAttack();

	public abstract void leftMouseReleased();

	public EditorScreen getScreen() {
		return screen;
	}

	public abstract void endClientTick();
}
