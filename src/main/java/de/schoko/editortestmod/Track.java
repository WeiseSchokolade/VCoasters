package de.schoko.editortestmod;


import de.schoko.editortestmod.codecs.TrackCodecs;
import de.schoko.editortestmod.core.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Track {
	private int exportVersion;

	private String id;
	private String trackName;
	private String trackComment;
	private final List<Line> lines;

	private double gravityInBlocksPerSecondSquared;
	private int friction;
	private int ticksPerSecondInHertz;

	private CartModel model;

	private transient List<Line> removedLines;
	private transient boolean dirty;

	public Track(String id, int loadingVersion, int exportVersion, String trackName, String trackComment, double gravity, int friction, int ticksInHertz, List<Line> lines, Optional<CartModel> model) {
		this.id = id;
		this.exportVersion = exportVersion;
		this.trackName = trackName;
		this.trackComment = trackComment;
		this.gravityInBlocksPerSecondSquared = gravity;
		this.friction = friction;
		this.ticksPerSecondInHertz = ticksInHertz;
		this.lines = new ArrayList<>(lines);
		this.removedLines = new ArrayList<>();
		this.model = model.orElse(new CartModel());
	}

	public Track(String id) {
		this(id, TrackCodecs.CURRENT_VERSION, 0, id, "", 0.5, 0, 20, List.of(), Optional.empty());
	}

	public void mergeMetaFrom(Track track) {
		this.exportVersion = track.exportVersion;
		this.trackName = track.trackName;
		this.trackComment = track.trackComment;
		this.gravityInBlocksPerSecondSquared = track.gravityInBlocksPerSecondSquared;
		this.friction = track.getFriction();
		this.ticksPerSecondInHertz = track.ticksPerSecondInHertz;
		this.model.mergeFrom(track.model);
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
		return exportVersion + 1;
	}

	public int getExportVersion() {
		return exportVersion;
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

	public Line getLine(String id) {
		for (Line line : lines) {
			if (line.getId().equals(id)) return line;
		}
		return null;
	}

	public void removeLine(String id) {
		this.lines.removeIf(line -> {
			if (line.getId().equals(id)) {
				this.removedLines.add(line);
				return true;
			}
			return false;
		});
	}

	public List<Line> getRemovedLines() {
		return removedLines;
	}

	public void setTrackComment(String trackComment) {
		if (!Objects.equals(this.trackComment, trackComment)) setDirty(true);
		this.trackComment = trackComment;
	}

	public void setTrackName(String trackName) {
		if (!Objects.equals(this.trackName, trackName)) setDirty(true);
		this.trackName = trackName;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public double getGravity() {
		return gravityInBlocksPerSecondSquared;
	}

	public void setGravity(double gravityInBlocksPerSecondSquared) {
		if (gravityInBlocksPerSecondSquared != this.gravityInBlocksPerSecondSquared) setDirty(true);
		this.gravityInBlocksPerSecondSquared = gravityInBlocksPerSecondSquared;
	}

	public int getTicksInHertz() {
		return ticksPerSecondInHertz;
	}

	public void setTicksInHertz(int tickInHertz) {
		if (tickInHertz != this.ticksPerSecondInHertz) setDirty(true);
		this.ticksPerSecondInHertz = tickInHertz;
	}

	public CartModel getCartModel() {
		return model;
	}

	public int getFriction() {
		return friction;
	}

	public void setFriction(int friction) {
		if (this.friction != friction) setDirty(true);
		this.friction = friction;
	}
}
