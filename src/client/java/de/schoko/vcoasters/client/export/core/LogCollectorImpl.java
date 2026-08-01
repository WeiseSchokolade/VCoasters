package de.schoko.vcoasters.client.export.core;

import java.util.List;
import java.util.function.Consumer;

class LogCollectorImpl implements LogCollector {
	private final List<String> lines;

	LogCollectorImpl(List<String> lines) {
		this.lines = lines;
	}

	@Override
	public void dump(Consumer<String> lineConsumer) {
		lines.forEach(lineConsumer);
	}

	@Override
	public void noticeStage(PipelineStage<?> stage) {
		lines.add("Executing stage " + stage.getName());
	}

	@Override
	public void noticeTransform(TransformerContainer<?> transformer) {
		lines.add("Executing transform " + transformer.name());
	}

	@Override
	public void addErrorMessage(String line) {
		lines.add("An error occurred!");
		lines.add(line);
	}
}
