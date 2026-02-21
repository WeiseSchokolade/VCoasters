package de.schoko.editortestmod.client.core.hitboxes;

import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public record SegmentedLineRing(LineBox[] boxes) implements Clippable {
	public SegmentedLineRing(int segmentAmount, Vector3f center, Vector3f up, Vector3f right) {
		this(new LineBox[segmentAmount]);

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
		for (LineBox box : boxes) {
			Optional<Vec3> clip = box.clip(from, to);
			if (clip.isPresent()) return clip;
		}
		return Optional.empty();
	}

	@Override
	public void draw(RenderContext context, Vector4f color) {
		for (LineBox box : boxes) {
			box.draw(context, color);
		}
	}
}
