package de.schoko.vcoasters.client.modes.train;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.EditorMode;
import de.schoko.vcoasters.client.modes.track.renderer.LineRenderImGuiComponent;
import de.schoko.vcoasters.client.modes.train.renderer.TrainLineBoxComponent;
import de.schoko.vcoasters.core.DirtContainer;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
import de.schoko.vcoasters.packets.ApplyLineChangesC2S;
import de.schoko.vcoasters.packets.ApplyTrackMetaChangesC2S;
import de.schoko.vcoasters.packets.SaveDataC2S;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiSliderFlags;
import imgui.type.ImInt;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;

import java.util.ArrayList;
import java.util.List;

public class TrainEditorMode extends EditorMode<TrainEditorMode> {
	private final Track track;

	public static final float MAX_SEGMENT_ANGLE = 15f;

	private float railGauge = 1.2f;
	private float beamLength = 2.4f;
	private float beamWidth = 0.4f;
	private float beamHeight = 0.2f;
	private float railHeight = 0.3f;
	private float railThickness = 0.2f;
	private float beamSpacing = 1f;

	private final List<Line> removedLines;

	public TrainEditorMode(Track track) {
		this.track = track;
		track.getLines().forEach(this::addComponentsToLine);
		setDefaultView();
		this.removedLines = new ArrayList<>();
	}

	@Override
	public void submitWorldObjects(RenderContext renderContext) {

	}

	@Override
	public void submitWorldModels(LevelExtractionContext context) {

	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (ImGui.begin("Train Editor Mode")) {
			if (ImGui.button("Place new")) {
				setView(new StartLinePlacementView(this));
			}

			if (ImGui.collapsingHeader("Rails")) {
				float[] floatInput = new float[] {railGauge};
				if (ImGui.dragFloat("Rail gauge", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) railGauge = floatInput[0];
				floatInput[0] = railHeight;
				if (ImGui.dragFloat("Rail height", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) railHeight = floatInput[0];
				floatInput[0] = railThickness;
				if (ImGui.dragFloat("Rail thickness", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) railThickness = floatInput[0];
				floatInput[0] = beamLength;
				if (ImGui.dragFloat("Beam length", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) beamLength = floatInput[0];
				floatInput[0] = beamWidth;
				if (ImGui.dragFloat("Beam width", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) beamWidth = floatInput[0];
				floatInput[0] = beamHeight;
				if (ImGui.dragFloat("Beam height", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) beamHeight = floatInput[0];
				floatInput[0] = beamSpacing;
				if (ImGui.dragFloat("Beam spacing", floatInput, 0.01f, 0.04f, 10f, ImGuiSliderFlags.Logarithmic)) beamSpacing = floatInput[0];
			}
			if (ImGui.collapsingHeader("Train design##TrainDesignCollapsingMenu")) {
				ImGui.text("Segments: ");
				ImGui.sameLine();
				ImInt imInt = new ImInt();
				imInt.set(track.getTrainMeta().getSegmentAmount());
				if (ImGui.inputInt("##SegmentAmountInput", imInt) && imInt.get() > 0) {
					track.getTrainMeta().setSegmentAmount(imInt.get());
				}

			}

			if (ImGui.button("Close without saving")) {
				close();
			}
			if (ImGui.button("Save")) {
				ClientPlayNetworking.send(new SaveDataC2S(track.getId()));
			}
			if (ImGui.button("Save and close")) {
				ClientPlayNetworking.send(new SaveDataC2S(track.getId()));
				close();
			}
		}
		ImGui.end();

		if (getSelectedObject() instanceof Line line) {
			line.getComponent(LineRenderImGuiComponent.class).renderImGui(io);
		}
	}

	@Override
	public void endClientTick() {

		if (track.isDirty()) {
			ClientPlayNetworking.send(new ApplyTrackMetaChangesC2S(track));
			track.setDirty(false);
		}
		List<Line> changedLines = new ArrayList<>();
		for (Line line : track.getLines()) {
			if (line.getComponent(DirtContainer.class).isDirty()) {
				changedLines.add(line);
				line.getComponent(DirtContainer.class).setDirty(false);
			}
		}
		if (!changedLines.isEmpty() || !removedLines.isEmpty()) ClientPlayNetworking.send(new ApplyLineChangesC2S(track.getId(), changedLines, new ArrayList<>(removedLines)));
		removedLines.clear();
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

	public void removeLine(Line line) {
		track.removeLine(line.getId());
		removedLines.add(line);
	}

	public void addComponentsToLine(Line line) {
		line.addComponent(new DirtContainer(true));
		line.addComponent(new TrainLineBoxComponent(line, this));
		line.addComponent(new LineRenderImGuiComponent(line, track));
	}

	public float getRailGauge() {
		return railGauge;
	}

	public float getBeamLength() {
		return beamLength;
	}

	public float getBeamWidth() {
		return beamWidth;
	}

	public float getBeamHeight() {
		return beamHeight;
	}

	public float getRailHeight() {
		return railHeight;
	}

	public float getRailThickness() {
		return railThickness;
	}

	public float getBeamSpacing() {
		return beamSpacing;
	}

}
