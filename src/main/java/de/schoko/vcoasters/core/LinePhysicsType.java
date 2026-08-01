package de.schoko.vcoasters.core;

public enum LinePhysicsType {
	REGULAR(0, false),
	LIFT(50, false),
	BRAKE(500, false),
	STATION(30, true);

	private int accelerationForce;
	private final boolean fullstop;

	LinePhysicsType(int accelerationForce, boolean supportsFullstop) {
		this.accelerationForce = accelerationForce;
		fullstop = supportsFullstop;
	}

	public int getAccelerationForce() {
		return accelerationForce;
	}

	public boolean supportsFullstop() {
		return fullstop;
	}
}
