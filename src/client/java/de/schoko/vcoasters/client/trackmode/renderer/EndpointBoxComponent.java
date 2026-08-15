package de.schoko.vcoasters.client.trackmode.renderer;

import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.editor.EditorOptions;
import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.core.EditorComponent;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.EndPoint;
import de.schoko.vcoasters.core.RenderContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public class EndpointBoxComponent implements EditorComponent {
	private final EndPoint endpoint;
	private AABB hitbox;

	public EndpointBoxComponent(EndPoint endpoint) {
		this.endpoint = endpoint;
		updateHitbox();
	}

	public void updateHitbox() {
		Vector3f pos = endpoint.pos();
		this.hitbox = new AABB(pos.x - EditorStyle.END_POINT_RADIUS, pos.y - EditorStyle.END_POINT_RADIUS, pos.z - EditorStyle.END_POINT_RADIUS, pos.x + EditorStyle.END_POINT_RADIUS, pos.y + EditorStyle.END_POINT_RADIUS, pos.z + EditorStyle.END_POINT_RADIUS);
	}

	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		if (isSameAsCorrespondingEndpoint() && endpoint.isOutputEndPoint()) return Optional.empty();
		return hitbox.clip(from, to);
	}

	public void upload(RenderContext context, boolean isTarget, boolean isSelected) {
		Vector4f lightColor;
		Vector4f baseColor;
		if (endpoint.isOutputEndPoint()) {
			baseColor = Colors.BLUE;
			lightColor = Colors.LIGHT_BLUE;
		} else {
			baseColor = Colors.RED;
			lightColor = Colors.LIGHT_RED;
		}
		if (isSameAsCorrespondingEndpoint()) {
			if (endpoint.isOutputEndPoint()) {
				return;
			}
			baseColor = Colors.YELLOW;
			lightColor = Colors.LIGHT_YELLOW;
			if (EditorOptions.showAngleSharpness) {
				baseColor = Colors.GREEN;
				float dot = endpoint.getLine().getDirection(1).dot(endpoint.getLine().getInputLine().getDirection(1));
				float t = (float) Math.clamp(Math.pow(dot, 8), 0, 1);
				baseColor = baseColor.lerp(Colors.RED, 1 - t, new Vector4f());
				lightColor = baseColor.add(0.25f, 0.25f, 0.25f, 0.0f).min(Colors.WHITE);
			}

		}

		Vector4f color = isTarget ? Colors.WHITE : isSelected ? lightColor : baseColor;
		context.drawAABB(hitbox, color);

		if (EditorOptions.showRollAngle) {
			Vector3f direction = endpoint.getRotatedViewDirection(1);
			Vector3f rollVector = direction.cross(new Vector3f(0f, 1f, 0f), new Vector3f()).cross(direction).rotateAxis(endpoint.getRoll(), direction.x, direction.y, direction.z).normalize(0.5f);
			//Vector3f rollAxis = Geometry.applyRotation(new Vector3f(0f, 0f, 1f), endpoint.getYaw(), endpoint.getPitch(), 0);
			//Vector3f rollVector = new Vector3f(0f, 0.5f, 0f).rotateAxis(endpoint.getRoll(), rollAxis.x, rollAxis.y, rollAxis.z);
			context.drawBoxLine(hitbox.getCenter().toVector3f(), hitbox.getCenter().toVector3f().add(rollVector), 0.05f, Colors.BLUE);
		}
	}

	public boolean isSameAsCorrespondingEndpoint() {
		return endpoint.getCorrespondingEndpoint() != null && endpoint.equalsCorrespondingEndpoint();
	}

}
