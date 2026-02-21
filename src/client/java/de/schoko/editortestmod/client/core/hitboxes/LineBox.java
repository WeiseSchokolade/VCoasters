package de.schoko.editortestmod.client.core.hitboxes;

import de.schoko.editortestmod.core.Geometry;
import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public record LineBox(Vector3f a, Vector3f b) implements Clippable {
	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		Vector3f lineVector = b.sub(a, new Vector3f());
		Vector3f bLineBase = from.toVector3f();
		Vector3f bLineDir = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);
		double smallestDistance = Geometry.getSmallestDistance(a, lineVector, bLineBase, bLineDir);
		if (Math.abs(smallestDistance) > 0.1) return Optional.empty();
		Optional<Double> possibleOffset = Geometry.getOffsetIntersectionDistanceAlongA(a, lineVector, bLineBase, bLineDir);
		if (possibleOffset.isEmpty()) return Optional.empty();
		double offset = possibleOffset.get();
		if (offset >= 0 & offset <= 1) {
			return Optional.of(new Vec3(lineVector.mul((float) offset).add(a)));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void draw(RenderContext context, Vector4f color) {
		context.drawBoxLine(a, b, 0.05f, color);
	}
}
