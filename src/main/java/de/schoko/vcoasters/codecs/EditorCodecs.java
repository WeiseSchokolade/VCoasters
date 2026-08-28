package de.schoko.vcoasters.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.vcoasters.Track;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import java.util.Optional;

public class EditorCodecs {
	public static final Codec<Vector3f> vector3fCodec = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.fieldOf("x").forGetter(Vector3f::x),
		Codec.FLOAT.fieldOf("y").forGetter(Vector3f::y),
		Codec.FLOAT.fieldOf("z").forGetter(Vector3f::z)
		).apply(instance, Vector3f::new));

	public static Optional<Integer> emptyOptionalIfZero(int value) {
		if (value == 0) return Optional.empty();
		else return Optional.of(value);
	}

	public static Optional<Float> emptyOptionalIfZero(float value) {
		if (value == 0) return Optional.empty();
		else return Optional.of(value);
	}


	public static Track loadTrack(CompoundTag data, Identifier identifier, int dataVersion) {
		Codec<Track> codec;
		if (dataVersion == TrackCodecs.CURRENT_VERSION) {
			codec = TrackCodecs.CURRENT_CODEC;
		} else {
			codec = TrackCodecs.getNewCodec(dataVersion);
			if (codec == null) throw new IllegalArgumentException("Unknown track data version: " + dataVersion);
		}

		Track track = codec.parse(NbtOps.INSTANCE, data).getOrThrow();
		track.setId(identifier.toString());
		return track;
	}

	public static boolean isDataStorageVersionCompatible(int dataStorageVersion) {
		return switch (dataStorageVersion) {
			case 1, 4, 5, 6, 7, 8, 10, 11, 12 -> true;
			default -> false;
		};
	}
}
