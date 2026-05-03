package de.schoko.editortestmod.codecs;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

public record Field<B, T1>(App<RecordCodecBuilder.Mu<B>, T1> codec) {
	public static <B, T1> Field<B, T1> of(App<RecordCodecBuilder.Mu<B>, T1> codec) {
		return new Field<>(codec);
	}

	public static <B, T> Field<B, T> of(Codec<T> codec, String fieldName, Function<B, T> getter) {
		return new Field<>(codec.fieldOf(fieldName).forGetter(getter));
	}

	public static <B, T> Field<B, Optional<T>> nullable(Codec<T> codec, String fieldName, Function<B, T> getter) {
		return new Field<>(codec.optionalFieldOf(fieldName).forGetter(b -> Optional.ofNullable(getter.apply(b))));
	}

	//public record CompatabilityResult(boolean compatible, String note) {}
}
