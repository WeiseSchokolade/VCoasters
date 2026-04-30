package de.schoko.editortestmod.codecs;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class CodecBuilderPattern<T> {
	private List<Version<T>> versions;

	private int currentVersion;
	private List<Transform> currentTransforms;

	public CodecBuilderPattern() {
		currentVersion = 0;
		versions = new ArrayList<>();
		currentTransforms = new ArrayList<>();
	}

	public void constructor(Constructor<T> constructor) {
		versions.add(new Version<>(currentVersion, constructor, List.copyOf(currentTransforms)));
		currentTransforms.clear();
	}

	public void version(int newVersion) {
		this.currentVersion = newVersion;
	}

	public Codec<T> build(int requestedVersion) {
		List<Transform> applicableTransforms = new ArrayList<>();
		Constructor<T> applicableConstructor = null;
		for (Version<T> version : versions) {
			if (version.number > requestedVersion) {
				break;
			}
			applicableTransforms.addAll(version.transforms());
			applicableConstructor = version.constructor();
		}
		if (applicableConstructor == null) throw new IllegalArgumentException("No valid codec could be built!");

		DataModel dataModel = new DataModel.Field0();
		for (Transform transform : applicableTransforms) {
			dataModel = transform.apply(dataModel);
		}


		return build(dataModel, applicableConstructor);
	}

	private <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Codec<T> build(DataModel dataModel, Constructor<T> applicableConstructor) {

//		if (dataModel instanceof DataModel.Field0 model && applicableConstructor instanceof Constructor.Arg0<T> constructor) return null;
//		if (dataModel instanceof DataModel.Field1 && applicableConstructor instanceof Constructor.Arg1<T1, T> constructor) {
//			DataModel.Field1<T1> model = (DataModel.Field1<T1>) dataModel;
//			return null;
//		}
		return null;
	}

	record Field<T, V>(String key, RecordCodecBuilder<T, V> handler) {

	}

	record Version<T>(int number, Constructor<T> constructor, List<Transform> transforms) {

	}

	sealed interface Transform permits AddFieldTransform, MoveFieldTransform {
		DataModel apply(DataModel dataModel);
	}

	record AddFieldTransform<T, V>(String key, RecordCodecBuilder<T, V> builder) implements Transform {
		@Override
		public DataModel apply(DataModel dataModel) {
			return dataModel.append(builder);
		}
	}

	record MoveFieldTransform(String key) implements Transform {
		@Override
		public DataModel apply(DataModel dataModel) {
			return null;
		}
	}

	public sealed interface Constructor<R> {
		non-sealed interface Arg0<R> extends Constructor<R> {
			R construct();
		}
		non-sealed interface Arg1<T1, R> extends Constructor<R> {
			R construct(T1 arg1);
		}
		non-sealed interface Arg2<T1, T2, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2);
		}
		non-sealed interface Arg3<T1, T2, T3, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3);
		}
		non-sealed interface Arg4<T1, T2, T3, T4, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
		}
		non-sealed interface Arg5<T1, T2, T3, T4, T5, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5);
		}
		non-sealed interface Arg6<T1, T2, T3, T4, T5, T6, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6);
		}
		non-sealed interface Arg7<T1, T2, T3, T4, T5, T6, T7, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7);
		}
		non-sealed interface Arg8<T1, T2, T3, T4, T5, T6, T7, T8, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8);
		}
		non-sealed interface Arg9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9);
		}
		non-sealed interface Arg10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10);
		}
		non-sealed interface Arg11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11);
		}
		non-sealed interface Arg12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11, T12 arg12);
		}
		non-sealed interface Arg13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11, T12 arg12, T13 arg13);
		}
		non-sealed interface Arg14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11, T12 arg12, T13 arg13, T14 arg14);
		}
		non-sealed interface Arg15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11, T12 arg12, T13 arg13, T14 arg14, T15 arg15);
		}
		non-sealed interface Arg16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> extends Constructor<R> {
			R construct(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10, T11 arg11, T12 arg12, T13 arg13, T14 arg14, T15 arg15, T16 arg16);
		}
	}
}
