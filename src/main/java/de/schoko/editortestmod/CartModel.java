package de.schoko.editortestmod;

import org.joml.Vector3f;

public class CartModel {
	private String model;
	private Vector3f offset;
	private Vector3f pivot;
	private float yawOffset;
	private float pitchOffset;
	private float rollOffset;
	private int segmentAmount;

	public CartModel(String model, Vector3f offset, Vector3f pivot, float yawOffset, float pitchOffset, float rollOffset, int segmentAmount) {
		this.model = model;
		this.offset = offset;
		this.pivot = pivot;
		this.yawOffset = yawOffset;
		this.pitchOffset = pitchOffset;
		this.rollOffset = rollOffset;
		this.segmentAmount = segmentAmount;
	}

	public CartModel() {
		this.model = "";
		this.offset = new Vector3f();
		this.pivot = new Vector3f();
		this.segmentAmount = 1;
	}

	public void mergeFrom(CartModel model) {
		this.model = model.model;
		this.offset.set(model.offset);
		this.pivot.set(model.pivot);
		this.yawOffset = model.yawOffset;
		this.pitchOffset = model.pitchOffset;
		this.rollOffset = model.rollOffset;
		this.segmentAmount = model.segmentAmount;
	}

	public String getModelId() {
		return model;
	}

	public void setModelId(String model) {
		this.model = model;
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
		this.yawOffset = yawOffset;
	}

	public float getPitchOffset() {
		return pitchOffset;
	}

	public void setPitchOffset(float pitchOffset) {
		this.pitchOffset = pitchOffset;
	}

	public float getRollOffset() {
		return rollOffset;
	}

	public void setRollOffset(float rollOffset) {
		this.rollOffset = rollOffset;
	}

	public int getSegmentAmount() {
		return segmentAmount;
	}

	public void setSegmentAmount(int segmentAmount) {
		this.segmentAmount = segmentAmount;
	}
}
