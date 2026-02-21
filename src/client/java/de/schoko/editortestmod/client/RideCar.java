package de.schoko.editortestmod.client;

import de.schoko.editortestmod.client.editor.EditorState;
import de.schoko.editortestmod.core.InterpolatedPoint;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;

import java.util.Arrays;

public class RideCar {
	private int cart1TravelledDist;
	private int cart1Velocity;

	private boolean stopInterpolation = false;

	private Line cart1Line;
	private Line cart4Line;

	private Line[] lines;
	private InterpolatedPoint[] points;

	private InterpolatedPoint[] oldPoints;
	private long lastTime;

	public RideCar(Line line) {
		cart1Line = line;
		cart4Line = line;
		this.lines = new Line[8];
		this.points = new InterpolatedPoint[8];
		this.oldPoints = this.points;
	}

	public void render(RenderContext context) {
		FollowerCar followerCar = EditorState.followerCarGetter.get();
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
			case TRANSFER:
				acceleration = (int) (line.getAcceleration() * 10000);
				break;
			case LIFT:
				int targetSpeed = 500;
				if (cart1Velocity <= targetSpeed) {
					acceleration = 50;
					if (cart1Velocity + acceleration > targetSpeed) {
						acceleration = targetSpeed - cart1Velocity;
					}
				} else acceleration = (int) (line.getAcceleration() * 10000);
				break;
			case BRAKE:
				int maxSpeed = 1000;
				if (cart1Velocity > maxSpeed) {
					acceleration = -500;
					if (cart1Velocity + acceleration < maxSpeed) {
						acceleration = maxSpeed - cart1Velocity;
					}
				} else acceleration = (int) (line.getAcceleration() * 10000);
		}
		return acceleration;
	}

	public void update() {
		int currentDistanceAlongTrack = cart1TravelledDist;
		currentDistanceAlongTrack += cart1Velocity;

		int acceleration = 0;
		if (lines[0] != null) for (Line line : lines) {
			acceleration += getAcceleration(line);
		}
		cart1Velocity += acceleration / lines.length;

		int lineLength = (int) (cart1Line.getLength() * 10000);

		// Assert range
		if (currentDistanceAlongTrack > lineLength) {
			if (cart1Line.getOutputLine() != null) {
				cart1Line = cart1Line.getOutputLine();
				currentDistanceAlongTrack -= lineLength;
				lineLength = (int) (cart1Line.getLength() * 10000);
			} else {
				currentDistanceAlongTrack = lineLength;
			}
		} else if (currentDistanceAlongTrack < 0) {
			if (cart1Line.getInputLine() != null) {
				cart1Line = cart1Line.getInputLine();
				lineLength = (int) (cart1Line.getLength() * 10000);
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

		for (int i = 1; i < 8; i++) {
			currentDistanceAlongTrack -= 15000;
			while (currentDistanceAlongTrack < 0) {
				if (currentLine.getInputLine() != null) {
					currentLine = currentLine.getInputLine();
					lineLength = (int) (currentLine.getLength() * 10000);
					currentDistanceAlongTrack += lineLength;
				} else {
					currentDistanceAlongTrack = 0;
				}
			}
			lines[i] = currentLine;
			points[i] = interpolate(currentLine, currentDistanceAlongTrack, lineLength);
		}
	}

	private InterpolatedPoint interpolate(Line currentLine, int dst, int line_length) {
		int t = dst;
		t *= 10000;
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
