package de.schoko.editortestmod.client;

import de.schoko.editortestmod.core.InterpolatedPoint;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.LinePhysicsType;
import de.schoko.editortestmod.core.RenderContext;

import java.util.Arrays;

public class RideCar {
	private static final int LINE_LENGTH_MODIFIER = 10000;

	private int cart1TravelledDist;
	private int cart1Velocity;

	private boolean stopInterpolation = false;

	private Line cart1Line;
	private Line cart4Line;
	private final int segmentAmount;
	private final FollowerCar followerCar;

	private Line[] lines;
	private InterpolatedPoint[] points;

	private InterpolatedPoint[] oldPoints;
	private long lastTime;
	private long lastUpdate;

	public RideCar(Line line, int segmentAmount, FollowerCar followerCar) {
		cart1Line = line;
		cart4Line = line;
		this.segmentAmount = segmentAmount;
		this.followerCar = followerCar;
		this.lines = new Line[segmentAmount];
		this.points = new InterpolatedPoint[segmentAmount];
		this.oldPoints = this.points;
	}

	public void render(RenderContext context) {
		if (oldPoints == null) return;
		float t = Math.clamp((System.currentTimeMillis() - lastTime) * 0.05f, 0, 1);
		lastTime = System.currentTimeMillis();
		for (int i = 0; i < points.length; i++) {
			if (points[i] == null) break;
			followerCar.setPoint(
				InterpolatedPoint.lerp(t, oldPoints[i], points[i])
			);

			followerCar.draw(context);
		}
	}

	private int getAcceleration(Line line) {
		int acceleration = 0;
		switch (line.getPhysicsType()) {
			case null:
			case REGULAR:
				acceleration = (int) Math.round(line.getAcceleration() * LINE_LENGTH_MODIFIER);
				break;
			case LIFT:
				int targetSpeed = 500;
				if (cart1Velocity <= targetSpeed) {
					acceleration = LinePhysicsType.LIFT.getAccelerationForce();
					if (cart1Velocity + acceleration > targetSpeed) {
						acceleration = targetSpeed - cart1Velocity;
					}
				} else acceleration = (int) Math.round(line.getAcceleration() * LINE_LENGTH_MODIFIER);
				break;
			case BRAKE:
				int maxSpeed = 1000;
				if (cart1Velocity > maxSpeed) {
					acceleration = -LinePhysicsType.BRAKE.getAccelerationForce();
					if (cart1Velocity + acceleration < maxSpeed) {
						acceleration = maxSpeed - cart1Velocity;
					}
				} else acceleration = (int) Math.round(line.getAcceleration() * LINE_LENGTH_MODIFIER);
				break;
			case STATION:
				targetSpeed = (line.isFullStop() ? 0 : 500);
				if (cart1Velocity <= targetSpeed) {
					acceleration = LinePhysicsType.STATION.getAccelerationForce();
					if (cart1Velocity + acceleration > targetSpeed) {
						acceleration = targetSpeed - cart1Velocity;
					}
				}
				if (cart1Velocity > targetSpeed && line.isFullStop()) {
					acceleration = -LinePhysicsType.STATION.getAccelerationForce();
					if (cart1Velocity + acceleration < targetSpeed) {
						acceleration = targetSpeed - cart1Velocity;
					}
				}
				break;
		}
		if (cart1Velocity != 0) acceleration -= (cart1Velocity * followerCar.getScreen().getTrack().getFriction()) / LINE_LENGTH_MODIFIER;
		//acceleration += ((int) -Math.signum(cart1Velocity)) * Math.abs((cart1Velocity * followerCar.getScreen().getTrack().getFriction()) / LINE_LENGTH_MODIFIER);
		return acceleration;
	}

