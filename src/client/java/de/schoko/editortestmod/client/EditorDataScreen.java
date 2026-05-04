package de.schoko.editortestmod.client;

import de.florianreuth.imguiexample.imgui.RenderInterface;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;

public sealed interface EditorDataScreen extends RenderInterface permits EditorScreen, EditorChatScreen, TrainViewScreen {
	void render(RenderContext renderContext);

	EditorObject getSelectedObject();
	Track getTrack();
}
