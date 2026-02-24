package de.schoko.editortestmod.client.core;

import de.florianreuth.imguiexample.imgui.RenderInterface;
import de.schoko.editortestmod.client.EditorScreen;
import de.schoko.editortestmod.core.RenderContext;

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
