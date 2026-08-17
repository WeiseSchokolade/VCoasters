package de.schoko.vcoasters.client.modes.train.renderer;

import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.client.modes.train.TrainEditorMode;
import de.schoko.vcoasters.core.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainLineBoxComponent implements EditorComponent {
	private final Line line;
	private final TrainEditorMode editorMode;
	public List<QuadObtainer.Quad> renderedQuads;

	public TrainLineBoxComponent(Line line, TrainEditorMode editorMode) {
		this.line = line;
		this.editorMode = editorMode;
		renderedQuads = new ArrayList<>();
		updateQuads();
	}

	public void upload(RenderContext context, boolean isTarget, boolean isSelected) {
		updateQuads();
		Vector4f baseColor = switch (line.getPhysicsType()) {
			case LIFT -> new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
			case BRAKE -> new Vector4f(0.8f, 0.8f, 0.2f, 1.0f);
			case STATION -> new Vector4f(0.8f, 0.2f, 0.8f, 1.0f);
			case null, default -> EditorStyle.LINE_COLOR;
		};

		Vector4f color = isTarget ? Colors.WHITE : isSelected ? new Vector4f(1f).lerp(baseColor, 0.5f) : baseColor;
		context.drawQuads(renderedQuads, color);
	}

	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		//Vector3f lineVector = line.getOutputEndPoint().pos().sub(line.getInputEndPoint().pos(), new Vector3f());
		Vector3f bLineBase = from.toVector3f();
		Vector3f bLineDir = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);

		Optional<Double> first = renderedQuads.stream().map(quad -> quad.intersects(bLineBase, bLineDir))
			.filter(Optional::isPresent)
			.filter(aDouble -> aDouble.get() > 0.0 && aDouble.get() <= 1.0)
			.map(Optional::get)
			.sorted()
			.findFirst();
		if (first.isEmpty()) return Optional.empty();
		return Optional.of(from.add(new Vec3(bLineDir.mul(first.get().floatValue()))));
	}

	public void updateQuads() {
		renderedQuads.clear();
		Vector3f deltaVector = line.getDeltaVector();
		Vector3f normal = new Vector3f(0, 1, 0);
		Vector3f supportDirection = normal.cross(deltaVector, new Vector3f()).normalize(editorMode.getBeamLength() / 2);
		EndPoint input = line.getInputEndPoint();
		renderedQuads.addAll(
			QuadObtainer.boxLine(
				new Vector3f(input.pos()).sub(supportDirection).add(deltaVector.div(2, new Vector3f())).sub(0, editorMode.getBeamHeight() + editorMode.getRailHeight(), 0),
				new Vector3f(input.pos()).add(supportDirection).add(deltaVector.div(2, new Vector3f())).sub(0, editorMode.getBeamHeight() + editorMode.getRailHeight(), 0), editorMode.getBeamWidth(), editorMode.getBeamHeight())
		);
		supportDirection.normalize(editorMode.getRailGauge() / 2);
		renderedQuads.addAll(
			QuadObtainer.boxLine(
				new Vector3f(input.pos()).add(supportDirection),
				new Vector3f(input.pos()).add(supportDirection).add(deltaVector), editorMode.getRailThickness(), editorMode.getRailHeight())
		);
		renderedQuads.addAll(
			QuadObtainer.boxLine(
				new Vector3f(input.pos()).sub(supportDirection),
				new Vector3f(input.pos()).sub(supportDirection).add(deltaVector), editorMode.getRailThickness(), editorMode.getRailHeight())
		);
	}

	public Line getLine() {
		return line;
	}
}
