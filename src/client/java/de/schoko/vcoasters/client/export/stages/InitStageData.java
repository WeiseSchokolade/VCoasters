package de.schoko.vcoasters.client.export.stages;

import de.schoko.vcoasters.Track;

import java.util.List;

public interface InitStageData {
	record TrackOnlyData(Track track, List<String> trainStartLineIds, String majorNamespace, String minorNamespace) {

	}

	record TrackAndLoadedFileData(Track track, int trainAmount, List<String> trainStartLineIds, List<LoadedFileData> fileDataList, String majorNamespace, String minorNamespace) {
		public record LoadedFileData(String localPath, String fullPath, StringContainer content) {

		}

		public LoadedFileData getFile(String localPath) {
			for (LoadedFileData data : fileDataList) {
				if (data.localPath.equals(localPath)) return data;
			}
			return null;
		}
	}

	record TrackEntitySpecificationsAndLoadedFileData(Track track, List<EntityGroupSpecification> specification, int trainAmount, List<String> trainStartLineIds, List<TrackAndLoadedFileData.LoadedFileData> fileDataList, String majorNamespace, String minorNamespace) {
		public TrackAndLoadedFileData.LoadedFileData getFile(String localPath) {
			for (TrackAndLoadedFileData.LoadedFileData data : fileDataList) {
				if (data.localPath.equals(localPath)) return data;
			}
			return null;
		}
	}

	record TrackParsedDataAndNamespaces(Track track, List<TrackAndLoadedFileData.LoadedFileData> fileDataList, String majorNamespace, String minorNamespace, String trackStorageName, String trainStorageName, String trainDataScore, String trainMathScore, String entityBaseTag) {

	}
}
