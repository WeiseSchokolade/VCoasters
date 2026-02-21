package de.schoko.editortestmod.client.gizmo;

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
		endpoint.setPos(vector3f);
		endpoint.updateCorrespondingEndpoint();
	}
}
