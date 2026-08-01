package de.schoko.vcoasters.core;

import org.joml.Vector3f;

import java.util.function.Function;

public final class EndPoint implements EditorObject, ValuePoint {
	public static Function<EndPoint, Renderer<EndPoint>> rendererGetter;

	private final Line line;
	private final boolean isOutputEndPoint;
	private final Vector3f pos;
	private float yaw;
	private float pitch;
	private float roll;
	private Renderer<EndPoint> renderer;

	public EndPoint(Line line, boolean isOutputEndPoint, float x, float y, float z, float yaw, float pitch, float roll) {
		this.line = line;
		this.isOutputEndPoint = isOutputEndPoint;
		this.pos = new Vector3f(x, y, z);
		clampPos();
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
	}

	public EndPoint(Line line, boolean isOutputEndPoint, ValuePoint point) {
		this(line, isOutputEndPoint, point.x(), point.y(), point.z(), point.yaw(), point.pitch(), point.roll());
	}

	private void clampPos() {
		this.pos.x = Math.round(pos.x * 1000d) / 1000f;
		this.pos.y = Math.round(pos.y * 1000d) / 1000f;
		this.pos.z = Math.round(pos.z * 1000d) / 1000f;
	}

	public EndPoint getCorrespondingEndpoint() {
		EndPoint point;
		if (isOutputEndPoint) {
			if (line.getOutputLine() == null) return null;
			point = line.getOutputLine().getInputEndPoint();
		} else {
			if (line.getInputLine() == null) return null;
			point = line.getInputLine().getOutputEndPoint();
		}
		return point;
	}

	public void updateCorrespondingEndpoint() {
		EndPoint point = getCorrespondingEndpoint();
		if (point == null) return;
		point.merge(this);
	}

	public boolean equalsCorrespondingEndpoint() {
		EndPoint endpoint = getCorrespondingEndpoint();
		if (endpoint == null) return false;
		return endpoint.pos.equals(this.pos) &&
			endpoint.yaw == this.yaw &&
			endpoint.pitch == this.pitch &&
			endpoint.roll == this.roll;
	}

	public void merge(ValuePoint valuePoint) {
		this.pos.set(valuePoint.x(), valuePoint.y(), valuePoint.z());
		clampPos();
		this.yaw = valuePoint.yaw();
		this.pitch = valuePoint.pitch();
		this.roll = valuePoint.roll();
		markRendererAsDirty();
	}

	@Override
	public String toString() {
		return "EndPoint{" +
			"line=" + line +
			", isOutputEndPoint=" + isOutputEndPoint +
			", pos=" + pos +
			", yaw=" + yaw +
			", pitch=" + pitch +
			", roll=" + roll +
			'}';
	}

	public void setPos(Vector3f pos) {
		setPos(pos.x, pos.y, pos.z);
	}

	public void setPos(double x, double y, double z) {
		this.pos.set(x, y, z);
		clampPos();
		markRendererAsDirty();
	}

	public Vector3f getRotatedViewDirection(float length) {
		return new Vector3f(0f, 0f, length).rotateX(pitch).rotateY(-yaw);
	}

	public Line getLine() {
		return line;
	}

	public Vector3f pos() {
		return pos;
	}

	public Vector3f getPos() {
		return pos;
	}

	public Renderer<EndPoint> getRenderer() {
		if (renderer == null) renderer = rendererGetter.apply(this);
		return renderer;
	}

	public void markRendererAsDirty() {
		if (renderer != null) renderer.setDirty(true);
	}

	@Override
	public float x() {
		return pos.x;
	}

	@Override
	public float y() {
		return pos.y;
	}

	@Override
	public float z() {
		return pos.z;
	}

	@Override
	public float yaw() {
		return yaw;
	}

	@Override
	public float pitch() {
		return pitch;
	}

	@Override
	public float roll() {
		return roll;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
		markRendererAsDirty();
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		markRendererAsDirty();
	}

	public void setRoll(float roll) {
		this.roll = roll;
		markRendererAsDirty();
	}

	public boolean isOutputEndPoint() {
		return isOutputEndPoint;
	}
}