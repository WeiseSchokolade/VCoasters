package de.schoko.vcoasters.client.export.core;

import de.schoko.vcoasters.client.export.stages.InitStageData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PipelineStageBuilder<T> {
	private final String name;
	private final List<TransformerContainer<T>> transformers;
	private final PipelineStageBuilder<?> parentBuilder;
	private final Function<Object, T> fromParentDataConverter;

	private PipelineStageBuilder(String name, PipelineStageBuilder<?> parentBuilder, Function<Object, T> fromParentDataConverter) {
		this.name = name;
		transformers = new ArrayList<>();
		this.parentBuilder = parentBuilder;
		this.fromParentDataConverter = fromParentDataConverter;
	}

	public static PipelineStageBuilder<InitStageData.TrackOnlyData> get() {
		return new PipelineStageBuilder<>("Init", null, null);
	}

	public PipelineStageBuilder<T> addTransformer(String name, Transformer<T> transformer) {
		transformers.add(new TransformerContainer<>(name, transformer));
		return this;
	}

	public <U> PipelineStageBuilder<U> advanceStage(String name, Function<T, U> dataConverter) {
		//noinspection unchecked
		return new PipelineStageBuilder<>(name, this, (Object o) -> dataConverter.apply((T) o));
	}

	private <U> PipelineStage<InitStageData.TrackOnlyData> build(PipelineStage<U> childStage, PipelineStageBuilder<U> childStageBuilder) {
		PipelineStage<T> stage = new PipelineStage<>(this.name, this.transformers, childStage, childStageBuilder.fromParentDataConverter);
		if (parentBuilder != null) {
			return parentBuilder.build(stage, this);
		} else {
			try {
				//noinspection unchecked
				return (PipelineStage<InitStageData.TrackOnlyData>) stage;
			} catch (ClassCastException e) { // Somehow, an instance not based on InitStageData.TrackOnlyData was created
				throw e;
			}
		}
	}

	public PipelineStage<InitStageData.TrackOnlyData> build() {
		if (parentBuilder == null) throw new IllegalStateException("Init stage cannot be output stage!");
		PipelineStage<T> stage = new PipelineStage<>(this.name, this.transformers, null, null);
		return parentBuilder.build(stage, this);
	}
}
