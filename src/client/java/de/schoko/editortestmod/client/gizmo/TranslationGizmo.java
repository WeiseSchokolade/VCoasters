package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.RenderContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class TranslationGizmo implements Gizmo {
	public static final TranslationAxis[] TRANSLATION_AXIS = {TranslationAxis.X, TranslationAxis.X, TranslationAxis.Y, TranslationAxis.Y, TranslationAxis.Z, TranslationAxis.Z};

	private static final float HALF_DIAMETER = 0.075f;
	private static final float LENGTH = 1f;
	private static final float OFFSET = 0.4f;

	private static final float AXIS_HALF_DIAMETER = 0.01f;
	private static final float AXIS_HALF_LENGTH = 160;

	private AABB posX, negX;
	private AABB posY, negY;
	private AABB posZ, negZ;
	private AABB[] hitboxes;
	private TranslationAxis draggedAxis;

	private Vector3f dragOffset;

	public TranslationGizmo() {
		hitboxes = new AABB[6];
	}

	@Override
	public void draw(RenderContext context, EditorObject target) {
		if (hitboxes[0] == null) updateHitboxes();

		if (draggedAxis != null) {
			Vector3f point = draggedAxis.getIntersection(getCenter());
			setNewCenter(point.sub(dragOffset));
			updateHitboxes();
		}

		context.drawAABB(negX, target == negX ? Colors.WHITE : Colors.RED);
		context.drawAABB(posX, target == posX ? Colors.WHITE : Colors.RED);
		context.drawAABB(negY, target == negY ? Colors.WHITE : Colors.GREEN);
		context.drawAABB(posY, target == posY ? Colors.WHITE : Colors.GREEN);
		context.drawAABB(negZ, target == negZ ? Colors.WHITE : Colors.BLUE);
		context.drawAABB(posZ, target == posZ ? Colors.WHITE : Colors.BLUE);

		if (draggedAxis == null) return;
		Vector3f center = getCenter();
		switch (draggedAxis) {
			case X:
				context.drawAABox(center.x - AXIS_HALF_LENGTH, center.y - AXIS_HALF_DIAMETER, center.z - AXIS_HALF_DIAMETER, center.x + AXIS_HALF_LENGTH, center.y + AXIS_HALF_DIAMETER, center.z + AXIS_HALF_DIAMETER, Colors.RED);
				break;
			case Y:
				context.drawAABox(center.x - AXIS_HALF_DIAMETER, center.y - AXIS_HALF_LENGTH, center.z - AXIS_HALF_DIAMETER, center.x + AXIS_HALF_DIAMETER, center.y + AXIS_HALF_LENGTH, center.z + AXIS_HALF_DIAMETER, Colors.GREEN);
				break;
			case Z:
				context.drawAABox(center.x - AXIS_HALF_DIAMETER, center.y - AXIS_HALF_DIAMETER, center.z - AXIS_HALF_LENGTH, center.x + AXIS_HALF_DIAMETER, center.y + AXIS_HALF_DIAMETER, center.z + AXIS_HALF_LENGTH, Colors.BLUE);
				break;
		}
	}

	public abstract void setNewCenter(Vector3f vector3f);
	public abstract Vector3f getCenter();

	public void updateHitboxes() {
		Vector3f center = getCenter();
		hitboxes[0] = negX = new AABB(center.x - OFFSET - LENGTH, center.y - HALF_DIAMETER, center.z - HALF_DIAMETER, center.x - OFFSET, center.y + HALF_DIAMETER, center.z + HALF_DIAMETER);
		hitboxes[1] = posX = new AABB(center.x + OFFSET, center.y - HALF_DIAMETER, center.z - HALF_DIAMETER, center.x + OFFSET + LENGTH, center.y + HALF_DIAMETER, center.z + HALF_DIAMETER);
		hitboxes[2] = negY = new AABB(center.x - HALF_DIAMETER, center.y - OFFSET - LENGTH, center.z - HALF_DIAMETER, center.x + HALF_DIAMETER, center.y - OFFSET, center.z + HALF_DIAMETER);
		hitboxes[3] = posY = new AABB(center.x - HALF_DIAMETER, center.y + OFFSET, center.z - HALF_DIAMETER, center.x + HALF_DIAMETER, center.y + OFFSET + LENGTH, center.z + HALF_DIAMETER);
		hitboxes[4] = negZ = new AABB(center.x - HALF_DIAMETER, center.y - HALF_DIAMETER, center.z - OFFSET - LENGTH, center.x + HALF_DIAMETER, center.y + HALF_DIAMETER, center.z - OFFSET);
		hitboxes[5] = posZ = new AABB(center.x - HALF_DIAMETER, center.y - HALF_DIAMETER, center.z + OFFSET, center.x + HALF_DIAMETER, center.y + HALF_DIAMETER, center.z + OFFSET + LENGTH);
	}

	@Override
	public void release() {
		setDraggedAxis(null);
	}

	public void setDraggedAxis(TranslationAxis draggedAxis) {
		this.draggedAxis = draggedAxis;
		if (draggedAxis == null) return;
		dragOffset = draggedAxis.getIntersection(getCenter()).sub(getCenter());
	}

	@Override
	public Optional<Vec3> clip(int i, Vec3 from, Vec3 to) {
		return hitboxes[i].clip(from, to);
	}

	public AABB[] getHitboxes() {
		return hitboxes;
	}

	@Override
	public int getHitboxAmount() {
		return hitboxes.length;
	}

	@Override
	public EditorObject getEditorObject(int index) {
		return (EditorObject) hitboxes[index];
	}
}
