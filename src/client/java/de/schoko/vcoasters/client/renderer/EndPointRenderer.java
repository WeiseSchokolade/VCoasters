package de.schoko.vcoasters.client.renderer;

import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.editor.EditorOptions;
import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.core.*;
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
			baseColor = Colors.BLUE;
			lightColor = Colors.LIGHT_BLUE;
		} else {
			baseColor = Colors.RED;
			lightColor = Colors.LIGHT_RED;
		}
		if (isSameAsCorrespondingEndpoint()) {
			if (getObject().isOutputEndPoint()) {
				return;
			}
			baseColor = Colors.YELLOW;
			lightColor = Colors.LIGHT_YELLOW;
			if (EditorOptions.showAngleSharpness) {
				baseColor = Colors.GREEN;
				float dot = getObject().getLine().getDirection(1).dot(getObject().getLine().getInputLine().getDirection(1));
				float t = (float) Math.clamp(Math.pow(dot, 8), 0, 1);
				baseColor = baseColor.lerp(Colors.RED, 1 - t, new Vector4f());
				lightColor = baseColor.add(0.25f, 0.25f, 0.25f, 0.0f).min(Colors.WHITE);
			}

		}

		Vector4f color = (isRendered(target)) ? Colors.WHITE : (isRendered(selected)) ? lightColor : baseColor;
		context.drawAABB(hitbox, color);

		if (EditorOptions.showRollAngle) {
			Vector3f direction = getObject().getRotatedViewDirection(1);
			Vector3f rollVector = direction.cross(new Vector3f(0f, 1f, 0f), new Vector3f()).cross(direction).rotateAxis(getObject().getRoll(), direction.x, direction.y, direction.z).normalize(0.5f);
			//Vector3f rollAxis = Geometry.applyRotation(new Vector3f(0f, 0f, 1f), getObject().getYaw(), getObject().getPitch(), 0);
			//Vector3f rollVector = new Vector3f(0f, 0.5f, 0f).rotateAxis(getObject().getRoll(), rollAxis.x, rollAxis.y, rollAxis.z);
			context.drawBoxLine(hitbox.getCenter().toVector3f(), hitbox.getCenter().toVector3f().add(rollVector), 0.05f, Colors.BLUE);
		}
	}

	public boolean isSameAsCorrespondingEndpoint() {
		return getObject().getCorrespondingEndpoint() != null && getObject().equalsCorrespondingEndpoint();
	}
}
