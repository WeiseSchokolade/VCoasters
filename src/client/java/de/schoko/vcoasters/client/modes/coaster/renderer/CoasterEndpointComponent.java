package de.schoko.vcoasters.client.modes.coaster.renderer;

import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.core.*;
import net.minecraft.server.commands.VersionCommand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;

public class CoasterEndpointComponent implements EditorComponent {
	private EndPoint endpoint;
	public List<QuadObtainer.Quad> renderedQuads;
	Vector3f b;
	Vector3f a;
	Vector3f c;

	public CoasterEndpointComponent(EndPoint endpoint) {
		this.endpoint = endpoint;
		updateQuads();
	}

	public void upload(RenderContext context, boolean isTarget, boolean isSelected) {
		if (endpoint.isOutputEndPoint() && endpoint.getLine().getOutputLine() != null) return;
		updateQuads();
		Vector4f baseColor = switch (endpoint.getLine().getPhysicsType()) {
			case LIFT -> new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
			case BRAKE -> new Vector4f(0.8f, 0.8f, 0.2f, 1.0f);
			case STATION -> new Vector4f(0.8f, 0.2f, 0.8f, 1.0f);
			case null, default -> EditorStyle.LINE_COLOR;
		};

		Vector4f color = isTarget ? Colors.WHITE : isSelected ? new Vector4f(1f).lerp(baseColor, 0.5f) : baseColor;
		context.drawQuads(renderedQuads, color);
		//context.drawBoxLine(line.getInputEndPoint().pos(), line.getOutputEndPoint().pos(), EditorStyle.TRACK_LINE_WIDTH, color);
		//Line outputLine = line.getOutputLine();
		//if (outputLine != null) {
		//	context.drawBoxLine(line.getOutputEndPoint().pos().sub(line.getDirection(0.3f), new Vector3f()), outputLine.getInputEndPoint().pos().add(outputLine.getDirection(0.3f), new Vector3f()), 0.025f, Colors.YELLOW);
		//}
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
		Vector3f direction = endpoint.getRotatedViewDirection(1);
		//Vector3f rollVector = direction.cross(new Vector3f(0f, 1f, 0f), new Vector3f());//.cross(direction).rotateAxis(endpoint.getRoll(), direction.x, direction.y, direction.z).normalize(0.5f);

		Vector3f offset = direction.cross(0, 1, 0, new Vector3f()).rotateAxis(endpoint.getRoll(), direction.x, direction.y, direction.z);

		offset.normalize(0.5f);
		Vector3f up = direction.cross(offset);


		a = endpoint.pos().add(offset, new Vector3f());
		b = endpoint.pos().sub(offset, new Vector3f());
		c = endpoint.pos().add(up, new Vector3f());

		renderedQuads = QuadObtainer.boxLine(a, b, EditorStyle.TRACK_LINE_WIDTH);
		renderedQuads.addAll(QuadObtainer.boxLine(b, c, EditorStyle.TRACK_LINE_WIDTH));
		renderedQuads.addAll(QuadObtainer.boxLine(c, a, EditorStyle.TRACK_LINE_WIDTH));
	}
}
