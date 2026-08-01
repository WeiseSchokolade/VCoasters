package de.schoko.editortestmod.client.export.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public interface LogCollector {
	static LogCollector getNewInstance() {
		return new LogCollectorImpl(new ArrayList<>());
	}

	void dump(Consumer<String> lineConsumer);

	void noticeStage(PipelineStage<?> stage);
	void noticeTransform(TransformerContainer<?> transformer);

	void addErrorMessage(String line);
}
