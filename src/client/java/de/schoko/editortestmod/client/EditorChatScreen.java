package de.schoko.editortestmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public class EditorChatScreen extends ChatScreen {
	private final Screen previousScreen;

	public EditorChatScreen(Screen previousScreen, String string, boolean bl) {
		super(string, bl);
		this.previousScreen = previousScreen;
	}

	@Override
	public void onClose() {
		super.onClose();
		Minecraft.getInstance().setScreen(previousScreen);
	}
}
