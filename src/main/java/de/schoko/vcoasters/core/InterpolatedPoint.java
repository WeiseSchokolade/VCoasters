package de.schoko.vcoasters.core;

import org.joml.Vector3f;

public record InterpolatedPoint(Vector3f point, float yaw, float pitch, float roll) implements ValuePoint {

	public InterpolatedPoint(float x, float y, float z, float yaw, float pitch, float roll) {
		this(new Vector3f(x, y, z), yaw, pitch, roll);
	}

	public InterpolatedPoint(ValuePoint valuePoint) {
		this(new Vector3f(valuePoint.x(), valuePoint.y(), valuePoint.z()), valuePoint.yaw(), valuePoint.pitch(), valuePoint.roll());
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

	public static InterpolatedPoint getAverage(ValuePoint valuePoint, ValuePoint a, ValuePoint b) {
		Vector3f delta = new Vector3f(b.posToVector3f()).sub(a.posToVector3f()).normalize();
		double yaw = Math.atan2(delta.z, delta.x) - Math.PI * 0.5;
		double pitch = -Math.asin(delta.y);
		return new InterpolatedPoint(valuePoint.x(), valuePoint.y(), valuePoint.z(), (float) yaw, (float) pitch, valuePoint.roll());
	}

	private static float lerp(float t, float a, float b) {
		return (1 - t) * a + b * t;
	}
}
