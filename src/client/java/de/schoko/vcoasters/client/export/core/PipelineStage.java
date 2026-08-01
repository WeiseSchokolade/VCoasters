package de.schoko.vcoasters.client.export.core;

import java.util.List;
import java.util.function.Function;

public final class PipelineStage<T> {
	private final String name;
	private final List<TransformerContainer<T>> transformers;
	private ChildContainer<?> childContainer;

	public <U> PipelineStage(String name, List<TransformerContainer<T>> transformers, PipelineStage<U> childStage, Function<Object, U> dataConverter) {
		this.name = name;
		this.transformers = transformers;
		this.childContainer = new ChildContainer<>(childStage, dataConverter);
	}

	public Transformer.Action applyTransformations(Object data, LogCollector logCollector) {
		for (TransformerContainer<T> transformer : transformers) {
			//noinspection unchecked
			if (transformer.transform((T) data, logCollector) == Transformer.Action.INTERRUPT) {
				return Transformer.Action.INTERRUPT;
			}
		}
		return Transformer.Action.CONTINUE;
	}

	public String getName() {
		return name;
	}

	public PipelineStage<?> getChildStage() {
		return childContainer.child;
	}

	public Object convertDataForChild(Object data) {
		return childContainer.dataConverter.apply(data);
	}

	record ChildContainer<U>(PipelineStage<U> child, Function<Object, U> dataConverter) {

	}
}
