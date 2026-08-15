package de.schoko.vcoasters.core;

import de.schoko.vcoasters.TrackLineManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class Line implements EditorObject {
	private final String id;
	private final EndPoint a;
	private final EndPoint b;
	private String label;
	private Line outputLine;
	private String outputLineId;
	private Line inputLine;
	private String onReachFunction;
	private String onHaltFunction;
	private double acceleration;
	private boolean accelerationCalculated;
	private LinePhysicsType physicsType;

	private boolean fullStop;

	private final List<EditorComponent> editorComponents;

	public Line(Vector3f a, Vector3f b) {
		this(getNewRandomId(), a, b);
	}

	public Line(String id, Vector3f a, Vector3f b) {
		this(id, new InterpolatedPoint(a, 0, 0, 0), new InterpolatedPoint(b, 0, 0, 0));
	}

	public Line(String id, ValuePoint a, ValuePoint b) {
		this.id = id;
		this.a = new EndPoint(this, false, a);
		this.b = new EndPoint(this, true, b);
		this.editorComponents = new ArrayList<>();
	}

	public static String getNewRandomId() {
		return "Id" + UUID.randomUUID();
	}

	public boolean mergeData(Line line) {
		boolean different = false;

		different |= a.merge(line.a);
		different |= b.merge(line.b);

		if (!this.label.equals(line.getLabel())) {
			this.label = line.getLabel();
			different = true;
		}
		if (!this.onReachFunction.equals(line.onReachFunction)) {
			this.onReachFunction = line.onReachFunction;
			different = true;
		}
		if (!this.onHaltFunction.equals(line.onHaltFunction)) {
			this.onHaltFunction = line.onHaltFunction;
			different = true;
		}
		if (this.physicsType != line.physicsType) {
			this.physicsType = line.physicsType;
			different = true;
		}
		if (this.fullStop != line.fullStop) {
			this.fullStop = line.fullStop;
			different = true;
		}
		if (!Objects.equals(this.outputLineId, line.outputLineId)) {
			TrackLineManager.replaceOutput(this, line.outputLineId);
			different = true;
		}
		return different;
	}

	public Vector3f getDirection(float length) {
		return b.pos().sub(a.pos(), new Vector3f()).normalize(length);
	}

	public double getPitch() {
		return -Math.sin((b.y() - a.y()) / getLength());
	}

	public Vector3f getCenter() {
		return a.pos().add(b.pos(), new Vector3f()).div(2);
	}

	public InterpolatedPoint lerp(float t) {
		return new InterpolatedPoint(
			lerp(t, a.pos().x, b.pos().x()),
			lerp(t, a.pos().y, b.pos().y()),
			lerp(t, a.pos().z, b.pos().z()),
			lerp(t, a.yaw(), b.yaw()),
			lerp(t, a.pitch(), b.pitch()),
			lerp(t, a.roll(), b.roll())
		);
	}

	private double lerp(double t, double a, double b) {
		return t * b + a * (1 - t);
	}

	private float lerp(float t, float a, float b) {
		return t * b + a * (1 - t);
	}

	public void setOutputLine(Line line) {
		if (this.outputLine != null) {
			this.outputLine.inputLine = null;
			this.outputLine.notifyOfDirt();
		}
		this.outputLine = line;
		if (this.outputLine == null) {
			this.outputLineId = null;
			return;
		}
		this.outputLineId = this.outputLine.getId();
		if (this.outputLine.inputLine != null && this.outputLine.inputLine != this) {
			this.outputLine.inputLine.outputLineId = null;
			this.outputLine.inputLine.outputLine = null;
		}
		this.outputLine.inputLine = this;
		notifyOfDirt();
	}

	public void setNewCenter(Vector3f newCenter) {
		Vector3f diff = getCenter().sub(newCenter);
		a.pos().sub(diff);
		b.pos().sub(diff);
		notifyOfDirt();
	}

	public void cutOut() {
		if (outputLine != null) {
			outputLine.inputLine = null;
			if (inputLine != null) {
				inputLine.outputLineId = null;
				inputLine.setOutputLine(outputLine);
			}
		} else {
			if (inputLine != null) {
				inputLine.setOutputLine(null);
			}
		}
		inputLine = null;
		outputLine = null;
		outputLineId = null;
	}

	public EndPoint getInputEndPoint() {
		return a;
	}

	public EndPoint getOutputEndPoint() {
		return b;
	}

	public Line getOutputLine() {
		return outputLine;
	}

	public Line getInputLine() {
		return inputLine;
	}

	@Override
	public String toString() {
		return "Line{" +
			"id='" + id + '\'' +
			'}';
	}

	public String getId() {
		return id;
	}

	public float getLength() {
		return b.pos().distance(a.pos());
	}

	public float getLengthSquared() {
		return b.pos().distanceSquared(a.pos());
	}

	public void notifyOfDirt() {
		DirtContainer component = getComponent(DirtContainer.class);
		if (component != null) component.setDirty(true);
	}

	public String getOutputLineId() {
		return outputLine != null ? outputLine.id : outputLineId;
	}

	public String getInputLineId() {
		return inputLine != null ? inputLine.id : null;
	}

	public void setOutputLineId(String outputLineId) {
		this.outputLineId = outputLineId;
	}

	public void setOnReachFunction(String onReachFunction) {
		if (!Objects.equals(this.onReachFunction, onReachFunction)) notifyOfDirt();
		this.onReachFunction = onReachFunction;
	}

	public String getOnReachFunction() {
		return onReachFunction;
	}

	public void setOnHaltFunction(String onHaltFunction) {
		if (!Objects.equals(this.onHaltFunction, onHaltFunction)) notifyOfDirt();
		this.onHaltFunction = onHaltFunction;
	}

	public String getOnHaltFunction() {
		return onHaltFunction;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
		accelerationCalculated = true;
	}

	public void resetVelocity() {
		this.accelerationCalculated = false;
	}

	public boolean isAccelerationCalculated() {
		return accelerationCalculated;
	}

	public void setAccelerationCalculated(boolean calculated) {
		this.accelerationCalculated = calculated;
	}

	public void setPhysicsType(LinePhysicsType physicsType) {
		if (this.physicsType != physicsType) notifyOfDirt();
		this.physicsType = physicsType;
	}

	public LinePhysicsType getPhysicsType() {
		return physicsType;
	}

	public double horizontalLength() {
		return Math.hypot(b.x() - a.x(), b.z() - a.z());
	}

	public double dY() {
		return b.y() - a.y();
	}

	public boolean isFullStop() {
		return fullStop;
	}

	public void setFullStop(boolean fullStop) {
		if (this.fullStop != fullStop) notifyOfDirt();
		this.fullStop = fullStop;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		if (!Objects.equals(this.label, label)) notifyOfDirt();
		this.label = label;
	}

	public void shift(float dx, float dy, float dz) {
		a.setPos(a.getPos().add(dx, dy, dz, new Vector3f()));
		b.setPos(b.getPos().add(dx, dy, dz, new Vector3f()));
		notifyOfDirt();
	}

	public List<EditorComponent> getEditorComponents() {
		return editorComponents;
	}

	public <T extends EditorComponent> T getComponent(Class<T> clazz) {
		//noinspection unchecked
		return (T) editorComponents.stream().filter(clazz::isInstance).findFirst().orElse(null);
	}

	public void addComponent(EditorComponent component) {
		this.editorComponents.add(component);
	}
}
