package de.schoko.editortestmod.client.export.core;

public record TransformerContainer<T>(String name, Transformer<T> transformer) implements Transformer<T> {
	@Override
	public Action transform(T data, LogCollector logCollector) {
		return transformer.transform(data, logCollector);
	}
}
