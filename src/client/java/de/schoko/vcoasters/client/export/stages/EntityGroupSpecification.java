package de.schoko.vcoasters.client.export.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record EntityGroupSpecification(String group, int amount, List<EntitySpecification> entities) {
	public EntityGroupSpecification(String group, int amount) {
		this(group, amount, new ArrayList<>());
		for (int i = 0; i < amount; i++) {
			entities.add(new EntitySpecification(group, UUID.randomUUID(), i + 1));
		}
	}
}
