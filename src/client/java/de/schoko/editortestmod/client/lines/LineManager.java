package de.schoko.editortestmod.client.lines;

import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineManager {
	private final List<Line> lines;
	private final Map<String, Line> idLineMap;
	private final List<EndPoint> lineEndpoints;
	private final List<Line> removedLines;

	public LineManager() {
		this.lines = new ArrayList<>();
		this.idLineMap = new HashMap<>();
		this.lineEndpoints = new ArrayList<>();
		this.removedLines = new ArrayList<>();
	}

	public void addLine(Line line) {
		this.lines.add(line);
		this.idLineMap.put(line.getId(), line);
		lineEndpoints.add(line.getInputEndPoint());
		lineEndpoints.add(line.getOutputEndPoint());
	}

	public void removeLine(Line line) {
		lines.remove(line);
		idLineMap.remove(line.getId());
		lineEndpoints.remove(line.getInputEndPoint());
		lineEndpoints.remove(line.getOutputEndPoint());
		line.cutOut();
		this.removedLines.add(line);
	}

	public void addLines(List<Line> lines) {
		for (Line line : lines) {
			addLine(line);
		}
	}

	public void updateOutputs(List<Line> lines) {
		String outputLineId;
		for (Line line : lines) {
			if ((outputLineId = line.getOutputLineId()) != null) {
				line.setOutputLine(idLineMap.get(outputLineId));
			}
		}
	}

	public List<Line> getLines() {
		return lines;
	}

	public void clearLines() {
		lines.clear();
		idLineMap.clear();
		lineEndpoints.clear();
	}

	public List<EndPoint> getLineEndpoints() {
		return lineEndpoints;
	}

	public List<Line> getRemovedLines() {
		return removedLines;
	}

	public void clearRemovedLines() {
		removedLines.clear();
	}

	public Line getLine(String id) {
		return idLineMap.get(id);
	}
}
