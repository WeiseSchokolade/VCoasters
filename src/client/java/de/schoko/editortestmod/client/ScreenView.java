package de.schoko.editortestmod.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class ScreenView {
	private final String name;
	private final ButtonScreen buttonScreen;
	private final boolean useVisibilityCheck;
	private boolean visible;

	private final List<GuiEventListener> eventListeners;
	private final List<Renderable> renderables;
	private final List<NarratableEntry> narratableEntries;

	public ScreenView(String name, ButtonScreen buttonScreen, boolean useVisibilityCheck) {
		this.name = name;
		this.buttonScreen = buttonScreen;
		this.useVisibilityCheck = useVisibilityCheck;

		eventListeners = new ArrayList<>();
		renderables = new ArrayList<>();
		narratableEntries = new ArrayList<>();
	}

	public void onShow() {

	}

	public void onHide() {

	}

	public abstract void onInit(int width, int height);

	public final void init(int width, int height) {
		eventListeners.clear();
		renderables.clear();
		narratableEntries.clear();
		onInit(width, height);
	}

	public final void show() {
		for (Renderable widget : renderables) {
			if (widget instanceof AbstractWidget abstractWidget) {
				abstractWidget.visible = true;
			}
		}
		visible = true;
		onShow();
	}

	public final void hide() {
		for (Renderable widget : renderables) {
			if (widget instanceof AbstractWidget abstractWidget) {
				abstractWidget.visible = false;
			}
		}
		visible = false;
		onHide();
	}

	public boolean shouldBeVisible() {
		return true;
	}

	public <T extends GuiEventListener & Renderable & NarratableEntry> T addWidget(T screenWidget) {
		eventListeners.add(screenWidget);
		renderables.add(screenWidget);
		narratableEntries.add(screenWidget);
		return screenWidget;
	}

	public ButtonScreen getButtonScreen() {
		return buttonScreen;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean useVisibilityCheck() {
		return useVisibilityCheck;
	}

	public List<? extends GuiEventListener> getEventListeners() {
		return eventListeners;
	}

	public List<Renderable> getRenderables() {
		return renderables;
	}

	public List<NarratableEntry> getNarratableEntries() {
		return narratableEntries;
	}
}
