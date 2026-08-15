package de.schoko.vcoasters.client.gizmo;

import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.hitboxes.Clippable;
import de.schoko.vcoasters.client.core.hitboxes.LineRing;
import de.schoko.vcoasters.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public class EndPointRotationGizmo implements Gizmo {
	private static final float ROTATION_HANDLE_DISTANCE = 0.5f;

	private static final float HALF_DIAMETER = 0.05f;
	private static final float LENGTH = 1f;
	private static final float OFFSET = 0.4f;

	private EndPoint endpoint;
	private Clippable[] clippables;
	private Clippable draggedClippable;

	private int draggedIndex;

	private ValuePoint dragOriginal;
	private Vector3f dragOffset;

	public EndPointRotationGizmo(EndPoint endpoint) {
		this.endpoint = endpoint;
		clippables = new Clippable[4];
		updateHitboxes();
	}

	@Override
	public void draw(RenderContext renderContext, EditorObject target) {
		if (endpoint.getComponent(DirtContainer.class).isDirty()) {
			updateHitboxes();
		}
		if (draggedClippable instanceof AABB aabb) {
			Vector3f eyePosition = Minecraft.getInstance().player.getEyePosition().toVector3f();
			Vector3f direction = TargetTester.getMouseViewDirection().toVector3f();

			float[] intersection = Geometry.getLineSphereIntersection(eyePosition, direction, endpoint.getPos(), ROTATION_HANDLE_DISTANCE);
			Vector3f closest = getClosestAlongLine(eyePosition, direction, intersection);

			if (closest != null) {
				Vector3f targetDirection = closest.sub(endpoint.getPos());
				endpoint.setYaw((float) Math.atan2(targetDirection.x, targetDirection.z));
				endpoint.setPitch((float) Math.asin(targetDirection.y / ROTATION_HANDLE_DISTANCE));
			}
			updateHitboxes();
			endpoint.updateCorrespondingEndpoint();
			endpoint.markRendererAsDirty();
		}
		if (draggedClippable instanceof LineRing ring) {
			Vector3f newPoint = ring.intersectionOnPlane(TargetTester.getCurrentFrom(), TargetTester.getCurrentTo(1f)).get().toVector3f();
			if (draggedIndex == 1) {
				float dPitch = (float) (-Math.atan2(newPoint.y - endpoint.y(), newPoint.z() - endpoint.z()) + Math.atan2(dragOffset.y - endpoint.y(), dragOffset.z() - endpoint.z()));
				if (dPitch < -0.5 * Math.PI) dPitch += (float) (2 * Math.PI);
				if (dPitch > 0.5 * Math.PI) dPitch -= (float) (2 * Math.PI);
				endpoint.setPitch(dPitch + dragOriginal.getPitch());
				dragOffset = newPoint;
				dragOriginal = endpoint.valueCopy();
			} else if (draggedIndex == 2) {
				float dYaw = (float) (-Math.atan2(newPoint.x - endpoint.x(), newPoint.z() - endpoint.z()) + Math.atan2(dragOffset.x - endpoint.x(), dragOffset.z() - endpoint.z()));
				if (dYaw < -0.5 * Math.PI) dYaw += (float) (2 * Math.PI);
				if (dYaw > 0.5 * Math.PI) dYaw -= (float) (2 * Math.PI);
				endpoint.setYaw(dYaw + dragOriginal.getYaw());
				dragOffset = newPoint;
				dragOriginal = endpoint.valueCopy();
			} else if (draggedIndex == 3) {
				float dRoll = (float) (Math.atan2(newPoint.y - endpoint.y(), newPoint.x() - endpoint.x()) - Math.atan2(dragOffset.y - endpoint.y(), dragOffset.x() - endpoint.x()));
				if (dRoll < -0.5 * Math.PI) dRoll += (float) (2 * Math.PI);
				if (dRoll > 0.5 * Math.PI) dRoll -= (float) (2 * Math.PI);
				endpoint.setRoll(dRoll + dragOriginal.getRoll());
				dragOffset = newPoint;
				dragOriginal = endpoint.valueCopy();
			}
			updateHitboxes();
			endpoint.updateCorrespondingEndpoint();
			endpoint.markRendererAsDirty();
		}
		VCoastersClient.addDebugString("Yaw", Math.round(Math.toDegrees(endpoint.getYaw())));
		VCoastersClient.addDebugString("Pitch", Math.round(Math.toDegrees(endpoint.getPitch())));
		VCoastersClient.addDebugString("Roll", Math.round(Math.toDegrees(endpoint.getRoll())));

		clippables[0].draw(renderContext, target == clippables[0] ? Colors.WHITE : Colors.YELLOW);
		//clippables[1].draw(renderContext, target == clippables[1] ? Colors.WHITE : Colors.BLUE);

		clippables[1].draw(renderContext, target == clippables[1] ? Colors.WHITE : Colors.RED);
		clippables[2].draw(renderContext, target == clippables[2] ? Colors.WHITE : Colors.GREEN);
		clippables[3].draw(renderContext, target == clippables[3] ? Colors.WHITE : Colors.BLUE);

		/*if (draggedAxis == null) return;
		Vector3f center = endpoint.getPos();
		switch (draggedAxis) {
			case X:
				renderContext.drawAABox(center.x - AXIS_HALF_LENGTH, center.y - AXIS_HALF_DIAMETER, center.z - AXIS_HALF_DIAMETER, center.x + AXIS_HALF_LENGTH, center.y + AXIS_HALF_DIAMETER, center.z + AXIS_HALF_DIAMETER, Colors.RED);
				break;
			case Y:
				renderContext.drawAABox(center.x - AXIS_HALF_DIAMETER, center.y - AXIS_HALF_LENGTH, center.z - AXIS_HALF_DIAMETER, center.x + AXIS_HALF_DIAMETER, center.y + AXIS_HALF_LENGTH, center.z + AXIS_HALF_DIAMETER, Colors.GREEN);
				break;
			case Z:
				renderContext.drawAABox(center.x - AXIS_HALF_DIAMETER, center.y - AXIS_HALF_DIAMETER, center.z - AXIS_HALF_LENGTH, center.x + AXIS_HALF_DIAMETER, center.y + AXIS_HALF_DIAMETER, center.z + AXIS_HALF_LENGTH, Colors.BLUE);
				break;
		}*/
	}

	@Override
	public Optional<Vec3> clip(int i, Vec3 from, Vec3 to) {
		return clippables[i].clip(from, to);
	}

	public void updateHitboxes() {
		Vector3f center = endpoint.getPos();
		Vector3f direction = endpoint.getRotatedViewDirection(ROTATION_HANDLE_DISTANCE);
		Vector3f yawPitchCenter = center.add(direction, new Vector3f());
		clippables[0] = (Clippable) new AABB(yawPitchCenter.x - HALF_DIAMETER, yawPitchCenter.y - HALF_DIAMETER, yawPitchCenter.z - HALF_DIAMETER, yawPitchCenter.x + HALF_DIAMETER, yawPitchCenter.y + HALF_DIAMETER, yawPitchCenter.z + HALF_DIAMETER);

		Vector3f right = direction.cross(new Vector3f(0, -1, 0), new Vector3f());
		Vector3f up = direction.cross(right, new Vector3f());

		//clippables[1] = new LineRing(20, center, up.normalize(1f), right.normalize(1f));

		clippables[1] = new LineRing(20, center, new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, 1f), 1f);
		clippables[2] = new LineRing(20, center, new Vector3f(1f, 0f, 0f), new Vector3f(0f, 0f, 1f), 1f);
		clippables[3] = new LineRing(20, center, new Vector3f(0f, 1f, 0f), new Vector3f(1f, 0f, 0f), 1f);
	}

	@Override
	public void release() {
		draggedClippable = null;
		draggedIndex = -1;
		endpoint.markRendererAsDirty();
	}

	@Override
	public EditorObject getEditorObject(int index) {
		return clippables[index];
	}

	@Override
	public int getHitboxAmount() {
		return clippables.length;
	}

	public void setDraggedClippable(int i) {
		draggedClippable = clippables[i];
		draggedIndex = i;
		if (draggedClippable instanceof LineRing ring) {
			dragOffset = ring.intersectionOnPlane(TargetTester.getCurrentFrom(), TargetTester.getCurrentTo(1f)).get().toVector3f();
		}
		dragOriginal = endpoint.valueCopy();
	}

	private Vector3f getClosestAlongLine(Vector3f eyePosition, Vector3f direction, float[] intersection) {
		if (Float.isFinite(intersection[0])) {
			if (Float.isFinite(intersection[1])) {
				Vector3f a = Geometry.getPointAlongLine(eyePosition, intersection[0], direction);
				Vector3f b = Geometry.getPointAlongLine(eyePosition, intersection[1], direction);

				return a.distanceSquared(eyePosition) < b.distanceSquared(eyePosition) ? a : b;
			} else {
				return Geometry.getPointAlongLine(eyePosition, intersection[0], direction);
			}
		} else {
			if (Float.isFinite(intersection[1])) {
				return Geometry.getPointAlongLine(eyePosition, intersection[1], direction);
			} else {
				return null;
			}
		}
	}
}
