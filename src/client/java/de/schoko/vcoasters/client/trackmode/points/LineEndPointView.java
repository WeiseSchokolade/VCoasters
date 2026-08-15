package de.schoko.vcoasters.client.trackmode.points;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.*;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.editor.EditorCommands;
import de.schoko.vcoasters.client.editor.EditorOptions;
import de.schoko.vcoasters.client.gizmo.*;
import de.schoko.vcoasters.client.trackmode.TrackEditorMode;
import de.schoko.vcoasters.client.trackmode.renderer.EndpointBoxComponent;
import de.schoko.vcoasters.client.trackmode.renderer.LineBoxComponent;
import de.schoko.vcoasters.client.trackmode.renderer.LineRenderImGuiComponent;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LineEndPointView extends View<TrackEditorMode> {
	private Gizmo gizmo;
	private Point previewPoint;

	private boolean useEndpointRotationGizmo;
	private boolean isPreviewing;

	private ImInt selectedComboItem;

	public LineEndPointView(TrackEditorMode mode) {
		super(mode);
		this.selectedComboItem = new ImInt();
	}

	public boolean select(EditorObject object) {
		if (object != null) gizmo = switch (object) {
			case Line line -> new LineTranslationGizmo(line);
			case Point point -> new PointTranslationGizmo(point);
			case EndPoint endPoint -> (useEndpointRotationGizmo) ? new EndPointRotationGizmo(endPoint) : new EndPointTranslationGizmo(endPoint);
			default -> null;
		};
		if (getMode().isSelected(object)) return false;
		getMode().select(object);
		return true;
	}

	@Override
	public boolean handleAttack() {
		Track track = getMode().getEditedTrack();
		Runnable successResponse = (Minecraft.getInstance().player != null) ? () -> Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND) : () -> {};
		boolean handled = TargetTester.consumeClosestTarget(
			TargetTester.consumer(previewPoint != null ? 1 : 0, (index, from, to) -> previewPoint.getAABB().clip(from, to), i -> {
				successResponse.run();
				gizmo = new PointTranslationGizmo(previewPoint);
			}),
			TargetTester.consumer(track.getLines().size() * 2, (i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getComponent(EndpointBoxComponent.class).clip(from, to), i -> {
				if (previewPoint != null) cancelPreview();
				EndPoint endpoint = (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint());
				if (getMode().isSelected(endpoint)) {
					useEndpointRotationGizmo = !useEndpointRotationGizmo;
				}
				select(endpoint);
				successResponse.run();
			}),
			TargetTester.consumer(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getComponent(LineBoxComponent.class).clip(from, to), i -> {
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
		getMode().getSimulator().extract(renderContext);

		Track track = getMode().getEditedTrack();

		//renderContext.drawBoxLine(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f), 0.2f, Colors.WHITE);

		EditorObject target;
		if (!VCoastersClient.isDraggingCamera() && (Minecraft.getInstance().gui.screen() == null || !Minecraft.getInstance().gui.screen().isMouseOver(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos()))) {
			Optional<EditorObject> optionalTarget = TargetTester.getClosestTarget(
				TargetTester.provider(
					track.getLines().size() * 2,
					(i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getComponent(EndpointBoxComponent.class).clip(from, to),
					i -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint())),
				TargetTester.provider(
					track.getLines().size(),
					(i, from, to) -> track.getLines().get(i).getComponent(LineBoxComponent.class).clip(from, to),
					i -> track.getLines().get(i)),
				TargetTester.provider(gizmo != null ? gizmo.getHitboxAmount() : 0,
					(gizmo != null) ? gizmo::clip : (i, from, to) -> Optional.empty(),
					(gizmo != null) ? gizmo::getEditorObject : i -> null)
			);
			target = optionalTarget.orElse(null);
		} else {
			target = null;
		}

		EditorObject selectedObject = getMode().getSelectedObject();
		track.getLines().forEach(line -> {
			line.getComponent(LineBoxComponent.class).upload(renderContext, line == target, getMode().isSelected(line));
			line.getInputEndPoint().getComponent(EndpointBoxComponent.class).upload(renderContext, line.getInputEndPoint() == target, getMode().isSelected(line.getInputEndPoint()));
			line.getOutputEndPoint().getComponent(EndpointBoxComponent.class).upload(renderContext, line.getOutputEndPoint() == target, getMode().isSelected(line.getOutputEndPoint()));
		});

		if (previewPoint != null) {
			previewPoint.draw(renderContext);
		}
		if (selectedObject != null && gizmo != null) {
			if (EditorOptions.autoSnap) {
				if (selectedObject instanceof EndPoint endPoint) {
					if (endPoint.equalsCorrespondingEndpoint()) {
						gizmo.draw(renderContext, target);
						if (endPoint.getComponent(DirtContainer.class).isDirty()) {
							endPoint.updateCorrespondingEndpoint();
						}
					} else {
						gizmo.draw(renderContext, target);
					}
				} else if (selectedObject instanceof Line line) {
					boolean inputEquals = line.getInputEndPoint().equalsCorrespondingEndpoint();
					boolean outputEquals = line.getOutputEndPoint().equalsCorrespondingEndpoint();
					gizmo.draw(renderContext, target);
					if (line.getComponent(DirtContainer.class).isDirty()) {
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
		EditorObject object = getMode().getSelectedObject();
		if (isPreviewing) {
			if (ImGui.begin("Line preview")) {
				if (ImGui.button("Create")) {
					this.createPreviewed();
				}
				ImGui.sameLine();
				if (ImGui.button("Cancel")) {
					this.cancelPreview();
				}

				String[] names = Arrays.stream(EditorOptions.SnapSetting.values()).map(EditorOptions.SnapSetting::getName).toArray(String[]::new);
				ImInt imInt = new ImInt(EditorOptions.snapSettingIndex);
				if (ImGui.combo("Snap to", imInt, names)) {
					EditorOptions.snapSettingIndex = imInt.get();
				}
			}
			ImGui.end();
		}
		if (ImGui.begin("Builder")) {

			List<Line> lines = getMode().getEditedTrack().getLabelledLines();
			String[] array = lines.stream().map(Line::getLabel).toArray(String[]::new);

			if (array.length > 0) {
				if (ImGui.button("Select")) {
					select(lines.get(selectedComboItem.get()));
				}
				ImGui.sameLine();
				ImGui.combo("##LabelledSelectionSelect", selectedComboItem, array);
			}

			ImGui.beginDisabled(object == null);
			if (ImGui.button("Deselect##DeselectButton")) select(null);
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
				ImGui.setItemTooltip("Splits selected line in the middle");
				ImGui.sameLine();
				if (ImGui.button("Delete")) deleteSelectedLine(line);

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

				String[] names = Arrays.stream(EditorOptions.SnapSetting.values()).map(EditorOptions.SnapSetting::getName).toArray(String[]::new);
				ImInt imInt = new ImInt(EditorOptions.snapSettingIndex);
				if (ImGui.combo("Snap to", imInt, names)) {
					EditorOptions.snapSettingIndex = imInt.get();
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
			ImGui.setItemTooltip("Automatically treats endpoints as if they were unified");
			ImInt entityInteractionDistance = new ImInt(EditorOptions.interactionRange);
			ImGui.text("Range: ");
			ImGui.sameLine();
			if (ImGui.inputInt("##Range", entityInteractionDistance)) {
				EditorOptions.interactionRange = entityInteractionDistance.get();
			}
		}
		ImGui.end();
		if (object instanceof Line line) {
			line.getComponent(LineRenderImGuiComponent.class).renderImGui(io);
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
		if (getMode().isSelected(previewPoint)) {
			Line previousLine = ((LineExtensionPreviewPoint) previewPoint).getLine();
			Line newLine = new Line(previousLine.getOutputEndPoint().getPos(), previewPoint.getPos());
			previousLine.setOutputLine(newLine);
			getMode().addLine(newLine);
			previewPoint = null;
			newLine.getComponent(DirtContainer.class).setDirty(true);
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
		getMode().getEditedTrack().removeLine(line.getId());
		getMode().getEditedTrack().getLines().add(lineA);
		getMode().getEditedTrack().getLines().add(lineB);

		select(lineA.getOutputEndPoint());

	}

	public void deleteSelectedLine(Line line) {
		getMode().getEditedTrack().removeLine(line.getId());
		select(null);
	}

	@Override
	public void endClientTick() {
		if (gizmo != null && getMode().isNothingSelected()) gizmo = null;
	}
}
