package de.schoko.vcoasters.client.lines;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.EditorScreen;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.gizmo.LineTranslationGizmo;
import de.schoko.vcoasters.core.EditorObject;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.RenderContext;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.Optional;

public class LineEditorView extends View {
	private Line selectedLine;
	private LineTranslationGizmo gizmo;

	public LineEditorView(EditorScreen screen) {
		super(screen);
	}

	@Override
	public boolean handleAttack() {
		Track track = getScreen().getTrack();
		return TargetTester.consumeClosestTarget(
			TargetTester.consumer(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getRenderer().clip(from, to), i -> {
				Line line = track.getLines().get(i);
				Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
				selectedLine = line;
				gizmo = new LineTranslationGizmo(selectedLine);
			}),
			TargetTester.consumer(gizmo != null ? 6 : 0, (i, from, to) -> gizmo.getHitboxes()[i].clip(from, to), i -> {
				gizmo.setDraggedAxis(LineTranslationGizmo.TRANSLATION_AXIS[i]);
			})
		);
	}

	@Override
	public boolean handleDraggedAttack() {
		Track track = getScreen().getTrack();
		return TargetTester.consumeClosestTarget(
			new TargetTester.ConsumingTargetProvider<>(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getRenderer().clip(from, to), i -> {

			}),
			new TargetTester.ConsumingTargetProvider<>(gizmo != null ? 6 : 0, (i, from, to) -> gizmo.getHitboxes()[i].clip(from, to), i -> {
				//gizmo.setDraggedAxis(TranslationGizmo.TRANSLATION_AXIS[i]);
			})
		);
	}

	@Override
	public void leftMouseReleased() {
		if (gizmo != null) gizmo.release();
	}

	@Override
	public void load() {
		getScreen().getTrack().getLines().add(new Line("0",
			new Vector3f(5, 5, 5),
			new Vector3f(8, 7, 6)
		));
		getScreen().getTrack().getLines().add(new Line("1",
			new Vector3f(8, 7, 6),
			new Vector3f(10, 6, 3)
		));
		getScreen().getTrack().getLines().getFirst().setOutputLine(getScreen().getTrack().getLines().getLast());
	}

	@Override
	public void render(RenderContext renderContext) {
		Track lineManager = getScreen().getTrack();
		EditorObject target;
		Optional<EditorObject> optionalTarget = TargetTester.getClosestTarget(
			TargetTester.provider(
				lineManager.getLines().size(),
				(i, from, to) -> lineManager.getLines().get(i).getRenderer().clip(from, to),
				i -> lineManager.getLines().get(i)),
			TargetTester.provider(gizmo != null ? 6 : 0,
				(i, from, to) -> gizmo.getHitboxes()[i].clip(from, to),
				i -> (EditorObject) gizmo.getHitboxes()[i]));
		target = optionalTarget.orElse(null);

		lineManager.getLines().forEach(line -> {
			line.getRenderer().upload(renderContext, target, selectedLine);
		});
		if (selectedLine != null && gizmo != null) {
			gizmo.draw(renderContext, target);
		}
	}

	@Override
	public void renderImGui(ImGuiIO io) {

	}

	@Override
	public void endClientTick() {

	}
}
