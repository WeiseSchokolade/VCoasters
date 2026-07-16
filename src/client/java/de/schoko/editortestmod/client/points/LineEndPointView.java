package de.schoko.editortestmod.client.points;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.*;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.TargetTester;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.editor.EditorCommands;
import de.schoko.editortestmod.client.editor.EditorOptions;
import de.schoko.editortestmod.client.gizmo.*;
import de.schoko.editortestmod.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class LineEndPointView extends View {
	private Gizmo gizmo;
	private Point previewPoint;

	private boolean useEndpointRotationGizmo;
	private boolean isPreviewing;

	private ImInt selectedComboItem;

	public LineEndPointView(EditorScreen screen) {
		super(screen);
		this.selectedComboItem = new ImInt();
	}

	public boolean select(EditorObject object) {
		if (object != null) gizmo = switch (object) {
			case Line line -> new LineTranslationGizmo(line);
			case Point point -> new PointTranslationGizmo(point);
			case EndPoint endPoint -> (useEndpointRotationGizmo) ? new EndPointRotationGizmo(endPoint) : new EndPointTranslationGizmo(endPoint);
			default -> null;
		};
		if (getScreen().getSelectedObject() == object) return false;
		getScreen().setSelectedObject(object);
		return true;
	}

	@Override
	public boolean handleAttack() {
		Track track = getScreen().getTrack();
		Runnable successResponse = (Minecraft.getInstance().player != null) ? () -> Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND) : () -> {};
		boolean handled = TargetTester.consumeClosestTarget(
			TargetTester.consumer(previewPoint != null ? 1 : 0, (index, from, to) -> previewPoint.getAABB().clip(from, to), i -> {
				successResponse.run();
				gizmo = new PointTranslationGizmo(previewPoint);
			}),
			TargetTester.consumer(track.getLines().size() * 2, (i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getRenderer().clip(from, to), i -> {
				if (previewPoint != null) cancelPreview();
				EndPoint endpoint = (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint());
				if (getScreen().getSelectedObject() == endpoint) {
					useEndpointRotationGizmo = !useEndpointRotationGizmo;
				}
				select(endpoint);
				successResponse.run();
			}),
			TargetTester.consumer(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getRenderer().clip(from, to), i -> {
				Line line = track.getLines().get(i);
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
		if (handled) return true;
		return false;
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
	public void load() {

	}

	@Override
	public void render(RenderContext renderContext) {
		Track track = getScreen().getTrack();

		//renderContext.drawBoxLine(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f), 0.2f, Colors.WHITE);

		EditorObject target;
		if (!EditorTestModClient.isDraggingCamera() && (Minecraft.getInstance().gui.screen() == null || !Minecraft.getInstance().gui.screen().isMouseOver(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos()))) {
			Optional<EditorObject> optionalTarget = TargetTester.getClosestTarget(
				TargetTester.provider(
					track.getLines().size() * 2,
					(i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getRenderer().clip(from, to),
					i -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint())),
				TargetTester.provider(
					track.getLines().size(),
					(i, from, to) -> track.getLines().get(i).getRenderer().clip(from, to),
					i -> track.getLines().get(i)),
				TargetTester.provider(gizmo != null ? gizmo.getHitboxAmount() : 0,
					(gizmo != null) ? gizmo::clip : (i, from, to) -> Optional.empty(),
					(gizmo != null) ? gizmo::getEditorObject : i -> null)
			);
			target = optionalTarget.orElse(null);
		} else {
			target = null;
		}

		EditorObject selectedObject = getScreen().getSelectedObject();
		track.getLines().forEach(line -> {
			line.getRenderer().upload(renderContext, target, selectedObject);
			line.getInputEndPoint().getRenderer().upload(renderContext, target, selectedObject);
			line.getOutputEndPoint().getRenderer().upload(renderContext, target, selectedObject);
		});

		if (previewPoint != null) {
			previewPoint.draw(renderContext);
		}
		if (selectedObject != null && gizmo != null) {
			if (EditorOptions.autoSnap) {
				if (selectedObject instanceof EndPoint endPoint) {
					if (endPoint.equalsCorrespondingEndpoint()) {
						gizmo.draw(renderContext, target);
						if (endPoint.getRenderer().isDirty()) {
							endPoint.updateCorrespondingEndpoint();
						}
					} else {
						gizmo.draw(renderContext, target);
					}
				} else if (selectedObject instanceof Line line) {
					boolean inputEquals = line.getInputEndPoint().equalsCorrespondingEndpoint();
					boolean outputEquals = line.getOutputEndPoint().equalsCorrespondingEndpoint();
					gizmo.draw(renderContext, target);
					if (line.getRenderer().isDirty()) {
						if (inputEquals) line.getInputEndPoint().updateCorrespondingEndpoint();
						if (outputEquals) line.getOutputEndPoint().updateCorrespondingEndpoint();
					}
				} else {
					gizmo.draw(renderContext, target);
				}
			} else {
				gizmo.draw(renderContext, target);
			}
		}
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		EditorObject object = getScreen().getSelectedObject();
		if (isPreviewing) {
			if (ImGui.begin("Line preview")) {
				if (ImGui.button("Create")) {
					this.createPreviewed();
				}
				if (ImGui.button("Cancel")) {
					this.cancelPreview();
				}
			}
			ImGui.end();
		}
		if (ImGui.begin("Builder")) {
			List<Line> lines = getScreen().getTrack().getLabelledLines();
			String[] array = lines.stream().map(Line::getLabel).toArray(String[]::new);
			ImGui.beginDisabled(array.length == 0);

			if (ImGui.button("Select")) {
				select(lines.get(selectedComboItem.get()));
			}
			ImGui.sameLine();
			if (array.length > 0) ImGui.combo("##LabelledSelectionSelect", selectedComboItem, array);
			ImGui.endDisabled();

			if (object instanceof Line line) {
				if (ImGui.button("Input point")) select(line.getInputEndPoint());
				ImGui.sameLine();
				ImGui.beginDisabled(line.getInputEndPoint().getCorrespondingEndpoint() == null);
				if (ImGui.button("Previous")) select(line.getInputEndPoint().getCorrespondingEndpoint().getLine());
				ImGui.endDisabled();
				ImGui.sameLine();
				ImGui.beginDisabled(line.getOutputEndPoint().getCorrespondingEndpoint() == null);
				if (ImGui.button("Next")) select(line.getOutputEndPoint().getCorrespondingEndpoint().getLine());
				ImGui.endDisabled();
				ImGui.sameLine();
				if (ImGui.button("Output point")) select(line.getOutputEndPoint());

				if (ImGui.button("Add")) this.showPreview(line);
				ImGui.sameLine();
				if (ImGui.button("Split")) splitSelectedLineInCenter(line);
				ImGui.sameLine();
				if (ImGui.button("Delete")) deleteSelectedLine(line);
				ImGui.sameLine();
				if (ImGui.button("Deselect")) select(null);

			}
			if (object instanceof EndPoint endPoint) {
				if (ImGui.button("Select line")) select(endPoint.getLine());
				ImGui.sameLine();
				ImGui.beginDisabled(endPoint.getCorrespondingEndpoint() == null);
				if (ImGui.button("Select corresponding")) select(endPoint.getCorrespondingEndpoint());
				ImGui.endDisabled();

				if (ImGui.radioButton("Translate", !useEndpointRotationGizmo)) {
					useEndpointRotationGizmo = false;
					select(endPoint);
				}
				ImGui.sameLine();
				if (ImGui.radioButton("Rotate", useEndpointRotationGizmo)) {
					useEndpointRotationGizmo = true;
					select(endPoint);
				}

				ImBoolean showAngleSharpness = new ImBoolean(EditorOptions.showAngleSharpness);
				if (ImGui.checkbox("Show angle sharpness", showAngleSharpness)) {
					EditorOptions.showAngleSharpness = showAngleSharpness.get();
				}
				ImBoolean showRollAngle = new ImBoolean(EditorOptions.showRollAngle);
				if (ImGui.checkbox("Show roll angle", showRollAngle)) {
					EditorOptions.showRollAngle = showRollAngle.get();
				}
			}
			ImBoolean autoSnap = new ImBoolean(EditorOptions.autoSnap);
			if (ImGui.checkbox("Auto-Snap", autoSnap)) {
				EditorOptions.autoSnap = autoSnap.get();
			}
			ImInt entityInteractionDistance = new ImInt(EditorOptions.interactionRange);
			ImGui.text("Range: ");
			ImGui.sameLine();
			if (ImGui.inputInt("##Range", entityInteractionDistance)) {
				EditorOptions.interactionRange = entityInteractionDistance.get();
			}
		}
		ImGui.end();
		if (object instanceof Line line) {
			line.getRenderer().renderImGui(io);
		} else if (object instanceof EndPoint endPoint) {
			if (ImGui.begin("Endpoint")) {
				ImGui.text(endPoint.isOutputEndPoint() ? "Output" : "Input");
				ImGui.sameLine();
				ImGui.text("of " + (endPoint.getLine().getLabel() != null ? endPoint.getLine().getLabel() : endPoint.getLine().getId()));

				ImGui.text("Position: ");
				ImGui.sameLine();
				float[] floats = new float[] {endPoint.getX(), endPoint.getY(), endPoint.getZ()};
				if (ImGui.inputScalarN("##PositionInput", floats, 3)) {
					endPoint.setPos(floats[0], floats[1], floats[2]);
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}

				ImGui.text("Rotation: ");
				ImGui.sameLine();
				floats[0] = (float) Math.toDegrees(endPoint.getYaw());
				floats[1] = (float) Math.toDegrees(endPoint.getPitch());
				floats[2] = (float) Math.toDegrees(endPoint.getRoll());
				if (ImGui.inputScalarN("##RotationInput", floats, 3)) {
					endPoint.setYaw((float) Math.toRadians(floats[0]));
					endPoint.setPitch((float) Math.toRadians(floats[1]));
					endPoint.setRoll((float) Math.toRadians(floats[2]));
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}

				if (ImGui.button("Reset rotation")) {
					endPoint.setYaw(0);
					endPoint.setPitch(0);
					endPoint.setRoll(0);
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Snap to xz bottom")) {
					endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()), Math.floor(endPoint.z()) + 0.5f);
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				ImGui.sameLine();
				if (ImGui.button("Snap to xyz center")) {
					endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()) + 0.5f, Math.floor(endPoint.z()) + 0.5f);
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Update corresponding endpoint")) {
					endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Copy rotation from player")) {
					LocalPlayer player = Minecraft.getInstance().player;
					assert player != null;
					float yaw = player.getYRot();
					float pitch = player.getXRot();
					endPoint.setYaw((float) Math.toRadians(yaw));
					endPoint.setPitch((float) Math.toRadians(pitch));
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Set rotation from tangent")) {
					Vector3f direction = EditorCommands.getAverageDirection(endPoint);

					float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
					float pitch = (float) -Math.asin(direction.y);

					endPoint.setYaw(yaw);
					endPoint.setPitch(pitch);
					//endPoint.updateCorrespondingEndpoint();
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Set rotation from weighted tangent")) {
					Vector3f direction = EditorCommands.getWeightedAverageDirection(endPoint);

					float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
					float pitch = (float) -Math.asin(direction.y);

					endPoint.setYaw(yaw);
					endPoint.setPitch(pitch);
					//endPoint.updateCorrespondingEndpoint();
					if (EditorOptions.autoSnap && endPoint.equalsCorrespondingEndpoint()) endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Set recursive weighted rotation")) {
					EditorCommands.applyWeightedRotationRecursively(endPoint);
				}
			}
			ImGui.end();
		}

	}

	public void showPreview(Line line) {
		Vector3f direction = line.getOutputEndPoint().getPos().sub(line.getInputEndPoint().getPos(), new Vector3f());
		direction.normalize();
		previewPoint = new LineExtensionPreviewPoint(line.getOutputEndPoint().pos().add(direction, direction), line, Colors.CYAN);
		select(previewPoint);
		isPreviewing = true;
	}

	public void createPreviewed() {
		EditorObject object = getScreen().getSelectedObject();
		if (object == previewPoint) {
			Line previousLine = ((LineExtensionPreviewPoint) previewPoint).getLine();
			Line newLine = new Line(previousLine.getOutputEndPoint().getPos(), previewPoint.getPos());
			previousLine.setOutputLine(newLine);
			getScreen().getTrack().getLines().add(newLine);
			previewPoint = null;
			newLine.getRenderer().setDirty(true);
			select(newLine);
		}
		isPreviewing = false;
	}

	public void cancelPreview() {
		Line line = ((LineExtensionPreviewPoint) previewPoint).getLine();
		select(line);
		previewPoint = null;
		isPreviewing = false;
	}

	public void splitSelectedLineInCenter(Line line) {
		InterpolatedPoint newCenter = line.lerp(0.5f);
		Line lineA = new Line(Line.getNewRandomId(), line.getInputEndPoint(), newCenter);
		Line lineB = new Line(Line.getNewRandomId(), newCenter, line.getOutputEndPoint());
		if (line.getInputLine() != null) line.getInputLine().setOutputLine(lineA);
		lineA.setOutputLine(lineB);
		lineB.setOutputLine(line.getOutputLine());
		getScreen().getTrack().removeLine(line.getId());
		getScreen().getTrack().getLines().add(lineA);
		getScreen().getTrack().getLines().add(lineB);

		select(lineA.getOutputEndPoint());

	}

	public void deleteSelectedLine(Line line) {
		getScreen().getTrack().removeLine(line.getId());
		select(null);
	}

	@Override
	public void endClientTick() {
		if (gizmo != null && getScreen().getSelectedObject() == null) gizmo = null;
	}
}
