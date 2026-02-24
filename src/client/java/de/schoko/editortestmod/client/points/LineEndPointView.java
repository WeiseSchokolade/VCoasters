package de.schoko.editortestmod.client.points;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.*;
import de.schoko.editortestmod.client.core.Colors;
import de.schoko.editortestmod.client.core.TargetTester;
import de.schoko.editortestmod.client.core.View;
import de.schoko.editortestmod.client.editor.EditorAction;
import de.schoko.editortestmod.client.editor.EditorCommands;
import de.schoko.editortestmod.client.editor.EditorState;
import de.schoko.editortestmod.client.gizmo.*;
import de.schoko.editortestmod.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.Optional;

public class LineEndPointView extends View {
	private Gizmo gizmo;
	private Point previewPoint;

	private FollowerCar car;
	private RideCar rideCar;

	public LineEndPointView(EditorScreen screen) {
		super(screen);
	}

	public boolean select(EditorObject object) {
		if (object != null) gizmo = switch (object) {
			case Line line -> new LineTranslationGizmo(line);
			case Point point -> new PointTranslationGizmo(point);
			case EndPoint endPoint -> (EditorState.endpointRotationMode) ? new EndPointRotationGizmo(endPoint) : new EndPointTranslationGizmo(endPoint);
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
					EditorState.endpointRotationMode = !EditorState.endpointRotationMode;
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
		EditorAction.createNewLinePreviewProvider = this::createPreviewed;
		EditorAction.cancelNewLinePreviewProvider = this::cancelPreview;


	}

	@Override
	public void render(RenderContext renderContext) {
		Track track = getScreen().getTrack();

		//renderContext.drawBoxLine(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f), 0.2f, Colors.WHITE);

		EditorObject target;
		if (!EditorTestModClient.isDraggingCamera() && (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.isMouseOver(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos()))) {
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
		if (car != null) {
			if (rideCar != null) {
				rideCar.render(renderContext);
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
		EditorObject object = getScreen().getSelectedObject();
		if (EditorState.isPreviewing) {
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
		if (object instanceof Line line) {
			if (ImGui.begin("Line")) {
				if (ImGui.button("Add")) this.showPreview(line);
				if (ImGui.button("Split")) splitSelectedLineInCenter(line);
				if (ImGui.button("Delete")) deleteSelectedLine(line);
				if (ImGui.button("Deselect")) select(null);

				if (ImGui.button("Select input")) select(line.getInputEndPoint());
				if (ImGui.button("Select output")) select(line.getOutputEndPoint());


				ImGui.text("Id: " + line.getId());
				ImGui.sameLine();
				if (ImGui.button("Copy")) {
					Minecraft.getInstance().keyboardHandler.setClipboard(line.getId());
				}

				ImGui.text("OutputLine: ");
				ImGui.sameLine();
				ImString string = new ImString();
				string.set(line.getOutputLineId() == null ? "" : line.getOutputLineId());
				if (ImGui.inputText("##OutputLineInput", string)) {
					line.setOutputLine(string.get().isBlank() ? null : getScreen().getTrack().getLine(string.get()));
				}

				ImGui.text("Length (in cb): " + Math.round(line.getLength() * 100));
				if (line.getLength() > 20) {
					ImGui.textColored(0xFF0000, "Line is too long! Only up to 20 blocks can be rendered properly!");
				}

				ImGui.text("Label: ");
				ImGui.sameLine();
				string.set(line.getLabel() != null ? line.getLabel() : "");
				if (ImGui.inputText("##LabelInput", string)) {
					line.setLabel(string.get().isBlank() ? null : string.get());
				}

				for (LinePhysicsType value : LinePhysicsType.values()) {
					if (ImGui.radioButton(value.name(), line.getPhysicsType() == value || (value == LinePhysicsType.REGULAR && line.getPhysicsType() == null))) {
						line.setPhysicsType(value);
					}
				}

				ImGui.text("OnReachFunction: ");
				ImGui.sameLine();
				string.set(line.getOnReachFunction() == null ? "" : line.getOnReachFunction());
				ImGui.inputText("##OnReachFunctionInput", string);
				line.setOnReachFunction(string.get().isBlank() || string.get().equals("null") ? null : string.get());

				ImGui.separatorText("Train");
				if (ImGui.button("Summon")) {
					car = new FollowerCar(line, getScreen());
					car.setRenderModel(EditorState.renderModel);
					rideCar = new RideCar(line, getScreen().getTrack().getCartModel().getSegmentAmount(), car);
				}
				if (line.getPhysicsType() == LinePhysicsType.STATION) {
					ImGui.text("Halting brake: ");
					ImGui.sameLine();
					ImBoolean fullStop = new ImBoolean();
					fullStop.set(line.isFullStop());
					ImGui.checkbox("##FullStop", fullStop);
					line.setFullStop(fullStop.get());
					if (ImGui.button("Release brakes")) {
						Line inspectedLine = line;
						while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
							inspectedLine.setFullStop(false);
							inspectedLine = inspectedLine.getOutputLine();
						}
						inspectedLine = line;
						while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
							inspectedLine.setFullStop(false);
							inspectedLine = inspectedLine.getInputLine();
						}
					}
					ImGui.sameLine();
					if (ImGui.button("Engage brakes")) {
						Line inspectedLine = line;
						while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
							inspectedLine.setFullStop(true);
							inspectedLine = inspectedLine.getOutputLine();
						}
						inspectedLine = line;
						while (inspectedLine != null && inspectedLine.getPhysicsType() == LinePhysicsType.STATION) {
							inspectedLine.setFullStop(true);
							inspectedLine = inspectedLine.getInputLine();
						}
					}
				}
			}
			ImGui.end();
		} else if (object instanceof EndPoint endPoint) {
			if (ImGui.begin("Endpoint")) {
				ImGui.text(endPoint.isOutputEndPoint() ? "Output" : "Input");
				if (ImGui.button("Select line")) select(endPoint.getLine());

				ImGui.text("Position: ");
				ImGui.sameLine();
				float[] floats = new float[] {endPoint.getX(), endPoint.getY(), endPoint.getZ()};
				if (ImGui.inputScalarN("##PositionInput", floats, 3)) {
					endPoint.setPos(floats[0], floats[1], floats[2]);
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
				}

				if (ImGui.radioButton("Translate", !EditorState.endpointRotationMode)) {
					EditorState.endpointRotationMode = false;
					select(endPoint);
				}
				ImGui.sameLine();
				if (ImGui.radioButton("Rotate", EditorState.endpointRotationMode)) {
					EditorState.endpointRotationMode = true;
					select(endPoint);
				}
				if (ImGui.button("Reset rotation")) {
					endPoint.setYaw(0);
					endPoint.setPitch(0);
					endPoint.setRoll(0);
				}
				if (ImGui.button("Snap to xz bottom")) {
					endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()), Math.floor(endPoint.z()) + 0.5f);
				}
				ImGui.sameLine();
				if (ImGui.button("Snap to xyz center")) {
					endPoint.setPos(Math.floor(endPoint.x()) + 0.5f, Math.floor(endPoint.y()) + 0.5f, Math.floor(endPoint.z()) + 0.5f);
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
				}
				if (ImGui.button("Set rotation from tangent")) {
					Vector3f direction = EditorCommands.getAverageDirection(endPoint);

					float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
					float pitch = (float) -Math.asin(direction.y);

					endPoint.setYaw(yaw);
					endPoint.setPitch(pitch);
					endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Set rotation from weighted tangent")) {
					Vector3f direction = EditorCommands.getWeightedAverageDirection(endPoint);

					float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
					float pitch = (float) -Math.asin(direction.y);

					endPoint.setYaw(yaw);
					endPoint.setPitch(pitch);
					endPoint.updateCorrespondingEndpoint();
				}
				if (ImGui.button("Set recursive weighted rotation")) {
					EditorCommands.applyWeightedRotationRecursively(endPoint);
				}

				ImGui.separatorText("Train");
				if (ImGui.button("Summon")) {
					Line line = (endPoint.isOutputEndPoint() && endPoint.getLine().getOutputLine() != null ? endPoint.getLine().getOutputLine() : endPoint.getLine());
					car = new FollowerCar(line, getScreen());
					car.setRenderModel(EditorState.renderModel);
					rideCar = new RideCar(line, getScreen().getTrack().getCartModel().getSegmentAmount(), car);
				}
			}
			ImGui.end();
		}

		if (car != null) {
			if (ImGui.begin("Follower car")) {

				ImGui.text("Show model: ");
				ImGui.sameLine();
				ImBoolean imBoolean = new ImBoolean();
				imBoolean.set(car.isRenderModel());
				ImGui.checkbox("##ToggleModelVisibility", imBoolean);
				car.setRenderModel(imBoolean.get());

				ImGui.text("Speed: ");
				ImGui.sameLine();
				ImFloat floatValue = new ImFloat();
				floatValue.set(car.getSpeed());
				ImGui.inputFloat("##SpeedInput", floatValue);
				car.setSpeed(floatValue.get());

				if (ImGui.button("Faster")) car.addToSpeed(0.2f);
				ImGui.sameLine();
				if (ImGui.button("Pause")) car.setSpeed(0f);
				ImGui.sameLine();
				if (ImGui.button("Slower")) car.addToSpeed(-0.2f);

				if (ImGui.button("Remove")) {
					car = null;
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
		EditorState.isPreviewing = true;
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
		EditorState.isPreviewing = false;
	}

	public void cancelPreview() {
		Line line = ((LineExtensionPreviewPoint) previewPoint).getLine();
		select(line);
		previewPoint = null;
		EditorState.isPreviewing = false;
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

	public void spawnFollowerCar() {
		if (getScreen().getSelectedObject() instanceof EndPoint endPoint) {
			car = new FollowerCar(endPoint.getLine(), getScreen());
			car.setRenderModel(EditorState.renderModel);
			if (endPoint.isOutputEndPoint()) {
				car.setDistanceTravelled(endPoint.getLine().getLength() - 0.00001f);
			}
		}
	}


	@Override
	public void endClientTick() {
		if (rideCar != null) rideCar.update();
		if (gizmo != null && getScreen().getSelectedObject() == null) gizmo = null;
	}
}
