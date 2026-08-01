package de.schoko.vcoasters;

import de.schoko.vcoasters.core.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackLineManager {
	private static final List<Line> lines = new ArrayList<>();
	private static final Map<String, Line> idLineMap = new HashMap<>();
	// Value: Line whose output (Id: key) did not exist when they were added
	private static final Map<String, Line> pendingOutputs = new HashMap<>();

	public static void addLine(Line line) {
		lines.add(line);
		idLineMap.put(line.getId(), line);
		if (pendingOutputs.containsKey(line.getId())) {
			pendingOutputs.get(line.getId()).setOutputLine(line);
			pendingOutputs.remove(line.getId());
		}
		setOutputOrAddAsPendingConnection(line, line.getOutputLineId());
	}

	public static void addLines(List<Line> lines) {
		lines.forEach(TrackLineManager::addLine);
	}

	public static void replaceOutput(Line line, String outputId) {
		if (pendingOutputs.containsValue(line)) {
			pendingOutputs.replaceAll((s, line1) -> {
				if (line1 == line) return null;
				else return line1;
			});
		} else if (line.getOutputLine() != null) {
			line.setOutputLine(null);
		}
		setOutputOrAddAsPendingConnection(line, outputId);
	}

	public static void setOutputOrAddAsPendingConnection(Line line, String outputId) {
		if (outputId == null) return;
		if (idLineMap.containsKey(outputId)) {
			line.setOutputLine(idLineMap.get(outputId));
		} else {
			pendingOutputs.put(outputId, line);
		}
	}

	public static boolean contains(String id) {
		return idLineMap.containsKey(id);
	}

	public static boolean contains(Line line) {
		return idLineMap.containsKey(line.getId());
	}

	public static Line getLine(String id) {
		return idLineMap.get(id);
	}

	public static void removeLine(Line line) {
		if (pendingOutputs.containsValue(line)) {
			pendingOutputs.replaceAll((s, line1) -> {
				if (line1 == line) return null;
				else return line1;
			});
		}
		line.cutOut();
		idLineMap.remove(line.getId());
		lines.remove(line);
	}

	public static void clear() {
		idLineMap.clear();
		lines.clear();
		pendingOutputs.clear();
	}
}
