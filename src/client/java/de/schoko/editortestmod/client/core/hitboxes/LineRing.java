package de.schoko.editortestmod.client.core.hitboxes;

import de.schoko.editortestmod.core.Geometry;
import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public record LineRing(LineBox[] boxes, int segmentAmount, Vector3f center, Vector3f up, Vector3f right, float radius) implements Clippable {
	public LineRing(int segmentAmount, Vector3f center, Vector3f up, Vector3f right, float radius) {
		this(new LineBox[segmentAmount], segmentAmount, center, up, right, radius);

		double deltaAngle = 2 * Math.PI / segmentAmount;
		Vector3f prevPoint = new Vector3f(center).add(right);
		for (int i = 0; i <= segmentAmount; i++) {
			double angle = deltaAngle * (i + 0.5f);
			Vector3f basePoint = up.mulAdd((float) Math.sin(angle), right.mulAdd((float) Math.cos(angle), center, new Vector3f()), new Vector3f());
			if (i != 0) boxes[i - 1] = new LineBox(prevPoint, basePoint);
			prevPoint = basePoint;
		}
	}

	@Override
	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		Vector3f direction = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);
		float lambda = Geometry.linePlaneIntersection(from.toVector3f(), direction, up.cross(right, new Vector3f()), center);
		Vec3 point = new Vec3(Geometry.getPointAlongLine(from.toVector3f(), lambda, direction));
		double distance = point.distanceTo(new Vec3(center.x, center.y, center.z));
		if (distance < radius - 0.1f || distance > radius + 0.1f) return Optional.empty();
		return Optional.of(point);
	}

	public Optional<Vec3> intersectionOnPlane(Vec3 from, Vec3 to) {
		Vector3f direction = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);
		float lambda = Geometry.linePlaneIntersection(from.toVector3f(), direction, up.cross(right, new Vector3f()), center);
		return Optional.of(new Vec3(Geometry.getPointAlongLine(from.toVector3f(), lambda, direction)));
	}

	@Override
	public void draw(RenderContext context, Vector4f color) {
		for (LineBox box : boxes) {
			box.draw(context, color);
		}
	}
}
