package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.client.editor.EditorOptions;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.RenderContext;
import org.joml.Vector3f;

public class EndPointTranslationGizmo extends TranslationGizmo {
	private final EndPoint endpoint;

	public EndPointTranslationGizmo(EndPoint endpoint) {
		this.endpoint = endpoint;
		updateHitboxes();
	}

	@Override
	public void draw(RenderContext context, EditorObject target) {
		if (endpoint.getRenderer().isDirty()) updateHitboxes();
		super.draw(context, target);
	}

	@Override
	public Vector3f getCenter() {
		return endpoint.pos();
	}

	@Override
	public void setNewCenter(Vector3f vector3f) {
		snap(vector3f);
		endpoint.setPos(vector3f);
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
