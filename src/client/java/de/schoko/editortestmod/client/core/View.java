package de.schoko.editortestmod.client.core;

import de.florianreuth.imguiexample.imgui.RenderInterface;
import de.schoko.editortestmod.core.RenderContext;

public abstract class View implements RenderInterface {
	private EditorContext context;

	public abstract void load(EditorContext editorContext);

	public abstract void upload(RenderContext renderContext);

	public abstract boolean handleAttack();
	public abstract boolean handleDraggedAttack();

	public abstract void leftMouseReleased();

	public void setContext(EditorContext context) {
		this.context = context;
	}

	public EditorContext getContext() {
		return context;
	}

}
