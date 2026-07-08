package de.schoko.editortestmod.client.points;

import de.schoko.editortestmod.client.EditorScreen;
import de.schoko.editortestmod.client.core.TargetTester;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.gizmo.PointTranslationGizmo;
import de.schoko.editortestmod.core.RenderContext;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import org.joml.Vector4f;

public class TestView extends View {
	private final PointManager pointManager;
	private Point selectedPoint;
	private PointTranslationGizmo gizmo;

	public TestView(EditorScreen screen, PointManager pointManager) {
		super(screen);
		this.pointManager = pointManager;
	}

	@Override
	public void load() {

	}

	@Override
	public boolean handleAttack() {
		return TargetTester.consumeClosestTarget(
			new TargetTester.ConsumingTargetProvider<>(pointManager.getBoxes().size(), (i, from, to) -> pointManager.getBoxes().get(i).getAABB().clip(from, to), i -> {
				Point point = pointManager.getBoxes().get(i);
				Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
				selectedPoint = point;
				gizmo = new PointTranslationGizmo(selectedPoint);
			}),
			new TargetTester.ConsumingTargetProvider<>(gizmo != null ? 6 : 0, (i, from, to) -> gizmo.getHitboxes()[i].clip(from, to), i -> {
				gizmo.setDraggedAxis(PointTranslationGizmo.TRANSLATION_AXIS[i]);
			})
		);
	}

	@Override
	public boolean handleDraggedAttack() {
		return TargetTester.consumeClosestTarget(
			new TargetTester.ConsumingTargetProvider<>(pointManager.getBoxes().size(), (i, from, to) -> pointManager.getBoxes().get(i).getAABB().clip(from, to), i -> {

			}),
			new TargetTester.ConsumingTargetProvider<>(gizmo != null ? 6 : 0, (i, from, to) -> gizmo.getHitboxes()[i].clip(from, to), i -> {
				//gizmo.setDraggedAxis(TranslationGizmo.TRANSLATION_AXIS[i]);
			})
		);
	}

	@Override
	public void leftMouseReleased() {

	}

	@Override
	public void render(RenderContext renderContext) {
		Point targetedPoint = pointManager.getTargetedPoint();
		pointManager.getBoxes().forEach(point -> {
			AABB aabb = point.getAABB();
			Vector4f color = new Vector4f(point.getColor());
			if (point == targetedPoint) {
				color.lerp(new Vector4f(1f), 0.75f);
			}
			if (point == selectedPoint) {
				color.lerp(new Vector4f(1f), 0.5f);
			}
			point.draw(renderContext);

			renderContext.drawAABox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color);
		});
		if (selectedPoint != null && gizmo != null) {
			//gizmo.draw(renderContext);
		}
	}

	@Override
	public void renderImGui(ImGuiIO io) {

	}

	@Override
	public void endClientTick() {

	}
}
