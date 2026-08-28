package de.schoko.vcoasters.client.modes.train;

import de.schoko.vcoasters.core.EndPoint;
import de.schoko.vcoasters.core.InterpolatedPoint;
import de.schoko.vcoasters.core.Line;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public record PlacementType<T extends PlacementType.ConfigurationObject>(String name, PointGenerator<T> endpointGenerator, PostProcessor<T> postProcessor, T configurationObject) {
	List<List<InterpolatedPoint>> generateEndpoints(Line baseLine, float beamSpacing) {
		return endpointGenerator.generateEndpoints(configurationObject, baseLine, beamSpacing);
	}

	void postProcess(List<Line> lines, Line baseLine, float spacing) {
		if (postProcessor == null) return;
		postProcessor.postProcess(lines, configurationObject, baseLine, spacing);
	}

	static PlacementType<StraightConfiguration> STRAIGHT = new PlacementType<>("Straight", (config, baseLine, beamSpacing) -> {
		List<InterpolatedPoint> points = new ArrayList<>();
		Vector3f lastPos = baseLine.getOutputEndPoint().pos();
		Vector3f direction = baseLine.getDirection(beamSpacing);

		for (int i = 0; i < config.length; i++) {
			Vector3f point = new Vector3f(lastPos).add(direction);
			points.add(new InterpolatedPoint(point, baseLine.getOutputEndPoint().getYaw(), baseLine.getOutputEndPoint().getPitch(), baseLine.getOutputEndPoint().getRoll()));
			lastPos = point;
		}

		return List.of(points);
	}, null, new StraightConfiguration());

	static class StraightConfiguration implements ConfigurationObject {
		private int length;

		public StraightConfiguration() {
			this(5);
		}

		public StraightConfiguration(int length) {
			this.length = length;
		}

		@Override
		public void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode) {
			int[] lengthInput = new int[] {length};
			if (ImGui.sliderInt("Segment length", lengthInput, 1, 30)) {
				length = lengthInput[0];
			}
		}
	}

	static PlacementType<CurvedConfiguration> CURVE = new PlacementType<>("Curve", (config, baseLine, beamSpacing) -> {
		List<InterpolatedPoint> points = new ArrayList<>();
		Vector3f lastPos = baseLine.getOutputEndPoint().pos();
		Vector3f direction = baseLine.getDirection(beamSpacing);

		Vector3f rotationVector;
		if (config.changePitchIfTrueElseYaw) {
			if (direction.dot(new Vector3f(0, 1, 0)) == 1) {
				rotationVector = new Vector3f(1, 0, 0);
			} else {
				rotationVector = direction.cross(new Vector3f(0, 1, 0), new Vector3f());
			}
		} else {
			rotationVector = new Vector3f(0, 1, 0);
		}
		float totalAngle = config.totalAngle ? config.segmentAngle : config.segmentAngle * config.length;

		for (int i = 0; i < config.length; i++) {
			float angle = totalAngle / (config.length - i);
			direction.rotateAxis(angle, rotationVector.x, rotationVector.y, rotationVector.z);
			totalAngle -= angle;
			if (i == config.length - 1 && Math.abs(direction.y) < 0.01) {
				direction.y = 0;
				direction.normalize(beamSpacing);
			}
			Vector3f point = new Vector3f(lastPos).add(direction);
			points.add(new InterpolatedPoint(point, 0, 0, 0));
			lastPos = point;
		}

		if (points.size() < 2) return List.of(points);
		points.set(0, InterpolatedPoint.getAverage(points.getFirst(), baseLine.getOutputEndPoint(), points.get(1)));
		for (int i = 1; i < points.size() - 1; i++) {
			points.set(i, InterpolatedPoint.getAverage(points.get(i), points.get(i - 1), points.get(i + 1)));
		}

		return List.of(points);
	}, null, new CurvedConfiguration());

	static class CurvedConfiguration implements ConfigurationObject {
		private int length;
		private boolean changePitchIfTrueElseYaw;
		private float segmentAngle;
		private boolean totalAngle;

		public CurvedConfiguration() {
			this(5, false, 0, false);
		}

		public CurvedConfiguration(int length, boolean changePitchIfTrueElseYaw, float segmentAngle, boolean totalAngle) {
			this.length = length;
			this.changePitchIfTrueElseYaw = changePitchIfTrueElseYaw;
			this.segmentAngle = segmentAngle;
			this.totalAngle = totalAngle;
		}

		@Override
		public void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode) {
			int[] lengthInput = new int[] {length};
			if (ImGui.sliderInt("Segment length", lengthInput, 1, 30)) {
				length = lengthInput[0];
				segmentAngle = (float) Math.clamp(segmentAngle, Math.toRadians(-getMaxSegmentAngle()), Math.toRadians(getMaxSegmentAngle()));
			}

			if (ImGui.radioButton("Yaw", !changePitchIfTrueElseYaw)) changePitchIfTrueElseYaw = false;
			ImGui.sameLine();
			if (ImGui.radioButton("Pitch", changePitchIfTrueElseYaw)) changePitchIfTrueElseYaw = true;

			ImBoolean targetAngleValue = new ImBoolean();
			targetAngleValue.set(totalAngle);
			if (ImGui.checkbox("Target arc angle", targetAngleValue)) {
				totalAngle = targetAngleValue.get();
				if (totalAngle) {
					segmentAngle *= length;
				} else {
					segmentAngle /= length;
				}
				segmentAngle = (float) Math.clamp(segmentAngle, Math.toRadians(-getMaxSegmentAngle()), Math.toRadians(getMaxSegmentAngle()));
			}

			float[] segmentAngleValue = new float[] {(float) Math.toDegrees(segmentAngle)};
			if (ImGui.dragFloat("Segment angle", segmentAngleValue, 0.1f, -getMaxSegmentAngle(), getMaxSegmentAngle())) {
				segmentAngle = (float) Math.toRadians(segmentAngleValue[0]);
			}
			if (!totalAngle) {
				ImGui.text("Total angle: ");
				ImGui.sameLine();
				ImGui.textColored(0xFFFFFFFF, "" + Math.toDegrees((segmentAngle * length)));
			}

			if (changePitchIfTrueElseYaw && ImGui.button("Snap horizon")) {
				Vector3f direction = line.getDirection(1f);
				double angleChange = ((Math.PI * 0.5) - new Vector3f(0, 1, 0).angle(direction));
				segmentAngle = (float) -Math.clamp(totalAngle ? angleChange : angleChange / (length), Math.toRadians(-getMaxSegmentAngle()), Math.toRadians(getMaxSegmentAngle()));
			}
		}

		private float getMaxSegmentAngle() {
			if (totalAngle) {
				return TrainEditorMode.MAX_SEGMENT_ANGLE * length;
			} else {
				return TrainEditorMode.MAX_SEGMENT_ANGLE;
			}
		}
	}

	static PlacementType<ConnectorConfiguration> STRAIGHT_CONNECTOR = new PlacementType<>("Straight Connector", (config, baseLine, beamSpacing) -> {
		if (config.endLine == null) return List.of();
		Line endLine = config.endLine;
		EndPoint startPoint = baseLine.getOutputEndPoint();
		EndPoint endPoint = endLine.getInputEndPoint();

		double distance = startPoint.getPos().distance(endPoint.getPos());
		int lineAmount = (int) Math.ceil(distance / beamSpacing);

		InterpolatedPoint previousPoint = InterpolatedPoint.getAverage(new InterpolatedPoint(startPoint), startPoint, endPoint);
		Vector3f delta = endPoint.getPos().sub(startPoint.getPos(), new Vector3f()).normalize();

		List<InterpolatedPoint> points = new ArrayList<>();
		for (int i = 0; i < lineAmount; i++) {
			if (i == lineAmount - 1) {
				points.add(new InterpolatedPoint(endPoint.pos(), previousPoint.yaw(), previousPoint.pitch(), previousPoint.roll()));
			} else {
				double length = (distance / (lineAmount - i));
				distance -= length;
				delta.normalize((float) length);
				InterpolatedPoint newPoint = new InterpolatedPoint(previousPoint.point().add(delta, new Vector3f()), previousPoint.yaw(), previousPoint.pitch(), previousPoint.roll());
				points.add(newPoint);
				previousPoint = newPoint;
			}
		}
		return List.of(points);
	}, (lines, config, baseLine, beamSpacing) -> {
		lines.getLast().setOutputLine(config.endLine);
	}, new ConnectorConfiguration());

	static class ConnectorConfiguration implements ConfigurationObject {
		private String endLineId;
		private Line endLine;

		@Override
		public void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode) {
			ImString input = new ImString();
			input.set(endLineId);
			if (ImGui.inputText("Connected line", input)) {
				endLineId = input.get();
				endLine = editorMode.getTrack().getLine(endLineId);
			}
			ImGui.beginDisabled();
			ImGui.checkbox("Found", endLine != null);
			ImGui.endDisabled();
		}
	}

	static PlacementType<ForwardsSwitchConfiguration> FORWARDS_SWITCH = new PlacementType<>("Forwards Switch", (config, baseLine, beamSpacing) -> {
		List<InterpolatedPoint> straightPoints = STRAIGHT.endpointGenerator
			.generateEndpoints(new StraightConfiguration(config.length), baseLine, beamSpacing).getFirst();

		List<InterpolatedPoint> curvePoints = CURVE.endpointGenerator
			.generateEndpoints(new CurvedConfiguration(config.length, false, (float) (config.leftIfTrueElseRightIfFalse ? Math.toRadians(25) : Math.toRadians(-25)), true), baseLine, beamSpacing).getFirst();

		return List.of(straightPoints, curvePoints);
	}, null, new ForwardsSwitchConfiguration());

	static class ForwardsSwitchConfiguration implements ConfigurationObject {
		private boolean leftIfTrueElseRightIfFalse;
		private final int length = 15;

		@Override
		public void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode) {
			if (ImGui.radioButton("Left", leftIfTrueElseRightIfFalse)) leftIfTrueElseRightIfFalse = true;
			ImGui.sameLine();
			if (ImGui.radioButton("Right", !leftIfTrueElseRightIfFalse)) leftIfTrueElseRightIfFalse = false;
		}
	}

	static PlacementType<InputFromSwitchConfiguration> INPUT_FROM_SWITCH = new PlacementType<>("Input from switch", (config, baseLine, beamSpacing) -> {
		List<InterpolatedPoint> straightPoints = STRAIGHT.endpointGenerator
			.generateEndpoints(new StraightConfiguration(2), baseLine, beamSpacing).getFirst();

		List<InterpolatedPoint> helperCurvePoints = CURVE.endpointGenerator
			.generateEndpoints(new CurvedConfiguration(config.length, false, (float) (config.leftIfTrueElseRightIfFalse ? Math.toRadians(-25) : Math.toRadians(25)), true),
				new Line(new InterpolatedPoint(straightPoints.getLast()), new InterpolatedPoint(straightPoints.getFirst())), beamSpacing).getFirst();

		return List.of(helperCurvePoints.reversed(), straightPoints.subList(0, 1));
	}, (lines, config, baseLine, beamSpacing) -> {
		if (lines.size() != 1) {
			lines.removeFirst().cutOut();
		}
	}, new InputFromSwitchConfiguration());

	static class InputFromSwitchConfiguration implements ConfigurationObject {
		private boolean leftIfTrueElseRightIfFalse;
		private final int length = 15;

		@Override
		public void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode) {
			if (ImGui.radioButton("Left", leftIfTrueElseRightIfFalse)) leftIfTrueElseRightIfFalse = true;
			ImGui.sameLine();
			if (ImGui.radioButton("Right", !leftIfTrueElseRightIfFalse)) leftIfTrueElseRightIfFalse = false;
		}
	}

	public static List<PlacementType<?>> LIST = List.of(STRAIGHT, CURVE, STRAIGHT_CONNECTOR, FORWARDS_SWITCH, INPUT_FROM_SWITCH);

	public interface PointGenerator<T extends ConfigurationObject> {
		List<List<InterpolatedPoint>> generateEndpoints(T config, Line baseLine, float beamSpacing);
	}

	public interface PostProcessor<T extends ConfigurationObject> {
		void postProcess(List<Line> lines, T config, Line baseLine, float beamSpacing);
	}

	public interface ConfigurationObject {
		void renderImGui(ImGuiIO io, Line line, TrainEditorMode editorMode);
	}

}
