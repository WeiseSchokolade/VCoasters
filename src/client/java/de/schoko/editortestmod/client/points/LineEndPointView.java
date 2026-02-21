package de.schoko.editortestmod.client.points;

import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.client.FollowerCar;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.EditorContext;
import de.schoko.editortestmod.client.core.TargetTester;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.editor.EditorAction;
import de.schoko.editortestmod.client.editor.EditorState;
import de.schoko.editortestmod.client.gizmo.*;
import de.schoko.editortestmod.client.lines.LineManager;
import de.schoko.editortestmod.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.Optional;

public class LineEndPointView extends View {
	private Gizmo gizmo;
	private Point previewPoint;

	private FollowerCar car;

	public boolean select(EditorObject object) {
		if (object != null) gizmo = switch (object) {
			case Line line -> new LineTranslationGizmo(line);
			case Point point -> new PointTranslationGizmo(point);
			case EndPoint endPoint -> (EditorState.endpointRotationMode) ? new EndPointRotationGizmo(endPoint) : new EndPointTranslationGizmo(endPoint);
			default -> null;
		};
		return getContext().setSelectedObject(object);
	}

	@Override
	public boolean handleAttack() {
		LineManager lineManager = getContext().getLineManager();
		Runnable successResponse = (Minecraft.getInstance().player != null) ? () -> Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND) : () -> {};
		boolean handled = TargetTester.consumeClosestTarget(
			TargetTester.consumer(previewPoint != null ? 1 : 0, (index, from, to) -> previewPoint.getAABB().clip(from, to), i -> {
				successResponse.run();
				gizmo = new PointTranslationGizmo(previewPoint);
			}),
			TargetTester.consumer(lineManager.getLineEndpoints().size(), (i, from, to) -> lineManager.getLineEndpoints().get(i).getRenderer().clip(from, to), i -> {
				if (previewPoint != null) cancelPreview();
				EndPoint endpoint = lineManager.getLineEndpoints().get(i);
				if (getContext().getSelectedObject() == endpoint) {
					EditorState.endpointRotationMode = !EditorState.endpointRotationMode;
				}
				select(endpoint);
				successResponse.run();
			}),
			TargetTester.consumer(lineManager.getLines().size(), (i, from, to) -> lineManager.getLines().get(i).getRenderer().clip(from, to), i -> {
				Line line = lineManager.getLines().get(i);
				successResponse.run();
				if (select(line) && previewPoint != null) cancelPreview();
			}),
			TargetTester.consumer(gizmo != null ? gizmo.getHitboxAmount() : 0, (i, from, to) -> gizmo.clip(i, from, to), i -> {
				if (gizmo instanceof TranslationGizmo translationGizmo) {
					translationGizmo.setDraggedAxis(TranslationGizmo.TRANSLATION_AXIS[i]);
				} else if (gizmo instanceof EndPointRotationGizmo endPointRotationGizmo) {
					endPointRotationGizmo.setDraggedClippable(i);
				}
			})
		);
		if (!handled) {
			EditorTestModClient.setDraggingCamera(true);
		}
		return true;
	}

	@Override
	public boolean handleDraggedAttack() {
		return false;
	}

	@Override
	public void leftMouseReleased() {
		if (gizmo != null) gizmo.release();
	}

	@Override
	public void load(EditorContext editorContext) {
		EditorAction.previewNewLinePreviewProvider = this::showPreview;
		EditorAction.createNewLinePreviewProvider = this::createPreviewed;
		EditorAction.cancelNewLinePreviewProvider = this::cancelPreview;

		EditorAction.splitSelectedLineInCenterProvider = this::splitSelectedLineInCenter;
		EditorAction.deleteSelectedLineProvider = this::deleteSelectedLine;

		EditorAction.spawnFollowerCarProvider = this::spawnFollowerCar;
		EditorState.followerCarGetter = () -> car;
		EditorAction.deleteFollowerCarProvider = () -> car = null;

		EditorAction.useTranslationGizmoForCurrentlySelectedEndpointProvider = () -> {
			EditorState.endpointRotationMode = false;
			if (getContext().getSelectedObject() instanceof EndPoint endPoint) select(endPoint);
		};
		EditorAction.useRotationGizmoForCurrentlySelectedEndpointProvider = () -> {
			EditorState.endpointRotationMode = true;
			if (getContext().getSelectedObject() instanceof EndPoint endPoint) select(endPoint);
		};

		EditorAction.resetCurrentlySelectedEndpointRotationProvider = () -> {
			if (getContext().getSelectedObject() instanceof EndPoint endPoint) {
				endPoint.setYaw(0);
				endPoint.setPitch(0);
				endPoint.setRoll(0);
			}
		};
	}

	@Override
	public void upload(RenderContext renderContext) {
		LineManager lineManager = getContext().getLineManager();

		renderContext.drawBoxLine(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f), 0.2f, Colors.WHITE);

		EditorObject target;
		if (getContext().editorActive() && !EditorTestModClient.isDraggingCamera() && (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.isMouseOver(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos()))) {
			Optional<EditorObject> optionalTarget = TargetTester.getClosestTarget(
				TargetTester.provider(
					lineManager.getLineEndpoints().size(),
					(i, from, to) -> lineManager.getLineEndpoints().get(i).getRenderer().clip(from, to),
					i -> lineManager.getLineEndpoints().get(i)),
				TargetTester.provider(
					lineManager.getLines().size(),
					(i, from, to) -> lineManager.getLines().get(i).getRenderer().clip(from, to),
					i -> lineManager.getLines().get(i)),
				TargetTester.provider(gizmo != null ? gizmo.getHitboxAmount() : 0,
					(gizmo != null) ? gizmo::clip : (i, from, to) -> Optional.empty(),
					(gizmo != null) ? gizmo::getEditorObject : i -> null)
			);
			target = optionalTarget.orElse(null);
		} else {
			target = null;
		}

		EditorObject selectedObject = getContext().getSelectedObject();
		lineManager.getLines().forEach(line -> line.getRenderer().upload(renderContext, target, selectedObject));
		lineManager.getLineEndpoints().forEach(endpoint -> endpoint.getRenderer().upload(renderContext, target, selectedObject));

		if (previewPoint != null) {
			previewPoint.draw(renderContext);
		}
		if (car != null) {
			if (EditorState.rideCar != null) {
				EditorState.rideCar.render(renderContext);
			} else {
				car.tick();
				car.draw(renderContext);
			}
		}
		if (selectedObject != null && gizmo != null) {
			gizmo.draw(renderContext, target);
		}
	}

	@Override
	public void render(ImGuiIO io) {
		EditorObject object = getContext().getSelectedObject();
		if (object instanceof Line line) {
			if (ImGui.begin("Line")) {
				ImGui.setWindowSize(200, 400);
				if (ImGui.button("Add")) EditorAction.previewNewLinePreviewProvider.run();
				if (ImGui.button("Split")) EditorAction.splitSelectedLineInCenterProvider.run();
				if (ImGui.button("Delete")) EditorAction.deleteSelectedLineProvider.run();
				if (ImGui.button("Deselect")) getContext().setSelectedObject(null);
				ImGui.separatorText("Train");
				if (ImGui.button("Summon")) EditorAction.spawnFollowerCarProvider.run();
				ImGui.end();
			}
		}

	}

	public void showPreview() {
		EditorObject object = getContext().getSelectedObject();
		if (object instanceof Line line) {
			Vector3f direction = line.getOutputEndPoint().getPos().sub(line.getInputEndPoint().getPos(), new Vector3f());
			direction.normalize();
			previewPoint = new LineExtensionPreviewPoint(line.getOutputEndPoint().pos().add(direction, direction), line, Colors.CYAN);
			select(previewPoint);
		}
		EditorState.isPreviewing = true;
	}

	public void createPreviewed() {
		EditorObject object = getContext().getSelectedObject();
		if (object == previewPoint) {
			Line previousLine = ((LineExtensionPreviewPoint) previewPoint).getLine();
			Line newLine = new Line(previousLine.getOutputEndPoint().getPos(), previewPoint.getPos());
			previousLine.setOutputLine(newLine);
			getContext().getLineManager().addLine(newLine);
			previewPoint = null;
			newLine.getRenderer().setDirty(true);
			select(newLine);
		}
		EditorState.isPreviewing = false;
	}

	public void cancelPreview() {
		Line line = ((LineExtensionPreviewPoint) previewPoint).getLine();
		select(line);
		previewPoint = null;
		EditorState.isPreviewing = false;
	}

	public void splitSelectedLineInCenter() {
		if (getContext().getSelectedObject() instanceof Line line) {
			InterpolatedPoint newCenter = line.lerp(0.5f);
			Line lineA = new Line(Line.getNewRandomId(), line.getInputEndPoint(), newCenter);
			Line lineB = new Line(Line.getNewRandomId(), newCenter, line.getOutputEndPoint());
			if (line.getInputLine() != null) line.getInputLine().setOutputLine(lineA);
			lineA.setOutputLine(lineB);
			lineB.setOutputLine(line.getOutputLine());
			getContext().getLineManager().removeLine(line);
			getContext().getLineManager().addLine(lineA);
			getContext().getLineManager().addLine(lineB);

			select(lineA.getOutputEndPoint());
		}
	}

	public void deleteSelectedLine() {
		if (getContext().getSelectedObject() instanceof Line line) {
			getContext().getLineManager().removeLine(line);
			select(null);
		}
	}

	public void spawnFollowerCar() {
		if (getContext().getSelectedObject() instanceof Line line) {
			car = new FollowerCar(line);
			car.setRenderModel(EditorState.renderModel);
			car.setModel(EditorState.followerCarModel != null ? EditorState.followerCarModel : Minecraft.getInstance().getModelManager().getItemModel(Identifier.fromNamespaceAndPath("editortestmod", "test")));
		} else if (getContext().getSelectedObject() instanceof EndPoint endPoint) {
			car = new FollowerCar(endPoint.getLine());
			car.setRenderModel(EditorState.renderModel);
			car.setModel(EditorState.followerCarModel != null ? EditorState.followerCarModel : Minecraft.getInstance().getModelManager().getItemModel(Identifier.fromNamespaceAndPath("editortestmod", "test")));
			if (endPoint.isOutputEndPoint()) {
				car.setDistanceTravelled(endPoint.getLine().getLength() - 0.00001f);
			}
		}
	}
}
