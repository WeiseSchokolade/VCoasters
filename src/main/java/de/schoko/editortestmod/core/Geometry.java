package de.schoko.editortestmod.core;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

public final class Geometry {
	public static Vector3f applyRotation(Vector3f vector, float yaw, float pitch, float roll) {
		vector.rotateZ(roll);
		vector.rotateX(pitch);
		vector.rotateY(-yaw);
		return vector;
	}

	public static Quaternionf getRotationQuaternion(float yaw, float pitch, float roll) {
		return new Quaternionf().rotationZYX(roll, -yaw, pitch);
	}

	public static double getIntersectionCoordinateAlongLine(Vector3f lineBase, Vector3f lineDirection, Vector3f planeBase, Vector3f planeNormal) {
		return -(planeNormal.dot(lineBase.sub(planeBase))) / (planeNormal.dot(lineDirection));
	}

	public static double getSmallestDistance(Vector3f aBase, Vector3f aDir, Vector3f bBase, Vector3f bDir) {
		Vector3f cross = aDir.cross(bDir, new Vector3f()).normalize();
		return bBase.sub(aBase, new Vector3f()).dot(cross);
	}

	public static Vector3f getIntersectionOffset(Vector3f aBase, Vector3f aDir, Vector3f bBase, Vector3f bDir) {
		Vector3f offset = aDir.cross(bDir, new Vector3f()).normalize();
		offset.mul(bBase.sub(aBase, new Vector3f()).dot(offset));
		return offset;
	}

	public static Optional<Double> getOffsetIntersectionDistanceAlongA(Vector3f aBase, Vector3f aDir, Vector3f bBase, Vector3f bDir) {
		if (aDir.equals(bDir)) return Optional.empty();
		Vector3f offsetBase = getIntersectionOffset(aBase, aDir, bBase, bDir).add(aBase);
		if (bDir.x == 0 && aDir.x != 0) return Optional.of((double) ((bBase.x - offsetBase.x) / aDir.x));
		double distanceAlongA = (bBase.y - offsetBase.y + bDir.y / bDir.x * (offsetBase.x - bBase.x))
								/
								(aDir.y - aDir.x * (bDir.y / bDir.x));
		if (Double.isFinite(distanceAlongA)) {
			return Optional.of(distanceAlongA);
		} else return Optional.empty();
	}

	public static Vector3f getPointAlongLine(Vector3f base, float lambda, Vector3f dir) {
		return new Vector3f(dir).mulAdd(lambda, base);
	}

	public static float fourthPower(float value) {
		return value * value * value * value;
	}

	public static float squared(float value) {
		return value * value;
	}

	public static float[] getLineSphereIntersection(Vector3f base, Vector3f dir, Vector3f sphereCenter, float radius) {
		base = new Vector3f(base).sub(sphereCenter);
		float dotProduct = base.dot(dir);


		return new float[] {
			positiveQuadraticFormula(dir.lengthSquared(), 2 * dotProduct, base.lengthSquared() - squared(radius)),
			negativeQuadraticFormula(dir.lengthSquared(), 2 * dotProduct, base.lengthSquared() - squared(radius))
		};
	}

	private static float positiveQuadraticFormula(float a, float b, float c) {
		return (float) ((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
	}

	private static float negativeQuadraticFormula(float a, float b, float c) {
		return (float) ((-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a));
	}

	public static float linePlaneIntersection(Vector3f lineBase, Vector3f lineDirection, Vector3f planeNormal, Vector3f planeBase) {
		return -(planeNormal.dot(lineBase.sub(planeBase, new Vector3f()))) / (planeNormal.dot(lineDirection));
	}
}
