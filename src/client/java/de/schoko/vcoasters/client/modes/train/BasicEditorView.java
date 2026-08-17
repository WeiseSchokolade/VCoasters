package de.schoko.vcoasters.client.modes.train;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.modes.train.renderer.TrainLineBoxComponent;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicEditorView extends View<TrainEditorMode> {
	private int previewLength = 1;
	private float segmentAngle;
	private RotationMode rotationMode = RotationMode.YAW;

	public BasicEditorView(TrainEditorMode mode) {
		super(mode);
	}

	@Override
	public boolean handleAttack() {
		Track track = getMode().getTrack();
		return TargetTester.consumeClosestTarget(
			TargetTester.consumer(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getComponent(TrainLineBoxComponent.class).clip(from, to), i -> {
				Line line = track.getLines().get(i);
				if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
				getMode().select(line);
			})
		);
	}

	@Override
	public boolean handleDraggedAttack() {
		return false;
	}

	@Override
	public void leftMouseReleased() {

	}

	@Override
	public void load() {

	}

	@Override
	public void render(RenderContext renderContext) {

		Track track = getMode().getTrack();

		//renderContext.drawBoxLine(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f), 0.2f, Colors.WHITE);

		EditorObject target;
		if (!VCoastersClient.isDraggingCamera() && (Minecraft.getInstance().gui.screen() == null || !Minecraft.getInstance().gui.screen().isMouseOver(Minecraft.getInstance().mouseHandler.xpos(), Minecraft.getInstance().mouseHandler.ypos()))) {
			Optional<EditorObject> optionalTarget = TargetTester.getClosestTarget(
//				TargetTester.provider(
//					track.getLines().size() * 2,
//					(i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getComponent(CoasterEndpointComponent.class).clip(from, to),
//					i -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint())),
				TargetTester.provider(
					track.getLines().size(),
					(i, from, to) -> track.getLines().get(i).getComponent(TrainLineBoxComponent.class).clip(from, to),
					i -> track.getLines().get(i))
			);
			target = optionalTarget.orElse(null);
		} else {
			target = null;
		}

		track.getLines().forEach(line -> {
			line.getComponent(TrainLineBoxComponent.class).upload(renderContext, line == target, getMode().isSelected(line));
			renderContext.drawBoxPoint(line.getInputEndPoint().getPos(), 0.05f, Colors.YELLOW);
		});

		if (getMode().getSelectedObject() instanceof Line line && line.getOutputLine() == null) {
			List<InterpolatedPoint> points = getGeneratedEndpoints(line, previewLength, rotationMode, segmentAngle, getMode().getBeamSpacing());
			List<Line> lines = new ArrayList<>();
			lines.add(new Line(line.getOutputEndPoint(), points.getFirst()));
			for (int i = 0; i < previewLength - 1; i++) {
				lines.add(new Line(points.get(i), points.get(i + 1)));
			}

			lines.forEach(drawnLine -> {
				getMode().addComponentsToLine(drawnLine);
				drawnLine.getComponent(TrainLineBoxComponent.class).upload(renderContext, false, false);
			});
		}
	}

	public List<InterpolatedPoint> getGeneratedEndpoints(Line baseLine, int endpointAmount, RotationMode rotationMode, float rotationAngle, float beamSpacing) {
		List<InterpolatedPoint> points = new ArrayList<>();
		Vector3f lastPos = baseLine.getOutputEndPoint().pos();
		Vector3f direction = baseLine.getDirection(beamSpacing);

		Vector3f rotationVector;
		switch (rotationMode) {
			case null:
			case YAW:
				rotationVector = new Vector3f(0, 1, 0);
				break;
			case PITCH:
				if (direction.dot(new Vector3f(0, 1, 0)) == 1) {
					rotationVector = new Vector3f(1, 0, 0);
					break;
				}
				rotationVector = direction.cross(new Vector3f(0, 1, 0), new Vector3f());
				break;
		}

		float runningRoll = baseLine.getOutputEndPoint().roll();
		float rollOffset = 0;

		for (int i = 0; i < endpointAmount; i++) {
			direction.rotateAxis(rotationAngle, rotationVector.x, rotationVector.y, rotationVector.z);
			Vector3f point = new Vector3f(lastPos).add(direction);
			points.add(new InterpolatedPoint(point, 0, 0, runningRoll += rollOffset));
			lastPos = point;
		}

		if (points.size() < 2) return points;
		points.set(0, getAverage(points.getFirst(), baseLine.getOutputEndPoint(), points.get(1)));
		for (int i = 1; i < points.size() - 1; i++) {
			points.set(i, getAverage(points.get(i), points.get(i - 1), points.get(i + 1)));
		}

		return points;
	}

	public void setAverage(EndPoint endPoint, ValuePoint a, ValuePoint b) {
		Vector3f delta = new Vector3f(b.posToVector3f()).sub(a.posToVector3f()).normalize();
		double yaw = Math.atan2(delta.z, delta.x) - Math.PI * 0.5;
		double pitch = -Math.asin(delta.y);
		endPoint.setYaw((float) yaw);
		endPoint.setPitch((float) pitch);
	}

	public InterpolatedPoint getAverage(ValuePoint valuePoint, ValuePoint a, ValuePoint b) {
		Vector3f delta = new Vector3f(b.posToVector3f()).sub(a.posToVector3f()).normalize();
		double yaw = Math.atan2(delta.z, delta.x) - Math.PI * 0.5;
		double pitch = -Math.asin(delta.y);
		return new InterpolatedPoint(valuePoint.x(), valuePoint.y(), valuePoint.z(), (float) yaw, (float) pitch, valuePoint.roll());
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (getMode().getSelectedObject() instanceof Line line && line.getOutputLine() == null) {

			if (ImGui.begin("Line Preview")) {
				int[] lengthValue = new int[] {previewLength};
				if (ImGui.sliderInt("Preview length", lengthValue, 1, 30)) {
					previewLength = lengthValue[0];
				}

				for (RotationMode value : RotationMode.values()) {
					if (ImGui.radioButton(value.name(), rotationMode == value)) {
						rotationMode = value;
					}
					ImGui.sameLine();
				}
				ImGui.spacing();

				float[] segmentAngleValue = new float[] {(float) Math.toDegrees(segmentAngle)};
				if (ImGui.dragFloat("Segment angle", segmentAngleValue, 0.1f, -15f, 15f)) {
					segmentAngle = (float) Math.toRadians(segmentAngleValue[0]);
				}

				if (ImGui.button("Place")) {
					List<InterpolatedPoint> points = getGeneratedEndpoints(line, previewLength, rotationMode, segmentAngle, getMode().getBeamSpacing());
					Line connectingLine = new Line(line.getOutputEndPoint(), points.getFirst());
					getMode().addLine(connectingLine);
					line.setOutputLine(connectingLine);
					for (int i = 0; i < previewLength - 1; i++) {
						Line newLine = new Line(points.get(i), points.get(i + 1));
						getMode().addLine(newLine);
						connectingLine.setOutputLine(newLine);
						connectingLine = newLine;
					}
					getMode().select(connectingLine);
				}
			}
			ImGui.end();
		}
	}

	@Override
	public void endClientTick() {

	}

	public enum RotationMode {
		YAW,
		PITCH;
	}
}