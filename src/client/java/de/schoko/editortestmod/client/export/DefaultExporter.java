package de.schoko.editortestmod.client.export;

import com.mojang.serialization.DataResult;
import de.schoko.editortestmod.client.export.core.Exporter;
import de.schoko.editortestmod.client.export.core.PipelineStageBuilder;
import de.schoko.editortestmod.client.export.core.Transformer;
import de.schoko.editortestmod.client.export.stages.EndStageData;
import de.schoko.editortestmod.client.export.stages.InitStageData;
import de.schoko.editortestmod.client.export.stages.StringContainer;
import de.schoko.editortestmod.codecs.TrackCodecs;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class DefaultExporter {
	private static final String RAW_CODE_FOLDER = "/de/schoko/editortestmod/raw_code/";
	private static final String[] RAW_CODE_FILES = {
		"tick",
		"tick_train",
		"tick_entity",
		"hard_reset",
		"reset_trains",
		"load_track_data",
		"spawn_entities",
		"call_on_reach",
		"call_on_halt",
		"interpolate",
		"current/load_interpolation_data",
		"current/leave_line_at_input",
		"current/jump_to_input_line",
		"current/physics/get_physics_accel",
		"current/physics/round_brake_accel",
		"current/physics/round_lift_accel",
		"tbase/tick",
		"tbase/on_halt",
		"tbase/leave_line_at_output",
		"tbase/leave_line_at_input",
		"tbase/jump_to_input_line",
		"tbase/jump_to_output_line",
		"tbase/physics/get_physics_accel",
		"tbase/physics/station_accel",
		"tbase/physics/station_full_stop"
	};

	private DefaultExporter() {

	}

	public static Exporter getExporter() {
		return new Exporter(
			PipelineStageBuilder.get()
				.advanceStage("Load basic code", data -> {
					List<InitStageData.TrackAndLoadedFileData.LoadedFileData> loadedFileData;
					try {
						loadedFileData = loadFileData(RAW_CODE_FOLDER, getDefaultFilePathList());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return new InitStageData.TrackAndLoadedFileData(data.track(), data.trainStartLineIds().size(), data.trainStartLineIds(), loadedFileData, data.majorNamespace(), data.minorNamespace());
				})
				.addTransformer("Add metadata to init", (data, logCollector) -> {
					var file = data.getFile("hard_reset");
					if (file == null) {
						logCollector.addErrorMessage("Hard reset file not found!");
						return Transformer.Action.INTERRUPT;
					}
					file.content().replaceFirst("@\\{segment_amount}", "" + data.track().getTrainMeta().getSegmentAmount());
					file.content().replaceFirst("@\\{data_version}", "" + data.track().getDataVersion());
					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Add track data", (data, logCollector) -> {
					var file = data.getFile("load_track_data");
					if (file == null) {
						logCollector.addErrorMessage("Track data file not found!");
						return Transformer.Action.INTERRUPT;
					}
					data.track().bakeAcceleration();
					DataResult<Tag> result = TrackCodecs.CURRENT_CODEC.encodeStart(NbtOps.INSTANCE, data.track());
					file.content().set("data merge storage track:storage ");
					if (result.result().isPresent()) {
						file.content().append(result.result().get().toString());
					}

					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand trains", (data, logCollector) -> { // ( ! ) Destroys full path information for tbase files
					int trainAmount = data.trainAmount();

					List<InitStageData.TrackAndLoadedFileData.LoadedFileData> newFiles = new ArrayList<>();
					for (var fileData : data.fileDataList()) {
						if (fileData.localPath().startsWith("tbase")) {
							for (int i = 1; i <= trainAmount; i++) {
								String name = "t" + i;
								newFiles.add(
									new InitStageData.TrackAndLoadedFileData.LoadedFileData(
										fileData.localPath().replace("tbase", name),
										fileData.fullPath(),
										new StringContainer(fileData.content().get().replaceAll("tbase", name))
									));
							}
						}
					}
					data.fileDataList().removeIf(file -> file.localPath().startsWith("tbase"));
					newFiles.forEach(data.fileDataList()::add);

					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand train tick function", (data, logCollector) -> {
					var tickFile = data.getFile("tick");
					if (tickFile == null) {
						logCollector.addErrorMessage("Tick file not found!");
						return Transformer.Action.INTERRUPT;
					}

					int trainAmount = data.trainAmount();
					for (int i = 1; i <= trainAmount; i++) {
						String name = "t" + i;
						tickFile.content().appendLine("function base:namespace/" + name + "/tick");
					}

					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand reset_trains function", (data, logCollector) -> {
					var resetFile = data.getFile("reset_trains");
					if (resetFile == null) {
						logCollector.addErrorMessage("Train reset file not found!");
						return Transformer.Action.INTERRUPT;
					}
					int trainAmount = data.trainAmount();
					String content = resetFile.content().get();
					resetFile.content().set("");
					for (int i = 1; i <= trainAmount; i++) {
						String name = "t" + i;
						resetFile.content().append(content.replaceAll("tbase", name).replace("@{train_start_line_id}", data.trainStartLineIds().get(i - 1)));
					}
					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand spawn entities function", (data, logCollector) -> {
					var spawnFile = data.getFile("spawn_entities");
					if (spawnFile == null) {
						logCollector.addErrorMessage("Spawn entities file not found!");
						return Transformer.Action.INTERRUPT;
					}

					String itemModelName = data.track().getTrainMeta().getModelId().toString();

					int trainAmount = data.trainAmount();
					int segmentAmount = data.track().getTrainMeta().getSegmentAmount();
					for (int trainId = 1; trainId <= trainAmount; trainId++) {
						String trainTag = "t" + trainId;
						for (int segmentId = 1; segmentId <= segmentAmount; segmentId++) {
							spawnFile.content().appendLine("summon item_display ~ ~ ~ {teleport_duration:2,item:{id:\"minecraft:paper\",count:1,components:{\"minecraft:item_model\":\"" + itemModelName + "\"}},Tags:[\"train_cart_tag\",\"train_cart_tag." + trainTag + "\",\"train_cart_tag." + ("cart." + segmentId) + "\"]}");
						}
					}

					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand tick entity function", (data, logCollector) -> {
					var tickFile = data.getFile("tick_entity");
					if (tickFile == null) {
						logCollector.addErrorMessage("Tick entity file not found!");
						return Transformer.Action.INTERRUPT;
					}
					int segmentAmount = data.track().getTrainMeta().getSegmentAmount();
					for (int segmentId = 1; segmentId <= segmentAmount; segmentId++) {
						tickFile.content().appendLine("execute if entity @s[tag=train_cart_tag." + ("cart." + segmentId) + "] run return run data modify entity @s {} merge from storage train:storage points[" + (segmentId - 1) + "]");
					}

					return Transformer.Action.CONTINUE;
				})
				.addTransformer("Expand tick train function", (data, logCollector) -> {
					var tickFile = data.getFile("tick_train");
					if (tickFile == null) {
						logCollector.addErrorMessage("Tick train file not found!");
						return Transformer.Action.INTERRUPT;
					}

					String commands = """
scoreboard players remove #dist train_math_score @\\{train_segment_dist}
execute if score #dist train_math_score matches ..-1 run function base:namespace/current/leave_line_at_input
function base:namespace/interpolate
data modify storage train:storage points append from storage train:storage interpolated_point
execute if score #should_calc_total train_math_score matches 1 run function base:namespace/current/physics/get_physics_accel
scoreboard players operation #total_acceleration train_math_score += #acceleration train_math_score

""".replace("@\\{train_segment_dist}", "" + data.track().getTrainMeta().getCarDistance());

					StringBuilder updatesBuilder = new StringBuilder();

					int segmentUpdateAmount = data.track().getTrainMeta().getSegmentAmount() - 1; // - 1 because the first cart is already being calculated
					for (int segmentId = 0; segmentId < segmentUpdateAmount; segmentId++) {
						updatesBuilder.append(commands);
					}
					tickFile.content().replaceFirst("@\\{insert_for_each_cart}", updatesBuilder.toString());

					return Transformer.Action.CONTINUE;
				})
				.advanceStage("Namespace correction", data -> {
					String scoreboardBase = data.majorNamespace() + "." + data.minorNamespace().replace("/", ".");
					return new InitStageData.TrackParsedDataAndNamespaces(data.track(), data.fileDataList(), data.majorNamespace(), data.minorNamespace(),
						data.track().getId(),
						data.majorNamespace() + ":" + data.minorNamespace() + "/train",
						scoreboardBase + ".data",
						scoreboardBase + ".math",
						scoreboardBase
						);
				})
				.addTransformer("Replace values", (data, logCollector) -> {
					data.fileDataList().forEach(file -> {
						file.content().replace("track:storage", data.trackStorageName());
						file.content().replace("train:storage", data.trainStorageName());
						file.content().replace("base:namespace", data.majorNamespace() + ":" + data.minorNamespace());
						file.content().replace("train_data_score", data.trainDataScore());
						file.content().replace("train_math_score", data.trainMathScore());
						file.content().replace("train_cart_tag", data.entityBaseTag());
					});
					return Transformer.Action.CONTINUE;
				})
				.advanceStage("Script load", data -> {
					Map<String, String> contentMap = new HashMap<>();
					String prefix = "data/" + data.majorNamespace() + "/function/" + data.minorNamespace() + "/";
					data.fileDataList().forEach(file -> {
						contentMap.put(prefix + file.localPath() + ".mcfunction", file.content().get());
					});
					return new EndStageData.FileListData(contentMap);
				})
				.build()
		);
	}

	public static List<InitStageData.TrackAndLoadedFileData.LoadedFileData> loadFileData(String baseDirectory, List<String> files) throws IOException {
		List<InitStageData.TrackAndLoadedFileData.LoadedFileData> fileDataList = new ArrayList<>();
		for (String file : files) {
			String filePath = baseDirectory + file;
			try (InputStream inputStream = DefaultExporter.class.getResourceAsStream(filePath + ".vcode")) {
				if (inputStream == null) {
					throw new IllegalArgumentException("Couldn't find file " + filePath);
				}
				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				String fileContent = new String(bufferedInputStream.readAllBytes());
				fileDataList.add(new InitStageData.TrackAndLoadedFileData.LoadedFileData(file, filePath, new StringContainer(fileContent)));
			}
		}
		return fileDataList;
	}

	public static List<String> getDefaultFilePathList() {
		return new ArrayList<>(Arrays.asList(RAW_CODE_FILES));
	}
}
