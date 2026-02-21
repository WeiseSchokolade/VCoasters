package de.schoko.editortestmod.client.core;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.lines.LineManager;
import de.schoko.editortestmod.core.EditorObject;
import net.minecraft.client.gui.screens.Screen;

public interface EditorContext {
	boolean editorActive();

	LineManager getLineManager();

	EditorObject getSelectedObject();
	boolean setSelectedObject(EditorObject editorObject);

	void setCurrentScreen(Screen currentScreen);
	Screen getCurrentScreen();

	View getView();
	void setView(View view);

	void load(Track track);

	void collectChanges();

	Track getTrack();
}
