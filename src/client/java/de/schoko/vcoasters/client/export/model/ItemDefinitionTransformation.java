package de.schoko.vcoasters.client.export.model;

import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public record ItemDefinitionTransformation(double[] translation, double[] left_rotation, double[] scale, double[] right_rotation) {
	public ItemDefinitionTransformation(Vector3f translation, Quaterniondc leftRotation, Vector3f scale, Quaterniondc rightRotation) {
		this(
			new double[] {translation.x, translation.y, translation.z},
			new double[] {leftRotation.x(), leftRotation.y(), leftRotation.z(), leftRotation.w()},
			new double[] {scale.x, scale.y, scale.z},
			new double[] {rightRotation.x(), rightRotation.y(), rightRotation.z(), rightRotation.w()}
		);
	}
}
