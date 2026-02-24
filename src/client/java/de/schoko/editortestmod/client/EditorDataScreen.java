package de.schoko.editortestmod.client;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;

public interface EditorDataScreen {
	void render(RenderContext renderContext);

	EditorObject getSelectedObject();
	Track getTrack();
}
