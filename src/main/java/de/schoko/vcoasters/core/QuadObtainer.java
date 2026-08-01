package de.schoko.vcoasters.core;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class QuadObtainer {
	private QuadObtainer() {}

	private static List<Quad> cuboid(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ, float eX, float eY, float eZ, float fX, float fY, float fZ, float gX, float gY, float gZ, float hX, float hY, float hZ) {
		List<Quad> quads = new ArrayList<>();
		quads.add(new Quad(
			aX, aY, aZ,
			bX, bY, bZ,
			cX, cY, cZ,
			dX, dY, dZ
		));
		quads.add(new Quad(
			aX, aY, aZ,
			eX, eY, eZ,
			fX, fY, fZ,
			bX, bY, bZ
		));
		quads.add(new Quad(
			bX, bY, bZ,
			fX, fY, fZ,
			gX, gY, gZ,
			cX, cY, cZ
		));
		quads.add(new Quad(
			cX, cY, cZ,
			gX, gY, gZ,
			hX, hY, hZ,
			dX, dY, dZ
		));
		quads.add(new Quad(
			aX, aY, aZ,
			dX, dY, dZ,
			hX, hY, hZ,
			eX, eY, eZ
		));
		quads.add(new Quad(
			hX, hY, hZ,
			gX, gY, gZ,
			fX, fY, fZ,
			eX, eY, eZ
		));
		return quads;
	}
	public static List<Quad> rhomboid(Vector3f o, Vector3f x, Vector3f y, Vector3f z) {
		return cuboid(
			o.x, o.y, o.z,
			o.x + x.x, o.y + x.y, o.z + x.z,
			o.x + x.x + z.x, o.y + x.y + z.y, o.z + x.z + z.z,
			o.x + z.x, o.y + z.y, o.z + z.z,
			o.x + y.x, o.y + y.y, o.z + y.z,
			o.x + x.x + y.x, o.y + x.y + y.y, o.z + x.z + y.z,
			o.x + x.x + y.x + z.x, o.y + x.y + y.y + z.y, o.z + x.z + y.z + z.z,
			o.x + y.x + z.x, o.y + y.y + z.y, o.z + y.z + z.z
		);
	}
	public static List<Quad> boxLine(Vector3f from, Vector3f to, float width) {
		Vector3f direction = to.sub(from, new Vector3f());
		Vector3f offDirection = direction.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize(width);
		if (!offDirection.isFinite()) offDirection = new Vector3f(0f, 0f, width);
		Vector3f downDirection = offDirection.cross(direction, new Vector3f()).normalize(width);
		return rhomboid(from.sub(downDirection, new Vector3f()).sub(offDirection), direction, downDirection.mul(2), offDirection.mul(2));
	}

	public record Quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {

		public Quad(float aX, float aY, float aZ, float bX, float bY, float bZ, float cX, float cY, float cZ, float dX, float dY, float dZ) {
			this(
				new Vector3f(aX, aY, aZ),
				new Vector3f(bX, bY, bZ),
				new Vector3f(cX, cY, cZ),
				new Vector3f(dX, dY, dZ)
				);
		}

		public Optional<Double> intersects(Vector3f rayBase, Vector3f rayDirection) {
			Optional<Double> result = Geometry.rayTriangleIntersection(rayBase, rayDirection, a, b, c);
			if (result.isPresent()) return result;
			return Geometry.rayTriangleIntersection(rayBase, rayDirection, c, d, a);
		}
	}
}
