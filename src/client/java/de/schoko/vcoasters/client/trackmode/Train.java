package de.schoko.vcoasters.client.trackmode;

import de.schoko.vcoasters.codecs.LineCodecs;
import de.schoko.vcoasters.core.InterpolatedPoint;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.LinePhysicsType;

import java.util.function.Consumer;

public class Train {
	private static final int TARGET_LIFT_SPEED = 1000;
	private static final int TARGET_THROUGH_STATION_SPEED = 1000;
	private static final int MAX_BRAKE_SPEED = 1500;


	private FrontCar frontCar;

	private final int friction;
	private final int segmentAmount;
	private final int distanceBetweenCars;
	private int acceleration;

	public Train(Line line, int friction, int segmentAmount, float distanceBetweenCars) {
		this.frontCar = new FrontCar(line);
		this.friction = friction;//8;
		this.segmentAmount = segmentAmount;//8;
		this.distanceBetweenCars = (int) (distanceBetweenCars * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);//15000;
	}

	public static int getAcceleration(Line line, int velocity, boolean isFirstCar) {
		int acceleration;
		switch (line.getPhysicsType()) {
			case BRAKE:
				if (velocity <= MAX_BRAKE_SPEED) break;
				acceleration = -LinePhysicsType.BRAKE.getAccelerationForce();
				if (velocity + acceleration > MAX_BRAKE_SPEED) return acceleration;
				return MAX_BRAKE_SPEED - velocity; // Make up the remaining difference
			case LIFT:
				if (velocity > TARGET_LIFT_SPEED) break;
				if (velocity == TARGET_LIFT_SPEED) return 0;
				acceleration = LinePhysicsType.LIFT.getAccelerationForce();
				if (velocity + acceleration <= TARGET_LIFT_SPEED) return acceleration;
				return TARGET_LIFT_SPEED - velocity; // Make up the remaining difference
			case STATION:
				if (!isFirstCar) break;
				int targetSpeed = TARGET_THROUGH_STATION_SPEED;
				if (line.isFullStop()) {
					targetSpeed = 0;
					if (velocity > targetSpeed) {
						acceleration = -30;
						if (velocity + acceleration > targetSpeed) return acceleration;
						return targetSpeed - velocity; // Make up the remaining difference
					}
				}
				acceleration = (int) (line.getAcceleration() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				if (velocity <= targetSpeed) acceleration = 30;
				if (velocity + acceleration <= targetSpeed) return acceleration;
				return targetSpeed - velocity;
			case null:
			default:
				break;
		}

		return (int) (line.getAcceleration() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
	}

	public void update() {
		frontCar.update();

		Line inspectedLine = frontCar.line;
		int distanceAlongLine = frontCar.distanceAlongLine;

		if (inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
			acceleration = getAcceleration(inspectedLine, frontCar.velocity, true);
		} else {
			int totalAcceleration = 0;
			for (int i = 0; i < segmentAmount; i++) {
				totalAcceleration += getAcceleration(inspectedLine, frontCar.velocity, i == 0);
				distanceAlongLine -= distanceBetweenCars;
				if (distanceAlongLine < 0) {
					if (inspectedLine.getInputLine() != null) {
						inspectedLine = inspectedLine.getInputLine();
						distanceAlongLine += (int) (inspectedLine.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
					} else {
						distanceAlongLine = 0;
					}
				}
			}
			acceleration = totalAcceleration / segmentAmount;
		}
		if (Math.abs(frontCar.velocity) > 100) {
			int appliedFriction = (frontCar.velocity * friction) / 10000;
			if (Math.abs(appliedFriction) == 1) appliedFriction = 0;
			acceleration -= appliedFriction;
		}

		frontCar.velocity += acceleration;
	}

	public void extractRenderedPositions(Consumer<InterpolatedPoint> positionConsumer) {
		Line inspectedLine = frontCar.line;
		int distanceAlongLine = frontCar.distanceAlongLine;

		for (int i = 0; i < segmentAmount; i++) {
			positionConsumer.accept(inspectedLine.lerp(((float) distanceAlongLine / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER) / inspectedLine.getLength()));
			distanceAlongLine -= distanceBetweenCars;
			while (distanceAlongLine < 0) {
				if (inspectedLine.getInputLine() != null) {
					inspectedLine = inspectedLine.getInputLine();
					distanceAlongLine += (int) (inspectedLine.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				} else {
					distanceAlongLine = 0;
				}
			}
		}
	}

	public int getVelocity() {
		return frontCar.velocity;
	}

	public void addToVelocity(int value) {
		frontCar.velocity += value;
	}

	public int getAcceleration() {
		return acceleration;
	}

	public InterpolatedPoint getCarPosition(int index) {
		Line inspectedLine = frontCar.line;
		int distanceAlongLine = frontCar.distanceAlongLine - index * distanceBetweenCars;

		while (distanceAlongLine < 0) {
			if (inspectedLine.getInputLine() != null) {
				inspectedLine = inspectedLine.getInputLine();
				distanceAlongLine += (int) (inspectedLine.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
			} else {
				distanceAlongLine = 0;
			}
		}
		return inspectedLine.lerp(((float) distanceAlongLine / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER) / inspectedLine.getLength());
	}

	public void move(double dx) {
		frontCar.distanceAlongLine += (int) Math.round(dx * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
		if (frontCar.distanceAlongLine < 0) {
			if (frontCar.line.getInputLine() != null) {
				frontCar.line = frontCar.line.getInputLine();
				frontCar.distanceAlongLine += (int) (frontCar.line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
			} else {
				frontCar.distanceAlongLine = 0;
			}
		}
		if (frontCar.distanceAlongLine > (int) (frontCar.line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER)) {
			frontCar.distanceAlongLine -= (int) (frontCar.line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
			frontCar.line = frontCar.line.getOutputLine();
		}
	}

	static class FrontCar {
		private Line line;
		private int velocity;
		private int distanceAlongLine;

		public FrontCar(Line line) {
			this.line = line;
		}

		public void update() {
			distanceAlongLine += velocity;
			while (distanceAlongLine > line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER) {
				if (line.getOutputLine() != null) {
					distanceAlongLine -= (int) (line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
					line = line.getOutputLine();
					// Call ON_REACH
				} else {
					distanceAlongLine = (int) (line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				}
			}
			while (distanceAlongLine < 0) {
				if (line.getInputLine() != null) {
					line = line.getInputLine();
					distanceAlongLine += (int) (line.getLength() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER);
				} else {
					distanceAlongLine = 0;
				}
			}
		}
	}
}
