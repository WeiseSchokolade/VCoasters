package de.schoko.editortestmod.client;

import de.schoko.editortestmod.client.core.EditorContext;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.core.RenderContext;
import imgui.ImGuiIO;

public class EmptyView extends View {
	@Override
	public void load(EditorContext editorContext) {

	}

	@Override
	public void upload(RenderContext renderContext) {

	}

	@Override
	public void render(ImGuiIO io) {

	}

	@Override
	public boolean handleAttack() {
		return false;
	}

	@Override
	public boolean handleDraggedAttack() {
		return false;
	}

	@Override
	public void leftMouseReleased() {

	}
}
