package de.schoko.vcoasters.client.export.core;

public interface Transformer<T> {
	Action transform(T data, LogCollector logCollector);

	enum Action {
		CONTINUE,
		INTERRUPT;
	}
}
