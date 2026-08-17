package de.schoko.vcoasters.client.modes.coaster;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.EditorMode;
import de.schoko.vcoasters.client.modes.coaster.renderer.CoasterEndpointComponent;
import de.schoko.vcoasters.client.modes.coaster.renderer.CoasterLineBoxComponent;
import de.schoko.vcoasters.core.DirtContainer;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;

public class CoasterEditorMode extends EditorMode<CoasterEditorMode> {
	private final Track track;

	public CoasterEditorMode(Track track) {
		this.track = track;
		track.getLines().forEach(this::addComponentsToLine);
		setDefaultView();
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {

	}

	@Override
	public void submitWorldModels(LevelExtractionContext context) {

	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (ImGui.begin("Editor Mode")) {
			if (ImGui.button("Close without saving")) {
				close();
			}
		}
		ImGui.end();
	}

	@Override
	public void endClientTick() {

	}

	public void setDefaultView() {
		if (track.getLines().isEmpty()) {
			setView(new StartLinePlacementView(this));
		} else {
			setView(new BasicEditorView(this));
		}
	}

	public Track getTrack() {
		return track;
	}

	public void addLine(Line line) {
		track.getLines().add(line);
		addComponentsToLine(line);
	}

	public void addComponentsToLine(Line line) {
		line.addComponent(new DirtContainer());
		line.getInputEndPoint().addComponent(new CoasterEndpointComponent(line.getInputEndPoint()));
		line.getOutputEndPoint().addComponent(new CoasterEndpointComponent(line.getOutputEndPoint()));
		line.addComponent(new CoasterLineBoxComponent(line));
	}
}
