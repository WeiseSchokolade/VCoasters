package de.schoko.vcoasters.client.modes.track.renderer;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.core.EditorComponent;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.LinePhysicsType;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;

public record LineRenderImGuiComponent(Line line, Track track) implements EditorComponent {
	public void renderImGui(ImGuiIO io) {

		if (ImGui.begin("Line")) {
			ImGui.text("Id: " + line.getId());
			ImGui.sameLine();
			if (ImGui.button("Copy")) {
				Minecraft.getInstance().keyboardHandler.setClipboard(line.getId());
			}

			ImGui.text("InputLine: ");
			ImGui.sameLine();
			if (line.getInputLine() == null) {
				ImGui.text("null");
			} else {
				ImGui.text(line.getInputLineId());
			}

			renderOutputLine(io, line, track);

			ImGui.text("Length (in cb): " + Math.round(line.getLength() * 100));
			if (line.getLength() > 20) {
				ImGui.textColored(0xFF0000, "Line is too long! Only up to 20 blocks can be rendered properly!");
			}

			ImGui.text("Label: ");
			ImGui.sameLine();
			ImString string = new ImString();
			string.set(line.getLabel() != null ? line.getLabel() : "");
			if (ImGui.inputText("##LabelInput", string)) {
				line.setLabel(string.get().isBlank() ? null : string.get());
			}

			for (LinePhysicsType value : LinePhysicsType.values()) {
				if (ImGui.radioButton(value.name(), line.getPhysicsType() == value || (value == LinePhysicsType.REGULAR && line.getPhysicsType() == null))) {
					line.setPhysicsType(value);
				}
			}

			ImGui.text("OnReachFunction: ");
			ImGui.sameLine();
			string.set(line.getOnReachFunction() == null ? "" : line.getOnReachFunction());
			if (ImGui.inputText("##OnReachFunctionInput", string))
				line.setOnReachFunction(string.get().isBlank() || string.get().equals("null") ? null : string.get());

			ImGui.text("OnHaltFunction: ");
			ImGui.sameLine();
			string.set(line.getOnHaltFunction() == null ? "" : line.getOnHaltFunction());
			if (ImGui.inputText("##OnHaltFunctionInput", string))
				line.setOnHaltFunction(string.get().isBlank() || string.get().equals("null") ? null : string.get());

			if (line.getPhysicsType() == LinePhysicsType.STATION) {
				ImGui.text("Brakes engaged: ");
				ImGui.sameLine();
				ImBoolean fullStop = new ImBoolean();
				fullStop.set(line.isFullStop());
				ImGui.checkbox("##FullStop", fullStop);
				line.setFullStop(fullStop.get());
				if (ImGui.button("Release brakes")) {
					Line inspectedLine = line;
					while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
						inspectedLine.setFullStop(false);
						inspectedLine = inspectedLine.getOutputLine();
					}
					inspectedLine = line;
					while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
						inspectedLine.setFullStop(false);
						inspectedLine = inspectedLine.getInputLine();
					}
				}
				ImGui.sameLine();
				if (ImGui.button("Engage brakes")) {
					Line inspectedLine = line;
					while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
						inspectedLine.setFullStop(true);
						inspectedLine = inspectedLine.getOutputLine();
					}
					inspectedLine = line;
					while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
						inspectedLine.setFullStop(true);
						inspectedLine = inspectedLine.getInputLine();
					}
				}
			}
		}
		ImGui.end();
	}

	public static void renderOutputLine(ImGuiIO io, Line line, Track track) {
		ImGui.text("OutputLine: ");
		ImGui.sameLine();
		ImString string = new ImString();
		string.set(line.getOutputLineId() == null ? "" : line.getOutputLineId());
		if (ImGui.inputText("##OutputLineInput", string)) {
			line.setOutputLine(string.get().isBlank() ? null : track.getLine(string.get()));
		}
	}
}
