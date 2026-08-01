package de.schoko.vcoasters.client;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.core.RenderInterface;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;

public sealed interface EditorDataScreen extends RenderInterface permits EditorScreen, EditorChatScreen, TrainViewScreen {
	void submitWorldObjects(RenderContext renderContext);

	EditorObject getSelectedObject();
	Track getTrack();
}
