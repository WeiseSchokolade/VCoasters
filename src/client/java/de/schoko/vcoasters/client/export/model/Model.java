package de.schoko.vcoasters.client.export.model;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

public record Model(Map<String, String> textures, List<ModelElement> elements) {
	public record ModelElement(double[] from, double[] to, Map<String, ElementFace> faces, RotationSpecification rotation) {

	}

	public record ElementFace(String texture) {

	}

	public record RotationSpecification(double[] origin, Direction.Axis axis, double angle) {

	}
}
