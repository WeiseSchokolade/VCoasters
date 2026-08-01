package de.schoko.vcoasters.codecs;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CodecFieldBuilder<B, C> {
	// TODO: Add missing cases

	BiFunction<RecordCodecBuilder.Instance<B>, C, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction();

	default Codec<B> build(C constructor) {
		return RecordCodecBuilder.create(instance -> getProductFunction().apply(instance, constructor));
	}

	static <B> BuilderEmpty<B> get() {
		return new BuilderEmpty<>();
	}

	record BuilderEmpty<B>() implements CodecFieldBuilder<B, Supplier<B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Supplier<B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			throw new UnsupportedOperationException();
		}

		public <N> Builder1<B, N> append(Field<B, N> appended) {
			return new Builder1<>(appended);
		}
	}

	record Builder1<B, T1>(Field<B, T1> t1) implements CodecFieldBuilder<B, Function<T1, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function<T1, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P1<>(t1.codec())::apply;
		}

		public <N> Builder2<B, T1, N> append(Field<B, N> appended) {
			return new Builder2<>(t1, appended);
		}
	}

	record Builder2<B, T1, T2>(Field<B, T1> t1,
	                           Field<B, T2> t2) implements CodecFieldBuilder<B, BiFunction<T1, T2, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, BiFunction<T1, T2, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P2<>(t1.codec(), t2.codec())::apply;
		}

		public <N> Builder3<B, T1, T2, N> append(Field<B, N> appended) {
			return new Builder3<>(t1, t2, appended);
		}
	}

	record Builder3<B, T1, T2, T3>(Field<B, T1> t1,
	                               Field<B, T2> t2,
	                               Field<B, T3> t3) implements CodecFieldBuilder<B, Function3<T1, T2, T3, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function3<T1, T2, T3, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P3<>(t1.codec(), t2.codec(), t3.codec())::apply;
		}

		public <N> Builder4<B, T1, T2, T3, N> append(Field<B, N> appended) {
			return new Builder4<>(t1, t2, t3, appended);
		}
	}

	record Builder4<B, T1, T2, T3, T4>(Field<B, T1> t1,
	                                   Field<B, T2> t2,
	                                   Field<B, T3> t3,
	                                   Field<B, T4> t4) implements CodecFieldBuilder<B, Function4<T1, T2, T3, T4, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function4<T1, T2, T3, T4, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P4<>(t1.codec(), t2.codec(), t3.codec(), t4.codec())::apply;
		}

		public <N> Builder5<B, T1, T2, T3, T4, N> append(Field<B, N> appended) {
			return new Builder5<>(t1, t2, t3, t4, appended);
		}
	}

	record Builder5<B, T1, T2, T3, T4, T5>(Field<B, T1> t1,
	                                       Field<B, T2> t2,
	                                       Field<B, T3> t3,
	                                       Field<B, T4> t4,
	                                       Field<B, T5> t5) implements CodecFieldBuilder<B, Function5<T1, T2, T3, T4, T5, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function5<T1, T2, T3, T4, T5, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P5<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec())::apply;
		}

		public <N> Builder6<B, T1, T2, T3, T4, T5, N> append(Field<B, N> appended) {
			return new Builder6<>(t1, t2, t3, t4, t5, appended);
		}

		public <R> Builder5<B, T1, T2, T3, T4, T5> identity(Field<B, R> appended) {
			return new Builder5<>(t1, t2, t3, t4, t5);
		}

		public <R> Builder5<B, R, T2, T3, T4, T5> replace1(Field<B, R> appended) {
			return new Builder5<>(appended, t2, t3, t4, t5);
		}

		public <R> Builder5<B, T1, R, T3, T4, T5> replace2(Field<B, R> appended) {
			return new Builder5<>(t1, appended, t3, t4, t5);
		}

		public <R> Builder5<B, T1, T2, R, T4, T5> replace3(Field<B, R> appended) {
			return new Builder5<>(t1, t2, appended, t4, t5);
		}

		public <R> Builder5<B, T1, T2, T3, R, T5> replace4(Field<B, R> appended) {
			return new Builder5<>(t1, t2, t3, appended, t5);
		}

		public <R> Builder5<B, T1, T2, T3, T4, R> replace5(Field<B, R> appended) {
			return new Builder5<>(t1, t2, t3, t4, appended);
		}
	}

	record Builder6<B, T1, T2, T3, T4, T5, T6>(Field<B, T1> t1,
	                                           Field<B, T2> t2,
	                                           Field<B, T3> t3,
	                                           Field<B, T4> t4,
	                                           Field<B, T5> t5,
	                                           Field<B, T6> t6) implements CodecFieldBuilder<B, Function6<T1, T2, T3, T4, T5, T6, B>> {

		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function6<T1, T2, T3, T4, T5, T6, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P6<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec())::apply;
		}

		public <N> Builder7<B, T1, T2, T3, T4, T5, T6, N> append(Field<B, N> appended) {
			return new Builder7<>(t1, t2, t3, t4, t5, t6, appended);
		}
	}

	record Builder7<B, T1, T2, T3, T4, T5, T6, T7>(Field<B, T1> t1,
	                                               Field<B, T2> t2,
	                                               Field<B, T3> t3,
	                                               Field<B, T4> t4,
	                                               Field<B, T5> t5,
	                                               Field<B, T6> t6,
	                                               Field<B, T7> t7) implements CodecFieldBuilder<B, Function7<T1, T2, T3, T4, T5, T6, T7, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function7<T1, T2, T3, T4, T5, T6, T7, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P7<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec(), t7.codec())::apply;
		}

		public <N> Builder8<B, T1, T2, T3, T4, T5, T6, T7, N> append(Field<B, N> appended) {
			return new Builder8<>(t1, t2, t3, t4, t5, t6, t7, appended);
		}
	}

	record Builder8<B, T1, T2, T3, T4, T5, T6, T7, T8>(Field<B, T1> t1,
	                                                   Field<B, T2> t2,
	                                                   Field<B, T3> t3,
	                                                   Field<B, T4> t4,
	                                                   Field<B, T5> t5,
	                                                   Field<B, T6> t6,
	                                                   Field<B, T7> t7,
	                                                   Field<B, T8> t8) implements CodecFieldBuilder<B, Function8<T1, T2, T3, T4, T5, T6, T7, T8, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function8<T1, T2, T3, T4, T5, T6, T7, T8, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P8<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec(), t7.codec(), t8.codec())::apply;
		}

		public <N> Builder9<B, T1, T2, T3, T4, T5, T6, T7, T8, N> append(Field<B, N> appended) {
			return new Builder9<>(t1, t2, t3, t4, t5, t6, t7, t8, appended);
		}

		public <R> Builder8<B, T1, T2, T3, T4, T5, T6, T7, T8> identity(Field<B, R> replacement) {
			return new Builder8<>(t1, t2, t3, t4, t5, t6, t7, t8);
		}

		public <R> Builder8<B, R, T2, T3, T4, T5, T6, T7, T8> replace1(Field<B, R> r) {
			return new Builder8<>(r, t2, t3, t4, t5, t6, t7, t8);
		}

		public <R> Builder8<B, T1, R, T3, T4, T5, T6, T7, T8> replace2(Field<B, R> r) {
			return new Builder8<>(t1, r, t3, t4, t5, t6, t7, t8);
		}

		public <R> Builder8<B, T1, T2, R, T4, T5, T6, T7, T8> replace3(Field<B, R> r) {
			return new Builder8<>(t1, t2, r, t4, t5, t6, t7, t8);
		}

		public <R> Builder8<B, T1, T2, T3, R, T5, T6, T7, T8> replace4(Field<B, R> r) {
			return new Builder8<>(t1, t2, t3, r, t5, t6, t7, t8);
		}

		public <R> Builder8<B, T1, T2, T3, T4, R, T6, T7, T8> replace5(Field<B, R> r) {
			return new Builder8<>(t1, t2, t3, t4, r, t6, t7, t8);
		}

		public <R> Builder8<B, T1, T2, T3, T4, T5, R, T7, T8> replace6(Field<B, R> r) {
			return new Builder8<>(t1, t2, t3, t4, t5, r, t7, t8);
		}

		public <R> Builder8<B, T1, T2, T3, T4, T5, T6, R, T8> replace7(Field<B, R> r) {
			return new Builder8<>(t1, t2, t3, t4, t5, t6, r, t8);
		}

		public <R> Builder8<B, T1, T2, T3, T4, T5, T6, T7, R> replace8(Field<B, R> r) {
			return new Builder8<>(t1, t2, t3, t4, t5, t6, t7, r);
		}

	}

	record Builder9<B, T1, T2, T3, T4, T5, T6, T7, T8, T9>(Field<B, T1> t1,
	                                                       Field<B, T2> t2,
	                                                       Field<B, T3> t3,
	                                                       Field<B, T4> t4,
	                                                       Field<B, T5> t5,
	                                                       Field<B, T6> t6,
	                                                       Field<B, T7> t7,
	                                                       Field<B, T8> t8,
	                                                       Field<B, T9> t9) implements CodecFieldBuilder<B, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P9<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec(), t7.codec(), t8.codec(), t9.codec())::apply;
		}

		public <N> Builder10<B, T1, T2, T3, T4, T5, T6, T7, T8, T9, N> append(Field<B, N> appended) {
			return new Builder10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, appended);
		}

		public <R> Builder9<B, T1, T2, T3, T4, T5, T6, T7, T8, T9> identity(Field<B, R> replacement) {
			return new Builder9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);
		}

		public <R> Builder9<B, R, T2, T3, T4, T5, T6, T7, T8, T9> replace1(Field<B, R> r) {
			return new Builder9<>(r, t2, t3, t4, t5, t6, t7, t8, t9);
		}

		public <R> Builder9<B, T1, R, T3, T4, T5, T6, T7, T8, T9> replace2(Field<B, R> r) {
			return new Builder9<>(t1, r, t3, t4, t5, t6, t7, t8, t9);
		}

		public <R> Builder9<B, T1, T2, R, T4, T5, T6, T7, T8, T9> replace3(Field<B, R> r) {
			return new Builder9<>(t1, t2, r, t4, t5, t6, t7, t8, t9);
		}

		public <R> Builder9<B, T1, T2, T3, R, T5, T6, T7, T8, T9> replace4(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, r, t5, t6, t7, t8, t9);
		}

		public <R> Builder9<B, T1, T2, T3, T4, R, T6, T7, T8, T9> replace5(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, t4, r, t6, t7, t8, t9);
		}

		public <R> Builder9<B, T1, T2, T3, T4, T5, R, T7, T8, T9> replace6(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, t4, t5, r, t7, t8, t9);
		}

		public <R> Builder9<B, T1, T2, T3, T4, T5, T6, R, T8, T9> replace7(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, t4, t5, t6, r, t8, t9);
		}

		public <R> Builder9<B, T1, T2, T3, T4, T5, T6, T7, R, T9> replace8(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, t4, t5, t6, t7, r, t9);
		}

		public <R> Builder9<B, T1, T2, T3, T4, T5, T6, T7, T8, R> replace9(Field<B, R> r) {
			return new Builder9<>(t1, t2, t3, t4, t5, t6, t7, t8, r);
		}
	}

	record Builder10<B, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(Field<B, T1> t1,
	                                                             Field<B, T2> t2,
	                                                             Field<B, T3> t3,
	                                                             Field<B, T4> t4,
	                                                             Field<B, T5> t5,
	                                                             Field<B, T6> t6,
	                                                             Field<B, T7> t7,
	                                                             Field<B, T8> t8,
	                                                             Field<B, T9> t9,
	                                                             Field<B, T10> t10) implements CodecFieldBuilder<B, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P10<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec(), t7.codec(), t8.codec(), t9.codec(), t10.codec())::apply;
		}

		public <N> Builder11<B, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, N> append(Field<B, N> appended) {
			return new Builder11<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, appended);
		}
	}

	record Builder11<B, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(Field<B, T1> t1,
	                                                                  Field<B, T2> t2,
	                                                                  Field<B, T3> t3,
	                                                                  Field<B, T4> t4,
	                                                                  Field<B, T5> t5,
	                                                                  Field<B, T6> t6,
	                                                                  Field<B, T7> t7,
	                                                                  Field<B, T8> t8,
	                                                                  Field<B, T9> t9,
	                                                                  Field<B, T10> t10,
	                                                                  Field<B, T11> t11) implements CodecFieldBuilder<B, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, B>> {
		@Override
		public BiFunction<RecordCodecBuilder.Instance<B>, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, B>, App<RecordCodecBuilder.Mu<B>, B>> getProductFunction() {
			return new Products.P11<>(t1.codec(), t2.codec(), t3.codec(), t4.codec(), t5.codec(), t6.codec(), t7.codec(), t8.codec(), t9.codec(), t10.codec(), t11.codec())::apply;
		}

	}
}