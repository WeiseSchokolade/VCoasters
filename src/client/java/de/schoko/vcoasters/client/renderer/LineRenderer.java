package de.schoko.vcoasters.client.renderer;

import de.schoko.vcoasters.client.EditorDataScreen;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.editor.EditorStyle;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LineRenderer extends Renderer<Line> {
	public List<QuadObtainer.Quad> renderedQuads;

	public LineRenderer(Line line) {
		renderedQuads = new ArrayList<>();
		super(line);
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		if (ImGui.begin("Line")) {

			Line line = getObject();

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
				// TODO: Find better solution than static access
				if (Minecraft.getInstance().gui.screen() instanceof EditorDataScreen dataScreen) line.setOutputLine(string.get().isBlank() ? null : dataScreen.getTrack().getLine(string.get()));
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
			if (ImGui.inputText("##OnReachFunctionInput", string))
				line.setOnReachFunction(string.get().isBlank() || string.get().equals("null") ? null : string.get());

			ImGui.text("OnHaltFunction: ");
			ImGui.sameLine();
			string.set(line.getOnHaltFunction() == null ? "" : line.getOnHaltFunction());
			if (ImGui.inputText("##OnHaltFunctionInput", string))
				line.setOnHaltFunction(string.get().isBlank() || string.get().equals("null") ? null : string.get());

			if (line.getPhysicsType() == LinePhysicsType.STATION) {
				ImGui.text("Brakes engaged: ");
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
	}

	@Override
	public void upload(RenderContext context, EditorObject target, EditorObject selected) {
		Vector4f baseColor = switch (getObject().getPhysicsType()) {
			case LIFT -> new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
			case BRAKE -> new Vector4f(0.8f, 0.8f, 0.2f, 1.0f);
			case STATION -> new Vector4f(0.8f, 0.2f, 0.8f, 1.0f);
			case null, default -> EditorStyle.LINE_COLOR;
		};

		Vector4f color = isRendered(target) ? Colors.WHITE : isRendered(selected) ? new Vector4f(1f).lerp(baseColor, 0.5f) : baseColor;
		context.drawBoxLine(getObject().getInputEndPoint().pos(), getObject().getOutputEndPoint().pos(), EditorStyle.TRACK_LINE_WIDTH, color);
		Line outputLine = getObject().getOutputLine();
		if (outputLine != null) {
			context.drawBoxLine(getObject().getOutputEndPoint().pos().sub(getObject().getDirection(0.3f), new Vector3f()), outputLine.getInputEndPoint().pos().add(outputLine.getDirection(0.3f), new Vector3f()), 0.025f, Colors.YELLOW);
		}
	}

	@Override
	public Optional<Vec3> clip(Vec3 from, Vec3 to) {
		//Vector3f lineVector = getObject().getOutputEndPoint().pos().sub(getObject().getInputEndPoint().pos(), new Vector3f());
		Vector3f bLineBase = from.toVector3f();
		Vector3f bLineDir = to.toVector3f().sub((float) from.x, (float) from.y, (float) from.z);

		Optional<Double> first = renderedQuads.stream().map(quad -> quad.intersects(bLineBase, bLineDir))
			.filter(Optional::isPresent)
			.filter(aDouble -> aDouble.get() > 0.0 && aDouble.get() <= 1.0)
			.map(Optional::get)
			.sorted()
			.findFirst();
		if (first.isEmpty()) return Optional.empty();
		return Optional.of(from.add(new Vec3(bLineDir.mul(first.get().floatValue()))));

		/*double smallestDistance = Geometry.getSmallestDistance(getObject().getInputEndPoint().pos(), lineVector, bLineBase, bLineDir);
		if (Math.abs(smallestDistance) > 0.1) return Optional.empty();
		Optional<Double> possibleOffset = Geometry.getOffsetIntersectionDistanceAlongA(getObject().getInputEndPoint().pos(), lineVector, bLineBase, bLineDir);
		if (possibleOffset.isEmpty()) return Optional.empty();
		double offset = possibleOffset.get();
		if (offset >= 0 & offset <= 1) {
			//System.out.println(possibleOffset.get());
			return Optional.of(new Vec3(lineVector.mul((float) offset).add(getObject().getInputEndPoint().pos())));
		} else {
			return Optional.empty();
		}*/
	}

	@Override
	public void updateHitbox(Line object) {
		getObject().getInputEndPoint().getRenderer().updateHitbox(getObject().getInputEndPoint());
		getObject().getOutputEndPoint().getRenderer().updateHitbox(getObject().getOutputEndPoint());
		renderedQuads = QuadObtainer.boxLine(getObject().getInputEndPoint().pos(), getObject().getOutputEndPoint().pos(), EditorStyle.TRACK_LINE_WIDTH);
	}

	@Override
	public boolean isDirty() {
		if (super.isDirty()) return true;
		if (getObject().getInputEndPoint().getRenderer().isDirty()) return true;
		if (getObject().getOutputEndPoint().getRenderer().isDirty()) return true;
		return false;
	}

	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		if (!dirty) {
			getObject().getInputEndPoint().getRenderer().setDirty(false);
			getObject().getOutputEndPoint().getRenderer().setDirty(false);
		}
	}
}
