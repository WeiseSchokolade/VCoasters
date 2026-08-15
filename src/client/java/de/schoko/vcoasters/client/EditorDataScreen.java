package de.schoko.vcoasters.client;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.core.RenderInterface;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;

sealed interface EditorDataScreen extends RenderInterface permits EditorScreen {
	void submitWorldObjects(RenderContext renderContext);
	void submitWorldModels(LevelExtractionContext context);
}
