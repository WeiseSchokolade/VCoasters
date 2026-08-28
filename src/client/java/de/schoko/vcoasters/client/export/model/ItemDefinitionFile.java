package de.schoko.vcoasters.client.export.model;

import java.util.List;

public record ItemDefinitionFile(AbstractModelDefinition model) {
	public interface AbstractModelDefinition {

	}

	public record CompositeDefinition(String type, List<AbstractModelDefinition> models, ItemDefinitionTransformation transformation) implements AbstractModelDefinition {

	}

	public record ItemDefinition(String type, String model, ItemDefinitionTransformation transformation) implements AbstractModelDefinition {

	}


}
