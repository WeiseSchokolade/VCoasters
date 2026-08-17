package de.schoko.vcoasters.client;

import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;
import imgui.ImGuiIO;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.input.KeyEvent;

public abstract class EditorMode<T extends EditorMode<T>> {
	private View<T> view;
	private EditorObject selectedObject;
	private boolean isDiscarded;

	public abstract void submitWorldObjects(RenderContext renderContext);

	final void submitCompleteWorldObjects(RenderContext renderContext) {
		view.render(renderContext);
		this.submitWorldObjects(renderContext);
	}

	public abstract void submitWorldModels(LevelExtractionContext context);

	final void submitCompleteWorldModels(LevelExtractionContext context) {
		this.submitWorldModels(context);
	}

	public abstract void renderImGui(ImGuiIO io);

	final void renderCompleteImGui(ImGuiIO io) {
		view.renderImGui(io);
		this.renderImGui(io);
	}

	public abstract void endClientTick();

	final void endCompleteClientTick() {
		view.endClientTick();
		this.endClientTick();
	}

	public boolean isSelected(EditorObject object) {
		return selectedObject == object;
	}

	public void select(EditorObject object) {
		this.selectedObject = object;
	}

	public boolean isNothingSelected() {
		return selectedObject == null;
	}

	public EditorObject getSelectedObject() {
		return selectedObject;
	}

	public void setView(View<T> view) {
		this.view = view;
	}

	public View<T> getView() {
		return view;
	}

	public void close() {
		this.isDiscarded = true;
	}

	public boolean isClosed() {
		return isDiscarded;
	}

	public boolean handleMouseClicked() {
		return view.handleAttack();
	}

	public boolean handleKeyPressed(KeyEvent keyEvent) {
		return false;
	}

	public boolean handleDraggedMouseClick() {
		return view.handleDraggedAttack();
	}

	public void handleLeftClickReleased() {
		view.leftMouseReleased();
	}
}
