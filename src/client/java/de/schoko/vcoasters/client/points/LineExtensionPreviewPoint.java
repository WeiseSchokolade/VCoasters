package de.schoko.vcoasters.client.points;

import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
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
