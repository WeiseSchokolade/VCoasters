package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import org.joml.Vector3f;

public class LineTranslationGizmo extends TranslationGizmo {
	private final Line line;

	public LineTranslationGizmo(Line line) {
		this.line = line;
		updateHitboxes();
	}

	@Override
	public void draw(RenderContext context, EditorObject target) {
		if (line.getRenderer().isDirty()) updateHitboxes();
		super.draw(context, target);
	}

	@Override
	public Vector3f getCenter() {
		return line.getCenter();
	}

	@Override
	public void setNewCenter(Vector3f vector3f) {
		line.setNewCenter(vector3f);
		line.markRendererAsDirty();
	}

}
