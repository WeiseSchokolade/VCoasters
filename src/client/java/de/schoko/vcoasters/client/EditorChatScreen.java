package de.schoko.vcoasters.client;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;
import imgui.ImGuiIO;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

public non-sealed class EditorChatScreen extends ChatScreen implements EditorDataScreen {
	private final EditorDataScreen previousScreen;

	public EditorChatScreen(EditorDataScreen previousScreen, String string, boolean bl) {
		super(string, bl);
		this.previousScreen = previousScreen;
		KeyMapping.releaseAll();
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().gui.setScreen((Screen) previousScreen);
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {
		previousScreen.submitWorldObjects(renderContext);
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		previousScreen.renderImGui(io);
	}

	@Override
	public EditorObject getSelectedObject() {
		return previousScreen.getSelectedObject();
	}

	@Override
	public Track getTrack() {
		return previousScreen.getTrack();
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) { // TODO: Keep up to date with new updates
		if (commandSuggestions.keyPressed(keyEvent)) {
			return true;
		} else if (this.isDraft && keyEvent.key() == 259) {
			this.input.setValue("");
			this.isDraft = false;
			return true;
		} else if (super.keyPressed(keyEvent)) {
			return true;
		} else if (keyEvent.isConfirmation()) {
			this.handleChatInput(this.input.getValue(), true);
			this.exitReason = ChatScreen.ExitReason.DONE;
			this.minecraft.gui.setScreen((Screen) previousScreen);
			return true;
		} else {
			switch (keyEvent.key()) {
				case 264:
					this.moveInHistory(1);
					break;
				case 265:
					this.moveInHistory(-1);
					break;
				//case 266: // TODO: Fix
				//	this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
				//	break;
				//case 267:
				//	this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
				//	break;
				default:
					return false;
			}

			return true;
		}
	}
}
