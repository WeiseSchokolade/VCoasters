package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.TrackLineManager;
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
		Track track = switch (dataVersion) {
			case 1 -> TrackCodecs.V1.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 4 -> TrackCodecs.V4.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 5 -> TrackCodecs.V5.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 6 -> TrackCodecs.V6.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 7 -> TrackCodecs.V7.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 8 -> TrackCodecs.V8.parse(NbtOps.INSTANCE, data).getOrThrow();
			case 10 -> TrackCodecs.V10.parse(NbtOps.INSTANCE, data).getOrThrow();
			default -> throw new IllegalArgumentException("Unknown track data version: " + dataVersion);
		};
		track.setId(identifier.toString());
		return track;
	}

	public static boolean isDataStorageVersionCompatible(int dataStorageVersion) {
		return switch (dataStorageVersion) {
			case 1, 4, 5, 6, 7, 8, 10 -> true;
			default -> false;
		};
	}
}
