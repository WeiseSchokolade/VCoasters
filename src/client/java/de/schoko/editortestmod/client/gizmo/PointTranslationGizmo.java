package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.client.editor.EditorOptions;
import de.schoko.editortestmod.client.points.Point;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;
import org.joml.Vector3f;

import java.util.Vector;

public class PointTranslationGizmo extends TranslationGizmo {
	private final Point point;

	public PointTranslationGizmo(Point point) {
		this.point = point;
		setNewCenter(new Vector3f(point.getPos()));
		updateHitboxes();
	}

	@Override
	public Vector3f getCenter() {
		return point.getPos();
	}

	@Override
	public void setNewCenter(Vector3f vector3f) {
		snap(vector3f);
		point.getPos().set(vector3f);
		point.updateHitbox();
	}

	public void snap(Vector3f vector3f) {
		EditorOptions.SnapSetting snapping = EditorOptions.getSnapSetting();
		vector3f.set(
			snapping.snap(vector3f.x),
			snapping.snap(vector3f.y),
			snapping.snap(vector3f.z)
		);
	}
}
