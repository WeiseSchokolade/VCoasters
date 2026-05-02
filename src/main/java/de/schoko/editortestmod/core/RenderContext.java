package de.schoko.editortestmod.core;

import net.minecraft.world.phys.AABB;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;


public interface RenderContext {
	void drawQuads(Iterable<QuadObtainer.Quad> quads, float r, float g, float b, float a);

	default void drawQuads(Iterable<QuadObtainer.Quad> quads, Vector4f color) {
		drawQuads(quads, color.x, color.y, color.z, color.w);
	}

	void drawAABox(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, float r, float g, float b, float a);

	default void drawAABox(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, Vector4f color) {
		drawAABox(fromX, fromY, fromZ, toX, toY, toZ, color.x, color.y, color.z, color.w);
	}

	default void drawAABox(Vector3f from, Vector3f to, Vector4f color) {
		drawAABox(from.x, from.y, from.z, to.x, to.y, to.z, color.x, color.y, color.z, color.w);
	}

	default void drawAABB(AABB aabb, Vector4f color) {
		drawAABox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color.x, color.y, color.z, color.w);
	}

	default void drawBoxPoint(Vector3f point, float sideLengthHalved, Vector4f color) {
		drawAABox(point.x - sideLengthHalved, point.y - sideLengthHalved, point.z - sideLengthHalved, point.x + sideLengthHalved, point.y + sideLengthHalved, point.z + sideLengthHalved, color);
	}

	void drawRhomboid(Vector3f origin, Vector3f x, Vector3f y, Vector3f z, Vector4f color);

	default void drawRotatedBox(Vector3f origin, float yaw, float pitch, float roll, float width, float height, float depth, Vector4f color) {
		Vector3f x = Geometry.applyRotation(new Vector3f(width, 0, 0), yaw, pitch, roll);
		Vector3f y = Geometry.applyRotation(new Vector3f(0, height, 0), yaw, pitch, roll);
		Vector3f z = Geometry.applyRotation(new Vector3f(0, 0, depth), yaw, pitch, roll);
		drawRhomboid(origin, x, y, z, color);
	}

	default void drawRotatedBox(Vector3f origin, float yaw, float pitch, float roll, Vector3f rotationCenter, float width, float height, float depth, Vector4f color) {
		Vector3f x = Geometry.applyRotation(new Vector3f(width, 0, 0), yaw, pitch, roll);
		Vector3f y = Geometry.applyRotation(new Vector3f(0, height, 0), yaw, pitch, roll);
		Vector3f z = Geometry.applyRotation(new Vector3f(0, 0, depth), yaw, pitch, roll);
		drawRhomboid(origin.sub(Geometry.applyRotation(rotationCenter, yaw, pitch, roll), new Vector3f()), x, y, z, color);
	}

	default void drawBoxLine(Vector3f from, Vector3f to, float width, Vector4f color) {
		Vector3f direction = to.sub(from, new Vector3f());
		Vector3f offDirection = direction.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize(width);
		if (!offDirection.isFinite()) offDirection = new Vector3f(0f, 0f, width);
		Vector3f downDirection = offDirection.cross(direction, new Vector3f()).normalize(width);
		drawRhomboid(from.sub(downDirection, new Vector3f()).sub(offDirection), direction, downDirection.mul(2), offDirection.mul(2), color);
	}

	default void drawBoxLine(Vector3f from, Vector3f to, float width, float r, float g, float b, float a) {
		drawBoxLine(from, to, width, vecOf(r, g, b, a));
	}

	static Vector3f vecOf(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}

	static Vector4f vecOf(float x, float y, float z, float w) {
		return new Vector4f(x, y, z, w);
	}


}
