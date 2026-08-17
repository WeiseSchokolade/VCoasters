package de.schoko.vcoasters.client.modes.coaster;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.modes.coaster.renderer.CoasterEndpointComponent;
import de.schoko.vcoasters.client.modes.coaster.renderer.CoasterLineBoxComponent;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicEditorView extends View<CoasterEditorMode> {
	private boolean preview;
	private int previewLength = 1;
	private float segmentAngle;
	private float segmentPitch;

	public BasicEditorView(CoasterEditorMode mode) {
		super(mode);
	}

	@Override
	public boolean handleAttack() {
		return false;
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
				TargetTester.provider(
					track.getLines().size() * 2,
					(i, from, to) -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint()).getComponent(CoasterEndpointComponent.class).clip(from, to),
					i -> (((i & 1) == 0) ? track.getLines().get(i / 2).getOutputEndPoint() : track.getLines().get(i / 2).getInputEndPoint())),
				TargetTester.provider(
					track.getLines().size(),
					(i, from, to) -> track.getLines().get(i).getComponent(CoasterLineBoxComponent.class).clip(from, to),
					i -> track.getLines().get(i))
			);
			target = optionalTarget.orElse(null);
		} else {
			target = null;
		}

		track.getLines().forEach(line -> {
			line.getInputEndPoint().getComponent(CoasterEndpointComponent.class).upload(renderContext, line.getInputEndPoint() == target, getMode().isSelected(line.getInputEndPoint()));
			line.getOutputEndPoint().getComponent(CoasterEndpointComponent.class).upload(renderContext, line.getOutputEndPoint() == target, getMode().isSelected(line.getOutputEndPoint()));
			line.getComponent(CoasterLineBoxComponent.class).upload(renderContext, line == target, getMode().isSelected(line));
		});

		if (preview) {
			List<Line> previewedLines = new ArrayList<>();
			Line baseLine = track.getLines().getLast();
			Line previousOutputLine = baseLine.getOutputLine();
			Line last = baseLine;
			ValuePoint lastEndpoint = new InterpolatedPoint(last.getOutputEndPoint());
			Vector3f previousDirection = last.getDirection(1);
			for (int i = 0; i < previewLength; i++) {
				Vector3f tangentVector = Geometry.getRotatedViewDirection(lastEndpoint, 1);
				//Vector3f rollVector = previousDirection.cross(new Vector3f(0f, 1f, 0f), new Vector3f());//.cross(previousDirection).rotateAxis(endpoint.getRoll(), previousDirection.x, previousDirection.y, previousDirection.z).normalize(0.5f);

				Vector3f offset = tangentVector.cross(0, 1, 0, new Vector3f()).rotateAxis(lastEndpoint.getRoll(), tangentVector.x, tangentVector.y, tangentVector.z);
				Vector3f up = tangentVector.cross(offset);
				Vector3f newPosition = new Vector3f(lastEndpoint.posToVector3f().add(new Vector3f(previousDirection).rotateAxis(segmentAngle, up.x, up.y, up.z).rotateAxis(segmentPitch, offset.x, offset.y, offset.z)));

				ValuePoint newEndpoint = new InterpolatedPoint(newPosition, lastEndpoint.yaw() + segmentAngle, lastEndpoint.pitch(), lastEndpoint.roll());
				Line line = new Line(lastEndpoint, newEndpoint);
				Vector3f newDirection = line.getDirection(1);

				Vector3f tangent = newDirection.add(previousDirection, new Vector3f()).normalize();
				double yaw = Math.atan2(tangent.z, tangent.x) - Math.PI * 0.5;
				double pitch = -Math.asin(tangent.y);
				line.getInputEndPoint().setYaw((float) yaw);
				line.getInputEndPoint().setPitch((float) pitch);
				last.getOutputEndPoint().setYaw((float) yaw);
				last.getOutputEndPoint().setPitch((float) pitch);

				previousDirection = newDirection;

				lastEndpoint = new InterpolatedPoint(line.getOutputEndPoint());
				getMode().addComponentsToLine(line);

				last.setOutputLine(line);
				previewedLines.add(line);
				last = line;
			}
			for (Line line : previewedLines) {
				line.getInputEndPoint().getComponent(CoasterEndpointComponent.class).upload(renderContext, line.getInputEndPoint() == target, getMode().isSelected(line.getInputEndPoint()));
				line.getOutputEndPoint().getComponent(CoasterEndpointComponent.class).upload(renderContext, line.getOutputEndPoint() == target, getMode().isSelected(line.getOutputEndPoint()));
				line.getComponent(CoasterLineBoxComponent.class).upload(renderContext, line == target, getMode().isSelected(line));
			}
			baseLine.setOutputLine(previousOutputLine);
		}
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (ImGui.begin("Line Preview")) {
			ImBoolean previewValue = new ImBoolean();
			previewValue.set(preview);
			if (ImGui.checkbox("Show Preview", previewValue)) {
				preview = previewValue.get();
			}

			int[] lengthValue = new int[] {previewLength};
			if (ImGui.sliderInt("Preview length", lengthValue, 1, 30)) {
				previewLength = lengthValue[0];
			}
			float[] segmentAngleValue = new float[] {(float) Math.toDegrees(segmentAngle)};
			if (ImGui.sliderFloat("Segment angle", segmentAngleValue, -15f, 15f)) {
				segmentAngle = (float) Math.toRadians(segmentAngleValue[0]);
			}
			float[] segmentPitchValue = new float[] {(float) Math.toDegrees(segmentPitch)};
			if (ImGui.sliderFloat("Segment pitch", segmentPitchValue, -15f, 15f)) {
				segmentPitch = (float) Math.toRadians(segmentPitchValue[0]);
			}
		}
		ImGui.end();

	}

	@Override
	public void endClientTick() {

	}
}