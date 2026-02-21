package de.schoko.editortestmod;


import de.schoko.editortestmod.codecs.TrackCodecs;
import de.schoko.editortestmod.core.Line;

import java.util.ArrayList;
import java.util.List;

public class Track {
	private int exportVersion;

	private String id;
	private String trackName;
	private String trackComment;
	private List<Line> lines;

	public Track(String id, int loadingVersion, int exportVersion, String trackName, String trackComment, List<Line> lines) {
		this.id = id;
		this.exportVersion = exportVersion;
		this.trackName = trackName;
		this.trackComment = trackComment;
		this.lines = new ArrayList<>(lines);
	}

	public Track(String trackName) {
		this.exportVersion = 0;
		this.trackName = trackName;
		this.trackComment = "";
		this.lines = new ArrayList<>();
	}

	public void setAcceleration(double gravityInBlocksPerSecondSquared, double tickDurationInSeconds) {
		resetAcceleration();
		for (Line line : lines) {
			double pitch = -Math.atan2(line.dY(), line.horizontalLength());
			if (Double.isFinite(pitch)) line.setAcceleration(gravityInBlocksPerSecondSquared * Math.sin(pitch) * tickDurationInSeconds);
			else line.setAcceleration(gravityInBlocksPerSecondSquared * tickDurationInSeconds);
		}

	}

	public void resetAcceleration() {
		lines.forEach(Line::resetVelocity);
	}

	public List<Line> getLines() {
		return lines;
	}

	public int getDataVersion() {
		return TrackCodecs.CURRENT_VERSION;
	}

	public int increasedExportVersion() {
		return ++exportVersion;
	}

	public String getTrackName() {
		return trackName;
	}

	public String getTrackComment() {
		return trackComment;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void removeLine(String id) {
		this.lines.removeIf(line -> line.getId().equals(id));
	}
}
