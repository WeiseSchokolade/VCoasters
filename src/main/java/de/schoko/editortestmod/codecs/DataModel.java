package de.schoko.editortestmod.codecs;

public sealed interface DataModel {
	<T> DataModel append(T newField);

	record Field0() implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field1<>(newField);
		}
	}
	record Field1<F1>(F1 field1) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field2<>(field1, newField);
		}
	}
	record Field2<F1, F2>(F1 field1, F2 field2) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field3<>(field1, field2, newField);
		}
	}
	record Field3<F1, F2, F3>(F1 field1, F2 field2, F3 field3) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field4<>(field1, field2, field3, newField);
		}

	}
	record Field4<F1, F2, F3, F4>(F1 field1, F2 field2, F3 field3, F4 field4) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field5<>(field1, field2, field3, field4, newField);
		}
	}
	record Field5<F1, F2, F3, F4, F5>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field6<>(field1, field2, field3, field4, field5, newField);
		}
	}
	record Field6<F1, F2, F3, F4, F5, F6>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field7<>(field1, field2, field3, field4, field5, field6, newField);
		}
	}
	record Field7<F1, F2, F3, F4, F5, F6, F7>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field8<>(field1, field2, field3, field4, field5, field6, field7, newField);
		}
	}
	record Field8<F1, F2, F3, F4, F5, F6, F7, F8>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field9<>(field1, field2, field3, field4, field5, field6, field7, field8, newField);
		}
	}
	record Field9<F1, F2, F3, F4, F5, F6, F7, F8, F9>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field10<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, newField);
		}
	}
	record Field10<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field11<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, newField);
		}
	}
	record Field11<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field12<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, newField);
		}
	}
	record Field12<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11, F12 field12) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field13<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, newField);
		}
	}
	record Field13<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11, F12 field12, F13 field13) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field14<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, newField);
		}
	}
	record Field14<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11, F12 field12, F13 field13, F14 field14) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field15<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, newField);
		}
	}
	record Field15<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11, F12 field12, F13 field13, F14 field14, F15 field15) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			return new Field16<>(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, newField);
		}
	}
	record Field16<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16>(F1 field1, F2 field2, F3 field3, F4 field4, F5 field5, F6 field6, F7 field7, F8 field8, F9 field9, F10 field10, F11 field11, F12 field12, F13 field13, F14 field14, F15 field15, F16 field16) implements DataModel {
		@Override
		public <T> DataModel append(T newField) {
			throw new UnsupportedOperationException("Max size has been reached!");
		}
	}
}
