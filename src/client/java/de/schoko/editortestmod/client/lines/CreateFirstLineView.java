package de.schoko.editortestmod.client.lines;

import de.schoko.editortestmod.client.EditorScreen;
import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.TargetTester;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.editor.EditorStyle;
import de.schoko.editortestmod.client.points.LineEndPointView;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.RenderContext;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CreateFirstLineView extends View {

	private Vector3f a;
	private Vector3f b;

	public CreateFirstLineView(EditorScreen screen) {
		super(screen);
	}

	@Override
	public void load() {

	}

	@Override
	public void render(RenderContext renderContext) {
		Vec3 direction = TargetTester.getPlayerViewDirection();
		Vec3 eyePosition = TargetTester.getEyePosition();
		Vec3 off = direction.cross(new Vec3(0, 1, 0)).normalize();

		Vec3 lineCenter = eyePosition.add(direction.scale(4));
		a = lineCenter.subtract(off).toVector3f();
		b = lineCenter.add(off).toVector3f();

		EditorTestModClient.addDebugString("A", a);
		EditorTestModClient.addDebugString("B", b);
		renderContext.drawBoxLine(a, b, EditorStyle.TRACK_LINE_WIDTH, Colors.CYAN);
		renderContext.drawBoxPoint(a, EditorStyle.END_POINT_RADIUS, Colors.RED);
		renderContext.drawBoxPoint(b, EditorStyle.END_POINT_RADIUS, Colors.BLUE);
	}

	@Override
	public void render(ImGuiIO io) {
		if (ImGui.begin("Preview")) {
			if (ImGui.button("Create")) {
				Line line = new Line(Line.getNewRandomId(), a, b);
				line.getRenderer().setDirty(true);
				getScreen().getTrack().getLines().add(line);
				getScreen().setView(new LineEndPointView(getScreen()));
			}
		}
		ImGui.end();
	}

	@Override
	public boolean handleAttack() {
		EditorTestModClient.setDraggingCamera(true);
		return true;
	}

	@Override
	public boolean handleDraggedAttack() {
		return false;
	}

	@Override
	public void leftMouseReleased() {

	}

	@Override
	public void endClientTick() {

	}
}
