package de.schoko.editortestmod.client.points;

import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LineExtensionPreviewPoint extends Point {
	private final Line line;

	public LineExtensionPreviewPoint(Vector3f pos, Line line, Vector4f color) {
		super(pos, color);
		this.line = line;
	}

	@Override
	public void draw(RenderContext context) {
		context.drawBoxLine(getPos(), line.getOutputEndPoint().pos(), 0.03f, Colors.LIGHT_BLUE);
		context.drawAABB(getAABB(), getColor());
	}

	public Line getLine() {
		return line;
	}
}
