package de.schoko.vcoasters.client.modes.train;

import de.schoko.vcoasters.client.modes.EditGroup;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.Line;

import java.util.List;
import java.util.Objects;

public record LineEditGroup(String id, List<Line> lines) implements EditGroup {
	@Override
	public boolean isMember(EditorObject o) {
		if (o instanceof Line line) return id.equals(line.getEditGroup());
		return false;
	}

	public Line getStart() {
		Line startLine = null;
		for (Line line : lines) {
			if (line.getInputLine() == null || !Objects.equals(line.getInputLine().getEditGroup(), id)) {
				if (startLine != null) return null;
				startLine = line;
			}
		}
		return startLine;
	}

	public Line getEnd() {
		Line endLine = null;
		for (Line line : lines) {
			if (line.getOutputLine() == null || !Objects.equals(line.getOutputLine().getEditGroup(), id)) {
				if (endLine != null) return null;
				endLine = line;
			}
		}
		return endLine;
	}

	public Line getExtensionLine() {
		Line extensionLine = null;
		for (Line line : lines) {
			if (line.getOutputLine() == null) {
				if (extensionLine != null) return null;
				extensionLine = line;
			}
		}
		return extensionLine;
	}

	public Line getInputtingLine() {
		Line inputtingLine = null;
		for (Line line : lines) {
			Line inputLine = line.getInputLine();
			if (inputLine != null) {
				if (id.equals(inputLine.getEditGroup())) continue;
				if (inputtingLine != null) return null;
				inputtingLine = inputLine;
			}
		}
		return inputtingLine;
	}
}
