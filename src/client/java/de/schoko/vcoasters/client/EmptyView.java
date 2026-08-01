package de.schoko.vcoasters.client;

import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.core.RenderContext;
import imgui.ImGuiIO;

public class EmptyView extends View {
	public EmptyView(EditorScreen screen) {
		super(screen);
	}

	@Override
	public void load() {

	}

	@Override
	public void render(RenderContext renderContext) {

	}

	@Override
	public void renderImGui(ImGuiIO io) {

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

	@Override
	public void endClientTick() {

	}
}
