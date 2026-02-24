package de.schoko.editortestmod.core;

public enum LinePhysicsType {
	REGULAR(0),
	LIFT(50),
	BRAKE(500),
	STATION(30);

	private int accelerationForce;

	LinePhysicsType(int accelerationForce) {
		this.accelerationForce = accelerationForce;
	}

	public int getAccelerationForce() {
		return accelerationForce;
	}
}
