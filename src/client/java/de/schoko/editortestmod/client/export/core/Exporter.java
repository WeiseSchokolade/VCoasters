package de.schoko.editortestmod.client.export.core;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.export.stages.EndStageData;
import de.schoko.editortestmod.client.export.stages.InitStageData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Exporter {
	private final PipelineStage<InitStageData.TrackOnlyData> initStage;

	public Exporter(PipelineStage<InitStageData.TrackOnlyData> initStage) {
		this.initStage = initStage;
	}

	private EndStageData.FileListData runPipeline(Track track, List<String> trainStartLineIds, String majorNamespace, String minorNamespace) {
		PipelineStage<?> stage = initStage;
		Object data = new InitStageData.TrackOnlyData(track, trainStartLineIds, majorNamespace, minorNamespace);
		LogCollector logCollector = LogCollector.getNewInstance();
		boolean interrupted = false;
		while (stage != null) {
			logCollector.noticeStage(stage);
			if (stage.applyTransformations(data, logCollector) == Transformer.Action.INTERRUPT) {
				interrupted = true;
				break;
			}
			if (data instanceof EndStageData.FileListData) break;
			data = stage.convertDataForChild(data);
			stage = stage.getChildStage();
		}

		if (interrupted) {
			logCollector.dump(System.out::println);
			throw new IllegalStateException("An error occurred while running the pipeline!");
		} else {
			if (data instanceof EndStageData.FileListData fileListData) {
				return fileListData;
			}
		}
		logCollector.dump(System.out::println);
		throw new IllegalStateException("Used export pipeline is incomplete!");
	}

	public void exportToZip(Track track, List<String> trainStartLineIds, String majorNamespace, String minorNamespace, File destination) throws IOException {
		EndStageData.FileListData data = runPipeline(track, trainStartLineIds, majorNamespace, minorNamespace);
		buildZip(data, destination);
	}

	private void buildZip(EndStageData.FileListData data, File destination) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
		for (var fileDescription : data.fileDataMap().entrySet()) {
			out.putNextEntry(new ZipEntry(fileDescription.getKey()));
			out.write(fileDescription.getValue().getBytes(StandardCharsets.UTF_8));
		}
		out.close();
	}
}
