package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import de.schoko.editortestmod.Track;

import java.util.Optional;
import java.util.stream.Collectors;

public enum TrackCodecs {
	;

	private static Field<Track, Integer> getVersion(final int dataVersion) {
		return Field.of(Codec.INT.fieldOf("data_version").forGetter((Track _) -> dataVersion));
	}

	public static Codec<Track> getNewCodec(int dataVersion) {
		var v1 = CodecFieldBuilder.<Track>get()
			.append(Field.of(Codec.STRING, "id", Track::getId))
			.append(Field.of(Codec.INT, "export_version", Track::increasedExportVersion))
			.append(Field.of(Codec.STRING, "track_name", Track::getTrackName))
			.append(Field.of(Codec.STRING, "comment", Track::getTrackComment))
			.append(Field.of(LineCodecs.V1.listOf(), "lines", Track::getLines));
		if (dataVersion == 1) return v1.append(getVersion(1)).build((id, exportVersion, trackName, comment, lines, _) -> new Track(id, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty()));
		var v4 = v1.replace5(Field.of(LineCodecs.V4.listOf(), "lines", Track::getLines));
		if (dataVersion == 4) return v4.append(getVersion(4)).build((id, exportVersion, trackName, comment, lines, _) -> new Track(id, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty()));
		var v5 = v4.replace5(Field.of(LineCodecs.V5.listOf(), "lines", Track::getLines));
		if (dataVersion == 5) return v5.append(getVersion(5)).build((id, exportVersion, trackName, comment, lines, _) -> new Track(id, exportVersion, trackName, comment, 1, 0, 20, lines, Optional.empty()));
		var v6 = v5
			.append(Field.of(Codec.DOUBLE, "gravity", Track::getGravity))
			.append(Field.of(Codec.INT, "ticks", Track::getTicksInHertz))
			.append(Field.nullable(CartModelCodecs.V6, "cart_model", Track::getCartModel));
		if (dataVersion == 6) return v6.append(getVersion(6)).build((id, exportVersion, trackName, comment, lines, gravity, ticks, cartModel, _) -> new Track(id, exportVersion, trackName, comment, gravity, 0, ticks, lines, cartModel));
		var v7 = v6
			.append(Field.of(Codec.INT, "friction", Track::getFriction));
		if (dataVersion == 7) return v7.append(getVersion(7)).build((id, exportVersion, trackName, comment, lines, gravity, ticks, cartModel, friction, _) -> new Track(id, exportVersion, trackName, comment, gravity, friction, ticks, lines, cartModel));
		var v8 = v7
			.replace5(Field.of(LineCodecs.V8.listOf(), "lines", Track::getLines));
		if (dataVersion == 8) return v8.append(getVersion(8)).build((id, exportVersion, trackName, comment, lines ,gravity, ticks, cartModel, friction, _) -> new Track(id, exportVersion, trackName, comment, gravity, friction, ticks, lines, cartModel));
		var v10 = v8
			.replace5(Field.of(Codec.unboundedMap(Codec.STRING, LineCodecs.V10), "lines", track ->
				track.getLines().stream().collect(Collectors.toMap(line -> line.getLabel() != null ? line.getLabel() : line.getId(), line -> line))));
		if (dataVersion == 10) return v10.append(getVersion(10)).build((id, exportVersion, trackName, comment, lines ,gravity, ticks, cartModel, friction, _) -> new Track(id, exportVersion, trackName, comment, gravity, friction, ticks, lines.values(), cartModel));

		return null;
	}

	public static final int CURRENT_VERSION = 10;
	public static final Codec<Track> CURRENT_CODEC = getNewCodec(CURRENT_VERSION);
}
