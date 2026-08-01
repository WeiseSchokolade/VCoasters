package de.schoko.vcoasters;

import de.schoko.vcoasters.core.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackManager {
	private List<Track> tracks;
	private Map<String, Track> idTrackMap;

	public TrackManager() {
		tracks = new ArrayList<>();
		idTrackMap = new HashMap<>();
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public Track getTrack(String id) {
		return idTrackMap.get(id);
	}

	public void removeTrack(Track track) {
		this.tracks.remove(track);
		this.idTrackMap.remove(track.getId());
	}

	public void removeTrack(String id) {
		Track removed = this.idTrackMap.remove(id);
		this.tracks.remove(removed);
		removed.getLines().forEach(TrackLineManager::removeLine);
	}

	public void addTrack(Track track) {
		if (idTrackMap.containsKey(track.getId())) {
			removeTrack(track.getId());
		}
		tracks.add(track);
		idTrackMap.put(track.getId(), track);
		TrackLineManager.addLines(track.getLines());
	}

	public void removeLine(Line line) {
		tracks.forEach(track -> {
			track.removeLine(line.getId());
		});
	}

	public void clear() {
		this.idTrackMap.clear();
		this.tracks.clear();
		TrackLineManager.clear();
	}

}
