package de.schoko.editortestmod.client.export.core;

public interface Transformer<T> {
	Action transform(T data, LogCollector logCollector);

	enum Action {
		CONTINUE,
		INTERRUPT;
	}
}
