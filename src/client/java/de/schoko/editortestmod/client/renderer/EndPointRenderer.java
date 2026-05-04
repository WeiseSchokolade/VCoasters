package de.schoko.editortestmod.client.renderer;

import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.editor.EditorStyle;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.RenderContext;
import de.schoko.editortestmod.core.Renderer;
import imgui.ImGuiIO;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public class EndPointRenderer extends Renderer<EndPoint> {
	private AABB hitbox;

	public EndPointRenderer(EndPoint endPoint) {
		super(endPoint);
	}

	@Override
	public void updateHitbox(EndPoint object) {
		Vector3f pos = object.pos();
		this.hitbox = new AABB(pos.x - EditorStyle.END_POINT_RADIUS, pos.y - EditorStyle.END_POINT_RADIUS, pos.z - EditorStyle.END_POINT_RADIUS, pos.x + EditorStyle.END_POINT_RADIUS, pos.y + EditorStyle.END_POINT_RADIUS, pos.z + EditorStyle.END_POINT_RADIUS);
	}

	@Override
	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		if (isSameAsCorrespondingEndpoint() && getObject().isOutputEndPoint()) return Optional.empty();
		return hitbox.clip(from, to);
	}

	@Override
	public void renderImGui(ImGuiIO io) {

	}

	public void upload(RenderContext context, EditorObject target, EditorObject selected) {
		Vector4f lightColor;
		Vector4f baseColor;
		if (getObject().isOutputEndPoint()) {
			lightColor = Colors.LIGHT_BLUE;
			baseColor = Colors.BLUE;
		} else {
			lightColor = Colors.LIGHT_RED;
			baseColor = Colors.RED;
		}
		if (isSameAsCorrespondingEndpoint()) {
			if (getObject().isOutputEndPoint()) {
				return;
			}
			lightColor = Colors.LIGHT_YELLOW;
			baseColor = Colors.YELLOW;
		}

		Vector4f color = (isRendered(target)) ? Colors.WHITE : (isRendered(selected)) ? lightColor : baseColor;
		context.drawAABB(hitbox, color);
	}

	public boolean isSameAsCorrespondingEndpoint() {
		return getObject().getCorrespondingEndpoint() != null && getObject().equalsCorrespondingEndpoint();
	}
}
