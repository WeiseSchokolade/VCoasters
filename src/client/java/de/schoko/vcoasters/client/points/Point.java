package de.schoko.vcoasters.client.points;

import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.RenderContext;
import de.schoko.vcoasters.core.ValuePoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Point implements EditorObject, ValuePoint {

	private final Vector3f pos;
	private float yaw, pitch, roll;

	private AABB hitbox;
	private final Vector4f color;

	public Point(Vector3f pos, Vector4f color) {
		this.pos = pos;
		this.color = color;
		this.updateHitbox();
	}

	public void draw(RenderContext context) {
		context.drawRotatedBox(new Vector3f(pos).sub(0.05f, 0.05f, 0.05f), yaw, pitch, roll, 0.1f, 0.3f, 0.1f, new Vector4f(0f, 0f, 1f, 1f));
	}

	public void updateHitbox() {
		hitbox = new AABB(pos.x - EditorStyle.END_POINT_RADIUS, pos.y - EditorStyle.END_POINT_RADIUS, pos.z - EditorStyle.END_POINT_RADIUS, pos.x + EditorStyle.END_POINT_RADIUS, pos.y + EditorStyle.END_POINT_RADIUS, pos.z + EditorStyle.END_POINT_RADIUS);
	}

	public Vector4f getColor() {
		return color;
	}

	public AABB getAABB() {
		return hitbox;
	}

	public Vector3f getPos() {
		return pos;
	}

	public boolean isBeingLookedAt() {
		LocalPlayer player = Minecraft.getInstance().player;
		Vec3 from = player.getEyePosition();
		Vec3 direction = player.getViewVector(1);
		double entityInteractionRange = player.entityInteractionRange();
		Vec3 to = from.add(direction.scale(entityInteractionRange));
		return hitbox.clip(from, to).isPresent();
	}

	@Override
	public float x() {
		return pos.x;
	}

	@Override
	public float y() {
		return pos.y;
	}

	@Override
	public float z() {
		return pos.z;
	}

	@Override
	public float yaw() {
		return yaw;
	}

	@Override
	public float pitch() {
		return pitch;
	}

	@Override
	public float roll() {
		return roll;
	}
}
