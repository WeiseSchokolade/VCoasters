package de.schoko.vcoasters.client.export.stages;

import java.util.Map;

public interface EndStageData {
	record FileListData(Map<String, String> fileDataMap) {

	}
}
