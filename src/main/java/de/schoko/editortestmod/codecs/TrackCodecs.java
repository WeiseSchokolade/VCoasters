package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.Track;

public enum TrackCodecs {
	;

	public static final Codec<Track> V1 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 1),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V1.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, Track::new));

	public static final Codec<Track> V2 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 2),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V2.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, Track::new));

	public static final Codec<Track> V3 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 3),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V3.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, Track::new));

	public static final Codec<Track> V4 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 4),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V4.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, Track::new));

	public static final Codec<Track> V5 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 5),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V5.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, Track::new));

	public static final Codec<Track> CURRENT = V5;
	public static final int CURRENT_VERSION = 5;
}
