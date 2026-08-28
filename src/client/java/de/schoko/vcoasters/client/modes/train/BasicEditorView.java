package de.schoko.vcoasters.client.modes.train;

import com.google.gson.Gson;
import com.mojang.math.Transformation;
import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.client.VCoastersClient;
import de.schoko.vcoasters.client.core.Colors;
import de.schoko.vcoasters.client.core.TargetTester;
import de.schoko.vcoasters.client.core.View;
import de.schoko.vcoasters.client.editor.EditorCommands;
import de.schoko.vcoasters.client.export.DefaultExporter;
import de.schoko.vcoasters.client.export.model.ItemDefinitionFile;
import de.schoko.vcoasters.client.export.model.ItemDefinitionTransformation;
import de.schoko.vcoasters.client.export.model.Model;
import de.schoko.vcoasters.client.modes.train.renderer.TrainLineBoxComponent;
import de.schoko.vcoasters.core.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BasicEditorView extends View<TrainEditorMode> {
	private boolean selectEditGroup;

	private PlacementType<?> placementType;
	private String requestedModelFilePath;
	private String requestedDefinitionPath;
	private String definitionModel;
	private boolean exportToFolderIfTrue;
	private String requestedFilePath;
	private String majorNamespace;
	private String minorNamespace;

	public BasicEditorView(TrainEditorMode mode) {
		super(mode);
		selectEditGroup = true;
	}

	@Override
	public boolean handleAttack() {
		Track track = getMode().getTrack();
		return TargetTester.consumeClosestTarget(
			TargetTester.consumer(track.getLines().size(), (i, from, to) -> track.getLines().get(i).getComponent(TrainLineBoxComponent.class).clip(from, to), i -> {
				Line line = track.getLines().get(i);
				if (selectEditGroup && line.getEditGroup() != null) {
					String groupId = line.getEditGroup();
					List<Line> list = track.getLines().stream().filter(checkedLine -> checkedLine.getEditGroup() != null && checkedLine.getEditGroup().equals(groupId)).toList();
					getMode().select(new LineEditGroup(groupId, list));
				} else {
					getMode().select(line);
				}
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

		String editGroup = null;
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
			if (selectEditGroup && target instanceof Line line) editGroup = line.getEditGroup();
		} else {
			target = null;
		}
		String usedEditorGroup = editGroup;

		track.getLines().forEach(line -> {
			line.getComponent(TrainLineBoxComponent.class).upload(renderContext, usedEditorGroup != null ? usedEditorGroup.equals(line.getEditGroup()) : line == target, getMode().isSelected(line));
			if (line.getEditGroup() != null) {
				if (line.getInputLine() == null) {
					renderContext.drawBoxLine(line.getInputEndPoint().getPos(), line.getOutputEndPoint().getPos(), 0.025f, Colors.RED);
				}
				if (line.getOutputLine() == null) {
					renderContext.drawBoxLine(line.getInputEndPoint().getPos(), line.getOutputEndPoint().getPos(), 0.025f, Colors.BLUE);
				}
			}
			if (line.getInputLine() == null) renderContext.drawBoxPoint(line.getInputEndPoint().getPos(), 0.05f, Colors.RED);
			if (line.getOutputLine() == null) renderContext.drawBoxPoint(line.getOutputEndPoint().getPos(), 0.05f, Colors.BLUE);

			if (line.getOutputLine() != null) {
				renderContext.drawBoxLine(line.getCenter(), line.getOutputLine().getCenter(), 0.025f, Colors.YELLOW);
			}
		});

		Line line = getLineForExtension();
		if (line != null && placementType != null) {
			List<List<InterpolatedPoint>> pointLists = placementType.generateEndpoints(line, getMode().getBeamSpacing());
			if (pointLists != null && !pointLists.isEmpty()) {
				for (List<InterpolatedPoint> points : pointLists) {
					List<Line> lines = new ArrayList<>();
					lines.add(new Line(line.getOutputEndPoint(), points.getFirst()));
					for (int i = 0; i < points.size() - 1; i++) {
						lines.add(new Line(points.get(i), points.get(i + 1)));
					}
					placementType.postProcess(lines, line, getMode().getBeamSpacing());
					VCoastersClient.addDebugString("Last direction", lines.getLast().getDirection(1));

					lines.forEach(drawnLine -> {
						TrainLineBoxComponent component = new TrainLineBoxComponent(drawnLine, getMode());
						component.updateQuads();
						component.upload(renderContext, false, false, Colors.GREEN);
					});
				}
			}
		}
	}

	@Override
	public void renderImGui(ImGuiIO io) {
		EditorObject selectedObject = getMode().getSelectedObject();
		if (ImGui.begin("Train Editor Mode")) {
			ImBoolean imBoolean = new ImBoolean();
			imBoolean.set(selectEditGroup);
			if (ImGui.checkbox("Select edit group", imBoolean)) {
				selectEditGroup = imBoolean.get();
			}

			ImGui.text("File:");
			ImGui.sameLine();
			ImString inputString = new ImString();
			inputString.set(requestedModelFilePath);
			if (ImGui.inputText("##FilePathInput", inputString)) {
				this.requestedModelFilePath = inputString.get();
			}
			ImGui.sameLine();
			if (ImGui.button("Choose##ModelFilePathChooser")) {
				try (MemoryStack stack = MemoryStack.stackPush()) {
					PointerBuffer filterPatterns = stack.mallocPointer(1);
					filterPatterns.put(stack.UTF8("*.json"));
					filterPatterns.flip();
					requestedModelFilePath = TinyFileDialogs.tinyfd_saveFileDialog("Choose file location", requestedModelFilePath != null ? requestedModelFilePath : "", filterPatterns, null);
				}
			}

			ImGui.beginDisabled(requestedModelFilePath == null);
			if (ImGui.button("Export model")) {
				Gson gson = new Gson();
				String json = gson.toJson(generateModel());
				try {
					Files.writeString(Path.of(requestedModelFilePath), json, StandardCharsets.UTF_8);
					TinyFileDialogs.tinyfd_messageBox("Export", "Model of " + getMode().getTrack().getTrackName() + " was exported!", "ok", "info", 0);
				} catch (IOException e) {
					TinyFileDialogs.tinyfd_messageBox("Export", "An error occurred while trying to export model!\n" + e.getMessage(), "ok", "error", 0);
					e.printStackTrace();
				}
			}
			ImGui.endDisabled();

			ImGui.text("Definition File:");
			ImGui.sameLine();
			inputString = new ImString();
			inputString.set(requestedDefinitionPath);
			if (ImGui.inputText("##DefinitionFilePathInput", inputString)) {
				this.requestedDefinitionPath = inputString.get();
			}
			ImGui.sameLine();
			if (ImGui.button("Choose##DefinitionFileChooser")) {
				try (MemoryStack stack = MemoryStack.stackPush()) {
					PointerBuffer filterPatterns = stack.mallocPointer(1);
					filterPatterns.put(stack.UTF8("*.json"));
					filterPatterns.flip();
					requestedDefinitionPath = TinyFileDialogs.tinyfd_saveFileDialog("Choose file location", requestedDefinitionPath != null ? requestedDefinitionPath : "", filterPatterns, null);
				}
			}
			ImGui.text("Model:");
			inputString = new ImString();
			inputString.set(definitionModel);
			ImGui.sameLine();
			if (ImGui.inputText("##DefinitionModelNameInput ", inputString)) {
				definitionModel = inputString.get();
			}
			String modelCenter = getItemModelDefinitionCenter().toString(NumberFormat.getNumberInstance());
			ImGui.text("Model center: " + modelCenter);
			ImGui.sameLine();
			if (ImGui.button("Copy")) {
				Minecraft.getInstance().keyboardHandler.setClipboard(modelCenter);
			}
			ImGui.beginDisabled(requestedDefinitionPath == null);
			if (ImGui.button("Export definition")) {
				Gson gson = new Gson();
				String json = gson.toJson(generateItemModelDefinition(definitionModel));
				try {
					Files.writeString(Path.of(requestedDefinitionPath), json, StandardCharsets.UTF_8);
					TinyFileDialogs.tinyfd_messageBox("Export", "Model of " + getMode().getTrack().getTrackName() + " was exported!", "ok", "info", 0);
				} catch (IOException e) {
					TinyFileDialogs.tinyfd_messageBox("Export", "An error occurred while trying to export model!\n" + e.getMessage(), "ok", "error", 0);
					e.printStackTrace();
				}
			}
			ImGui.endDisabled();


			if (!getMode().getTrack().getLines().isEmpty() && ImGui.collapsingHeader("Export##ExportHeader")) {
				//TinyFileDialogs.tinyfd_colorChooser("Colors?", "#FF0077", ByteBuffer.wrap(new byte[] {0, 0, 0}), ByteBuffer.wrap(new byte[] {0, 0, 0}));
				//TinyFileDialogs.tinyfd_messageBox("Heya!", "How you doin?", "ok", "info", 0);
				//TinyFileDialogs.tinyfd_notifyPopup("Uh, oh!", "They call me hermit, the frog", "warning");
				if (ImGui.radioButton("Folder", exportToFolderIfTrue)) exportToFolderIfTrue = true;
				ImGui.sameLine();
				if (ImGui.radioButton("Zip", !exportToFolderIfTrue)) exportToFolderIfTrue = false;
				ImGui.text(exportToFolderIfTrue ? "Folder:" : "File:");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(requestedFilePath);
				if (ImGui.inputText("##PackExportFilePathInput", inputString)) {
					this.requestedFilePath = inputString.get();
				}
				ImGui.sameLine();
				if (ImGui.button("Choose")) {
					if (exportToFolderIfTrue) requestedFilePath = TinyFileDialogs.tinyfd_selectFolderDialog("Choose folder", requestedFilePath);
					else requestedFilePath = TinyFileDialogs.tinyfd_saveFileDialog("Choose file location", requestedFilePath, PointerBuffer.allocateDirect(0), "This is a description");
				}

				if (majorNamespace == null) majorNamespace = getMode().getTrack().getId().split(":")[0];
				if (minorNamespace == null)
					minorNamespace = getMode().getTrack().getId().contains(":") ? getMode().getTrack().getId().split(":")[1] : getMode().getTrack().getTrackName();

				ImGui.text("Major namespace");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(majorNamespace);
				if (ImGui.inputText("##MajorNameInput", inputString)) {
					majorNamespace = inputString.get();
				}

				ImGui.text("Minor namespace");
				ImGui.sameLine();
				inputString = new ImString();
				inputString.set(minorNamespace);
				if (ImGui.inputText("##MinorNameInput", inputString)) {
					minorNamespace = inputString.get();
				}
				if (ImGui.button("Export")) {
					try {
						EditorCommands.applyWeightedRotationRecursively(getMode().getTrack().getLines());
						if (exportToFolderIfTrue) {
							DefaultExporter.getExporter().exportToDirectory(getMode().getTrack(), List.of(getMode().getTrack().getLines().getFirst().getId()), majorNamespace, minorNamespace, new File(requestedFilePath));
						} else {
							DefaultExporter.getExporter().exportToZip(getMode().getTrack(), List.of(getMode().getTrack().getLines().getFirst().getId()), majorNamespace, minorNamespace, new File(requestedFilePath));
						}
						TinyFileDialogs.tinyfd_messageBox("Export", getMode().getTrack().getTrackName() + " was exported!", "ok", "info", 0);
					} catch (IOException e) {
						TinyFileDialogs.tinyfd_messageBox("Export", "An error occurred while trying to export your track!\n" + e.getMessage(), "ok", "error", 0);
						e.printStackTrace();
					}
				}
			}

		}
		ImGui.end();
		if (selectedObject != null) {
			if (ImGui.begin("Edit")) {
				if (ImGui.button("Remove")) {
					if (selectedObject instanceof LineEditGroup lineEditGroup) {
						Line inputtingLine = lineEditGroup.getInputtingLine();
						lineEditGroup.lines().forEach(this::removeLine);
						if (inputtingLine != null) {
							if (inputtingLine.getEditGroup() != null) {
								String groupId = inputtingLine.getEditGroup();
								List<Line> list = getMode().getTrack().getLines().stream().filter(checkedLine -> checkedLine.getEditGroup() != null && checkedLine.getEditGroup().equals(groupId)).toList();
								getMode().select(new LineEditGroup(groupId, list));
							} else {
								getMode().select(inputtingLine);
							}
						}
						else getMode().select(null);
					} else if (selectedObject instanceof Line line) {
						Line previousInput = line.getInputLine();
						Line previousOutput = line.getOutputLine();
						removeLine(line);
						getMode().select(previousInput != null ? previousInput : previousOutput);
					}
				}
				if (selectedObject instanceof LineEditGroup lineEditGroup) {
					Line groupStart = lineEditGroup.getStart();
					if (groupStart != null && ImGui.button("Select start line")) getMode().select(groupStart);
					Line groupEnd = lineEditGroup.getEnd();
					if (groupEnd != null && ImGui.button("Select end line")) getMode().select(groupEnd);

				}
			}
			ImGui.end();
		}

		Line extensionLine = getLineForExtension();
		if (extensionLine != null) {
			if (ImGui.begin("Place")) {
				for (PlacementType<?> type : PlacementType.LIST) {
					if (ImGui.radioButton(type.name(), type == placementType)) {
						placementType = type;
					}
					ImGui.sameLine();
				}
				if (ImGui.radioButton("None", null == placementType)) {
					placementType = null;
				}
				if (placementType != null) {
					placementType.configurationObject().renderImGui(io, extensionLine, getMode());

					if (ImGui.button("Place")) {
						for (List<InterpolatedPoint> points : placementType.generateEndpoints(extensionLine, getMode().getBeamSpacing())) {
							String editGroup = Line.getNewRandomId();
							List<Line> lines = new ArrayList<>();

							Line connectingLine = new Line(extensionLine.getOutputEndPoint(), points.getFirst());
							extensionLine.setOutputLine(connectingLine);
							lines.add(connectingLine);
							connectingLine.setEditGroup(editGroup);
							for (int i = 0; i < points.size() - 1; i++) {
								Line newLine = new Line(points.get(i), points.get(i + 1));
								connectingLine.setOutputLine(newLine);
								lines.add(newLine);
								newLine.setEditGroup(editGroup);
								connectingLine = newLine;
							}
							placementType.postProcess(lines, extensionLine, getMode().getBeamSpacing());
							lines.forEach(line -> {
								getMode().addLine(line);
							});
							if (selectEditGroup) {
								getMode().select(new LineEditGroup(editGroup, lines));
							} else {
								getMode().select(connectingLine);
							}
						}
					}
				}

			}
			ImGui.end();
		}
	}

	public Model generateModel() {
		Model model = new Model(Map.of("0", "minecraft:block/spruce_log", "1", "minecraft:block/iron_block"), new ArrayList<>());
		model.elements().add(new Model.ModelElement(
			wrap(-getMode().getRailGauge() * 8 - getMode().getRailThickness() * 16, -getMode().getRailHeight() * 8, -8), wrap(-getMode().getRailGauge() * 8, getMode().getRailHeight() * 8, 8), Map.of(
			"up", new Model.ElementFace("#1"),
			"north", new Model.ElementFace("#1"),
			"east", new Model.ElementFace("#1"),
			"south", new Model.ElementFace("#1"),
			"west", new Model.ElementFace("#1"),
			"down", new Model.ElementFace("#1")
		), new Model.RotationSpecification(new double[] {0, 0, 0}, Direction.Axis.Y, 0)));
		model.elements().add(new Model.ModelElement(
			wrap(getMode().getRailGauge() * 8,-getMode().getRailHeight() * 8, -8), wrap(getMode().getRailGauge() * 8 + getMode().getRailThickness() * 16, getMode().getRailHeight() * 8, 8), Map.of(
			"up", new Model.ElementFace("#1"),
			"north", new Model.ElementFace("#1"),
			"east", new Model.ElementFace("#1"),
			"south", new Model.ElementFace("#1"),
			"west", new Model.ElementFace("#1"),
			"down", new Model.ElementFace("#1")
		), new Model.RotationSpecification(new double[] {0, 0, 0}, Direction.Axis.Y, 0)));
		model.elements().add(new Model.ModelElement(new double[] {-8, -getMode().getRailHeight() * 8 - getMode().getBeamHeight() * 16, -getMode().getBeamWidth() * 8}, new double[] {8, -getMode().getRailHeight() * 8, getMode().getBeamWidth() * 8}, Map.of(
			"up", new Model.ElementFace("#0"),
			"north", new Model.ElementFace("#0"),
			"east", new Model.ElementFace("#0"),
			"south", new Model.ElementFace("#0"),
			"west", new Model.ElementFace("#0"),
			"down", new Model.ElementFace("#0")
		), new Model.RotationSpecification(new double[] {0, 0, 0}, Direction.Axis.Y, 0)));
		return model;
	}

	public Vector3f getItemModelDefinitionCenter() {
		Vector3f center = new Vector3f();
		int amount = 0;
		for (Line line : getMode().getTrack().getLines()) {
			center.add(line.getCenter());
			amount++;
		}
		center.div(amount);
		return center;
	}

	public ItemDefinitionFile generateItemModelDefinition(String model) {
		ArrayList<ItemDefinitionFile.AbstractModelDefinition> models = new ArrayList<>();
		ItemDefinitionFile itemDefinitionFile = new ItemDefinitionFile(new ItemDefinitionFile.CompositeDefinition("minecraft:composite", models, null));

		Vector3f center = new Vector3f();
		int amount = 0;
		for (Line line : getMode().getTrack().getLines()) {
			center.add(line.getCenter());
			amount++;
		}
		center.div(amount);

		for (Line line : getMode().getTrack().getLines()) {
			models.add(new ItemDefinitionFile.ItemDefinition("minecraft:model", model, new ItemDefinitionTransformation(line.getCenter().sub(center).add(0.5f, 0.5f, 0.5f), line.getQuaternion(), new Vector3f(1), new Quaterniond())));
		}

		return itemDefinitionFile;
	}

	public double[] wrap(double... values) {
		return values;
	}

	public void removeLine(Line line) {
		if (line.getInputLine() != null) line.getInputLine().setOutputLine(null);
		line.setOutputLine(null);
		line.cutOut();
		getMode().removeLine(line);
	}

	public Line getLineForExtension() {
		if (getMode().getSelectedObject() instanceof Line line) {
			if (line.getOutputLine() == null) return line;
		} else if (getMode().getSelectedObject() instanceof LineEditGroup editGroup) {
			return editGroup.getExtensionLine();
		}
		return null;
	}

	@Override
	public void endClientTick() {

	}
}