package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.client.points.Point;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;
import org.joml.Vector3f;

public class PointTranslationGizmo extends TranslationGizmo {
	private final Point point;

	public PointTranslationGizmo(Point point) {
		this.point = point;
		updateHitboxes();
	}

	@Override
	public Vector3f getCenter() {
		return point.getPos();
	}

	@Override
	public void setNewCenter(Vector3f vector3f) {
		point.getPos().set(vector3f);
		point.updateHitbox();
	}
}
