package de.schoko.editortestmod;

import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public class TrainMeta {
	private Identifier model;
	private float carDistance;
	private int segmentAmount;

	private Vector3f offset;
	private Vector3f pivot;
	private float yawOffset;
	private float pitchOffset;
	private float rollOffset;

	private transient boolean isDirty;

	public TrainMeta(Identifier model, float carDistance, Vector3f offset, Vector3f pivot, float yawOffset, float pitchOffset, float rollOffset, int segmentAmount) {
		this.model = model;
		this.carDistance = carDistance;
		this.segmentAmount = segmentAmount;

		this.offset = offset;
		this.pivot = pivot;
		this.yawOffset = yawOffset;
		this.pitchOffset = pitchOffset;
		this.rollOffset = rollOffset;
	}

	public TrainMeta() {
		this.model = Identifier.fromNamespaceAndPath("minecraft", "target_block");
		this.carDistance = 15000;
		this.offset = new Vector3f();
		this.pivot = new Vector3f();
		this.segmentAmount = 1;
	}

	public void mergeFrom(TrainMeta model) {
		this.model = model.model;
		this.offset.set(model.offset);
		this.pivot.set(model.pivot);
		this.yawOffset = model.yawOffset;
		this.pitchOffset = model.pitchOffset;
		this.rollOffset = model.rollOffset;
		this.segmentAmount = model.segmentAmount;
	}

	public Identifier getModelId() {
		return model;
	}

	public void setModelId(Identifier model) {
		if (!this.model.equals(model)) setDirty(true);
		this.model = model;
	}

	public float getCarDistance() {
		return carDistance;
	}

	public void setCarDistance(float carDistance) {
		if (this.carDistance != carDistance) setDirty(true);
		this.carDistance = carDistance;
	}

	public Vector3f getOffset() {
		return offset;
	}

	public Vector3f getPivot() {
		return pivot;
	}

	public float getYawOffset() {
		return yawOffset;
	}

	public void setYawOffset(float yawOffset) {
		if (this.yawOffset != yawOffset) setDirty(true);
		this.yawOffset = yawOffset;
	}

	public float getPitchOffset() {
		return pitchOffset;
	}

	public void setPitchOffset(float pitchOffset) {
		if (this.pitchOffset != pitchOffset) setDirty(true);
		this.pitchOffset = pitchOffset;
	}

	public float getRollOffset() {
		return rollOffset;
	}

	public void setRollOffset(float rollOffset) {
		if (this.rollOffset != rollOffset) setDirty(true);
		this.rollOffset = rollOffset;
	}

	public int getSegmentAmount() {
		return segmentAmount;
	}

	public void setSegmentAmount(int segmentAmount) {
		if (this.segmentAmount != segmentAmount) setDirty(true);
		this.segmentAmount = segmentAmount;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean dirty) {
		isDirty = dirty;
	}
}
