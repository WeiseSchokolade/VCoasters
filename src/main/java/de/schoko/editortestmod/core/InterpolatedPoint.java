package de.schoko.editortestmod.core;

import org.joml.Vector3f;

public record InterpolatedPoint(Vector3f point, float yaw, float pitch, float roll) implements ValuePoint {

	public InterpolatedPoint(float x, float y, float z, float yaw, float pitch, float roll) {
		this(new Vector3f(x, y, z), yaw, pitch, roll);
	}

	@Override
	public float x() {
		return point.x;
	}

	@Override
	public float y() {
		return point.y;
	}

	@Override
	public float z() {
		return point.z();
	}

	public static InterpolatedPoint lerp(float t, ValuePoint a, ValuePoint b) {
		return new InterpolatedPoint(
			lerp(t, a.x(), b.x()),
			lerp(t, a.y(), b.y()),
			lerp(t, a.z(), b.z()),
			lerp(t, a.yaw(), b.yaw()),
			lerp(t, a.pitch(), b.pitch()),
			lerp(t, a.roll(), b.roll())
		);
	}

	private static float lerp(float t, float a, float b) {
		return (1 - t) * a + b * t;
	}
}