	public void update() {
		long delta = System.currentTimeMillis() - lastUpdate;
		if (delta < 50) {
			return;
		} else if (delta > 15000) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		lastUpdate += 50;

		int currentDistanceAlongTrack = cart1TravelledDist;
		currentDistanceAlongTrack += cart1Velocity;


		int lineLength = Math.round(cart1Line.getLength() * LINE_LENGTH_MODIFIER);

		// Assert range
		if (currentDistanceAlongTrack > lineLength) {
			if (cart1Line.getOutputLine() != null) {
				cart1Line = cart1Line.getOutputLine();
				currentDistanceAlongTrack -= lineLength;
				lineLength = Math.round(cart1Line.getLength() * LINE_LENGTH_MODIFIER);
			} else {
				currentDistanceAlongTrack = lineLength;
			}
		} else if (currentDistanceAlongTrack < 0) {
			if (cart1Line.getInputLine() != null) {
				cart1Line = cart1Line.getInputLine();
				lineLength = Math.round(cart1Line.getLength() * LINE_LENGTH_MODIFIER);
				currentDistanceAlongTrack += lineLength;
			} else {
				currentDistanceAlongTrack = 0;
			}
		}
		cart1TravelledDist = currentDistanceAlongTrack;

		Line currentLine = cart1Line;

		if (stopInterpolation) return;
		Arrays.setAll(oldPoints, value -> points[value]);

		points[0] = interpolate(currentLine, currentDistanceAlongTrack, lineLength);
		lines[0] = currentLine;

		int acceleration = 0;
		int totalAcceleration = getAcceleration(currentLine);
		boolean calculateTotalAcceleration = currentLine.getPhysicsType() != LinePhysicsType.STATION;

		for (int i = 1; i < points.length; i++) {
			currentDistanceAlongTrack -= (int) (1.5 * LINE_LENGTH_MODIFIER);
			while (currentDistanceAlongTrack < 0) {
				if (currentLine.getInputLine() != null) {
					currentLine = currentLine.getInputLine();
					lineLength = Math.round(currentLine.getLength() * LINE_LENGTH_MODIFIER);
					currentDistanceAlongTrack += lineLength;
				} else {
					currentDistanceAlongTrack = 0;
				}
			}
			lines[i] = currentLine;
			points[i] = interpolate(currentLine, currentDistanceAlongTrack, lineLength);
			if (calculateTotalAcceleration) acceleration = getAcceleration(currentLine);
			totalAcceleration += acceleration;
		}

		acceleration = totalAcceleration;
		acceleration /= points.length;
		cart1Velocity += acceleration;
	}

	private InterpolatedPoint interpolate(Line currentLine, int dst, int line_length) {
		int t = dst;
		t *= LINE_LENGTH_MODIFIER;
		t /= line_length;
		t /= 10;

		int x = (int) (currentLine.getInputEndPoint().x() * 1000);
		int y = (int) (currentLine.getInputEndPoint().y() * 1000);
		int z = (int) (currentLine.getInputEndPoint().z() * 1000);
		int yaw = (int) (currentLine.getInputEndPoint().yaw() * 1000);
		int pitch = (int) (currentLine.getInputEndPoint().pitch() * 1000);
		int roll = (int) (currentLine.getInputEndPoint().roll() * 1000);

		int dx = (int) (currentLine.getOutputEndPoint().x() * 1000) - x;
		int dy = (int) (currentLine.getOutputEndPoint().y() * 1000) - y;
		int dz = (int) (currentLine.getOutputEndPoint().z() * 1000) - z;
		int dyaw = (int) (currentLine.getOutputEndPoint().yaw() * 1000) - yaw;
		int dpitch = (int) (currentLine.getOutputEndPoint().pitch() * 1000) - pitch;
		int droll = (int) (currentLine.getOutputEndPoint().roll() * 1000) - roll;

		return new InterpolatedPoint(
			interpolate(t, x, dx) * 0.001f,
			interpolate(t, y, dy) * 0.001f,
			interpolate(t, z, dz) * 0.001f,
			interpolate(t, yaw, dyaw) * 0.001f,
			interpolate(t, pitch, dpitch) * 0.001f,
			interpolate(t, roll, droll) * 0.001f
		);
	}

	private int interpolate(int t, int a, int da) {
		da *= t;
		da /= 1000;

		return a + da;
	}
}
