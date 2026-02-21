package de.schoko.editortestmod.client.renderer;

import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.editor.EditorStyle;
import de.schoko.editortestmod.core.*;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public class LineRenderer extends Renderer<Line> {
	public LineRenderer(Line line) {
		super(line);
	}

	@Override
	public void upload(RenderContext context, EditorObject target, EditorObject selected) {
		Vector4f baseColor = switch (getObject().getPhysicsType()) {
			case LIFT -> new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
			case BRAKE -> new Vector4f(0.7f, 0.7f, 0.3f, 1.0f);
			case null, default -> EditorStyle.LINE_COLOR;
		};

		Vector4f color = isRendered(target) ? Colors.WHITE : isRendered(selected) ? new Vector4f(1f).lerp(baseColor, 0.5f) : baseColor;
		context.drawBoxLine(getObject().getInputEndPoint().pos(), getObject().getOutputEndPoint().pos(), EditorStyle.TRACK_LINE_WIDTH, color);
		Line outputLine = getObject().getOutputLine();
		if (outputLine != null) {
			context.drawBoxLine(getObject().getOutputEndPoint().pos().sub(getObject().getDirection(0.3f), new Vector3f()), outputLine.getInputEndPoint().pos().add(outputLine.getDirection(0.3f), new Vector3f()), 0.025f, Colors.YELLOW);
		}
	}

	@Override
	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		Vector3f lineVector = getObject().getOutputEndPoint().pos().sub(getObject().getInputEndPoint().pos(), new Vector3f());
		Vector3f bLineBase = from.toVector3f();
		Vector3f bLineDir = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);
		double smallestDistance = Geometry.getSmallestDistance(getObject().getInputEndPoint().pos(), lineVector, bLineBase, bLineDir);
		if (Math.abs(smallestDistance) > 0.1) return Optional.empty();
		Optional<Double> possibleOffset = Geometry.getOffsetIntersectionDistanceAlongA(getObject().getInputEndPoint().pos(), lineVector, bLineBase, bLineDir);
		if (possibleOffset.isEmpty()) return Optional.empty();
		double offset = possibleOffset.get();
		if (offset >= 0 & offset <= 1) {
			return Optional.of(new Vec3(lineVector.mul((float) offset).add(getObject().getInputEndPoint().pos())));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void updateHitbox(Line object) {
		getObject().getInputEndPoint().getRenderer().updateHitbox(getObject().getInputEndPoint());
		getObject().getOutputEndPoint().getRenderer().updateHitbox(getObject().getOutputEndPoint());
	}

	@Override
	public boolean isDirty() {
		if (super.isDirty()) return true;
		if (getObject().getInputEndPoint().getRenderer().isDirty()) return true;
		if (getObject().getOutputEndPoint().getRenderer().isDirty()) return true;
		return false;
	}

	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		if (!dirty) {
			getObject().getInputEndPoint().getRenderer().setDirty(false);
			getObject().getOutputEndPoint().getRenderer().setDirty(false);
		}
	}
}
