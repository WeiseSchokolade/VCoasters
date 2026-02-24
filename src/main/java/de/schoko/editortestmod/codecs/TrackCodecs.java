package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.CartModel;
import de.schoko.editortestmod.Track;

import java.util.Optional;

public enum TrackCodecs {
	;

	public static final Codec<Track> V1 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 1),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V1.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, (id, dataVersion, exportVersion, trackName, comment, lines) -> new Track(id, dataVersion, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty())));

	public static final Codec<Track> V4 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 4),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V4.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, (id, dataVersion, exportVersion, trackName, comment, lines) -> new Track(id, dataVersion, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty())));

	public static final Codec<Track> V5 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(track -> 5),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		LineCodecs.V5.listOf().fieldOf("lines").forGetter(Track::getLines)
	).apply(instance, (id, dataVersion, exportVersion, trackName, comment, lines) -> new Track(id, dataVersion, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty())));

	public static final Codec<Track> V6 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(Track::getDataVersion),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		Codec.DOUBLE.fieldOf("gravity").forGetter(Track::getGravity),
		Codec.INT.fieldOf("ticks").forGetter(Track::getTicksInHertz),
		LineCodecs.V5.listOf().fieldOf("lines").forGetter(Track::getLines),
		CartModelCodecs.V6.optionalFieldOf("cart_model").forGetter((track) -> Optional.ofNullable(track.getCartModel()))
	).apply(instance, (id, dataVersion, exportVersion, trackName, comment, gravity, ticks, lines, cartModel) -> new Track(id, dataVersion, exportVersion, trackName, comment, gravity, 0, ticks, lines, cartModel)));

	public static final Codec<Track> V7 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(Track::getDataVersion),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		Codec.DOUBLE.fieldOf("gravity").forGetter(Track::getGravity),
		Codec.INT.fieldOf("friction").forGetter(Track::getFriction),
		Codec.INT.fieldOf("ticks").forGetter(Track::getTicksInHertz),
		LineCodecs.V5.listOf().fieldOf("lines").forGetter(Track::getLines),
		CartModelCodecs.V6.optionalFieldOf("cart_model").forGetter((track) -> Optional.ofNullable(track.getCartModel()))
	).apply(instance, Track::new));

	public static final Codec<Track> V8 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Track::getId),
		Codec.INT.fieldOf("data_version").forGetter(Track::getDataVersion),
		Codec.INT.fieldOf("export_version").forGetter(Track::increasedExportVersion),
		Codec.STRING.fieldOf("track_name").forGetter(Track::getTrackName),
		Codec.STRING.fieldOf("comment").forGetter(Track::getTrackComment),
		Codec.DOUBLE.fieldOf("gravity").forGetter(Track::getGravity),
		Codec.INT.fieldOf("friction").forGetter(Track::getFriction),
		Codec.INT.fieldOf("ticks").forGetter(Track::getTicksInHertz),
		LineCodecs.V8.listOf().fieldOf("lines").forGetter(Track::getLines),
		CartModelCodecs.V6.optionalFieldOf("cart_model").forGetter((track) -> Optional.ofNullable(track.getCartModel()))
	).apply(instance, Track::new));

	public static final Codec<Track> CURRENT = V8;
	public static final int CURRENT_VERSION = 8;
}
