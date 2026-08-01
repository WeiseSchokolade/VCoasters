package de.schoko.vcoasters.client.editor;

import de.schoko.vcoasters.core.EndPoint;
import de.schoko.vcoasters.core.Line;
import org.joml.Vector3f;

public class EditorCommands {
	public static Vector3f getAverageDirection(EndPoint endPoint) {
		Line line = endPoint.getLine();
		Vector3f direction = endPoint.getLine().getDirection(1f);
		if (endPoint.isOutputEndPoint()) {
			if (line.getOutputLine() != null) {
				direction.add(line.getOutputLine().getDirection(1f));
				direction.div(2);
			}
		} else {
			if (line.getInputLine() != null) {
				direction.add(line.getInputLine().getDirection(1f));
				direction.div(2);
			}
		}
		direction.normalize();
		return direction;
	}

	public static Vector3f getWeightedAverageDirection(EndPoint endPoint) {
		Line line = endPoint.getLine();
		Vector3f direction = line.getDirection(1f).mul(line.getLengthSquared());
		if (endPoint.isOutputEndPoint()) {
			if (line.getOutputLine() != null) {
				direction.add(line.getOutputLine().getDirection(1f).mul(line.getOutputLine().getLengthSquared()));
			}
		} else {
			if (line.getInputLine() != null) {
				direction.add(line.getInputLine().getDirection(1f).mul(line.getInputLine().getLengthSquared()));
			}
		}
		direction.normalize();
		return direction;
	}

	public static void applyWeightedRotationRecursively(EndPoint origin) {
		Line originLine = origin.getLine();
		if (!origin.isOutputEndPoint()) {

			origin = originLine.getOutputEndPoint();
		}
		Line currentLine = originLine;

		Vector3f previousDirection = getWeightedAverageDirection(origin);
		double previousYaw = Math.atan2(previousDirection.z, previousDirection.x) - Math.PI * 0.5;
		double previousPitch = -Math.asin(previousDirection.y);

		double accumulatedDYaw = previousYaw;
		double accumulatedDPitch = previousPitch;

		origin.setYaw((float) accumulatedDYaw);
		origin.setPitch((float) accumulatedDPitch);
		origin.updateCorrespondingEndpoint();

		int i = 0;
		while ((currentLine = currentLine.getOutputLine()) != null && currentLine != originLine && (i++) < 10000) {
			EndPoint outputEndPoint = currentLine.getOutputEndPoint();
			Vector3f direction = getWeightedAverageDirection(outputEndPoint);
			double yaw = Math.atan2(direction.z, direction.x) - Math.PI * 0.5;
			double pitch = -Math.asin(direction.y);
			double dYaw = yaw - previousYaw;
			if (dYaw < -1 * Math.PI) {
				dYaw += 2 * Math.PI;
			}
			double dPitch = pitch - previousPitch;
			if (dYaw > 1 * Math.PI) {
				dYaw -= 2 * Math.PI;
			}
			accumulatedDYaw += dYaw;
			accumulatedDPitch += dPitch;

			previousYaw = yaw;
			previousPitch = pitch;

			outputEndPoint.setYaw((float) accumulatedDYaw);
			outputEndPoint.setPitch((float) accumulatedDPitch);
			outputEndPoint.updateCorrespondingEndpoint();
		}
		if (i >= 10000) throw new IllegalArgumentException("Encountered loop (" + i + " iterations!) and applied infinite loop protection.");

		if (currentLine == originLine) {
			EndPoint inputEndPoint = originLine.getInputEndPoint();
			Vector3f direction = getWeightedAverageDirection(origin);
			double yaw = Math.atan2(direction.z, direction.x) - Math.PI * 0.5;
			double pitch = -Math.asin(direction.y);
			inputEndPoint.setYaw((float) yaw);
			inputEndPoint.setPitch((float) pitch);
		}
	}
}
