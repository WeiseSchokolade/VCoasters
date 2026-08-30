package de.schoko.vcoasters.client.export.stages;

import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record EntitySpecification(String group, UUID uuid, int count) {
	public String inject(String line) {
		int[] uuidInts = UUIDUtil.uuidToIntArray(uuid);
		String snbtUUID = "[I;" + uuidInts[0] + "," + uuidInts[1] + "," + uuidInts[2] + "," + uuidInts[3] + "]";
		return line
			.replace("@{count}", "" + count)
			.replace("@{index}", "" + (count - 1))
			.replace("@{snbt_uuid}", snbtUUID)
			.replace("@{uuid}", uuid.toString());
	}
}
