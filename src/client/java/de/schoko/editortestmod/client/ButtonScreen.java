package de.schoko.editortestmod.client;

import de.florianreuth.imguiexample.imgui.RenderInterface;
import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.client.core.EditorContext;
import de.schoko.editortestmod.client.editor.EditorAction;
import de.schoko.editortestmod.client.editor.EditorState;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.Line;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ButtonScreen extends Screen implements RenderInterface {
	private List<ScreenView> views;
	private final List<ScreenView> shownScreenViews;
	private final List<ScreenView> visibilityChecked;

	public ButtonScreen() {
		super(Component.literal("Editor Mode"));
		shownScreenViews = new ArrayList<>();
		visibilityChecked = new ArrayList<>();
	}

	private void addViews() {
		views = new ArrayList<>();
		views.add(new ScreenView("Edit line selection",this, true) {
			@Override
			public void onInit(int width, int height) {
				new SpriteIconButtonListBuilder(5, this)
					.setAlignment(SpriteIconButtonListBuilder.Alignment.CENTER)
					.setDirection(SpriteIconButtonListBuilder.Direction.VERTICAL)
					.setGap(5)
					.add(SpriteIconButton.builder(Component.literal("Add"), button -> EditorAction.previewNewLinePreviewProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/add"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Split"), button -> EditorAction.splitSelectedLineInCenterProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/split"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Delete"), button -> EditorAction.deleteSelectedLineProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/delete"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Spawn"), button -> EditorAction.spawnFollowerCarProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/spawn"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Deselect"), button -> EditorTestModClient.instance.getEditorCtx().setSelectedObject(null), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/cancel"), 15, 15)
						.build())
					.build();
			}

			@Override
			public boolean shouldBeVisible() {
				return EditorTestModClient.instance.getEditorCtx().getSelectedObject() != null &&
					EditorTestModClient.instance.getEditorCtx().getSelectedObject() instanceof Line &&
					!EditorState.isPreviewing;
			}
		});
		views.add(new ScreenView("Showing line preview",this, true) {
			@Override
			public void onInit(int width, int height) {
				new SpriteIconButtonListBuilder(5, this)
					.setAlignment(SpriteIconButtonListBuilder.Alignment.CENTER)
					.setDirection(SpriteIconButtonListBuilder.Direction.VERTICAL)
					.setGap(5)
					.add(SpriteIconButton.builder(Component.literal("Add"), button -> EditorAction.createNewLinePreviewProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.withDefaultNamespace("icon/checkmark"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Cancel"), button -> EditorAction.cancelNewLinePreviewProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/cancel"), 15, 15)
						.build())
					.build();
			}

			@Override
			public boolean shouldBeVisible() {
				return EditorState.isPreviewing;
			}
		});

		views.add(new ScreenView("Edit endpoint selection", this, true) {
			@Override
			public void onInit(int width, int height) {
				new SpriteIconButtonListBuilder(5, this)
					.setAlignment(SpriteIconButtonListBuilder.Alignment.CENTER)
					.setDirection(SpriteIconButtonListBuilder.Direction.VERTICAL)
					.setGap(5)
					.add(SpriteIconButton.builder(Component.literal("Translate"), button -> EditorAction.useTranslationGizmoForCurrentlySelectedEndpointProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/translate"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Rotation"), button -> EditorAction.useRotationGizmoForCurrentlySelectedEndpointProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/rotate"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Reset rotation"), button -> EditorAction.resetCurrentlySelectedEndpointRotationProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/reset"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Snap bottom center"), button -> EditorAction.snapCurrentlySelectedEndpointXZBottom.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/snap_xz_center"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Snap center"), button -> EditorAction.snapCurrentlySelectedEndpointXYZ.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/snap_xyz_center"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Spawn cart"), button -> EditorAction.spawnFollowerCarProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/spawn"), 15, 15)
						.build())
					.build();
			}

			@Override
			public boolean shouldBeVisible() {
				return EditorTestModClient.instance.getEditorCtx().getSelectedObject() != null &&
					EditorTestModClient.instance.getEditorCtx().getSelectedObject() instanceof EndPoint &&
					!EditorState.isPreviewing;
			}
		});

		views.add(new ScreenView("Follower car view", this, true) {
			@Override
			public void onInit(int width, int height) {
				new SpriteIconButtonListBuilder(5, this)
					.setAlignment(SpriteIconButtonListBuilder.Alignment.END)
					.setDirection(SpriteIconButtonListBuilder.Direction.HORIZONTAL)
					.setGap(5)
					.add(SpriteIconButton.builder(Component.literal("Move cart to current selection"), button -> {
							FollowerCar car = EditorState.followerCarGetter.get();
							if (car != null) {
								EditorObject selectedObject = EditorTestModClient.instance.getEditorCtx().getSelectedObject();
								if (selectedObject instanceof Line line) {
									car.setCurrentLine(line);
									car.setDistanceTravelled(0f);
								} else if (selectedObject instanceof EndPoint endPoint) {
									car.setCurrentLine(endPoint.getLine());
									car.setDistanceTravelled(endPoint.isOutputEndPoint() ? endPoint.getLine().getLength() - 0.0001f : 0);
								}
							}
						}, true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/spawn"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Remove Car"), button -> EditorAction.deleteFollowerCarProvider.run(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/delete"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Toggle visibility"), button -> EditorState.followerCarGetter.get().toggleRenderModel(), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/follower/eye"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Slower"), button -> EditorState.followerCarGetter.get().addToSpeed(-0.2f), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/follower/slower"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Pause"), button -> EditorState.followerCarGetter.get().setSpeed(0), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/follower/pause"), 15, 15)
						.build())
					.add(SpriteIconButton.builder(Component.literal("Faster"), button -> EditorState.followerCarGetter.get().addToSpeed(0.2f), true)
						.size(23, 23)
						.sprite(Identifier.fromNamespaceAndPath("editortestmod", "icon/follower/faster"), 15, 15)
						.build())
					.build();
			}

			@Override
			public boolean shouldBeVisible() {
				return EditorState.followerCarGetter.get() != null;
			}
		});
	}

	@Override
	protected void init() {
		if (views == null) {
			addViews();
		}
		visibilityChecked.clear();
		for (ScreenView view : views) {
			view.init(width, height);
			if (!shownScreenViews.contains(view) && (!view.useVisibilityCheck() || !view.shouldBeVisible())) view.hide();
			if (view.useVisibilityCheck()) visibilityChecked.add(view);
			this.children.addAll(view.getEventListeners());
			this.renderables.addAll(view.getRenderables());
			this.narratables.addAll(view.getNarratableEntries());
		}
	}

	@Override
	public void render(ImGuiIO io) {
		EditorState.doesImGuiCaptureMouseEvents = io.getWantCaptureMouse();
		EditorTestModClient.instance.getEditorCtx().getView().render(io);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		for (ScreenView view : visibilityChecked) {
			if (view.shouldBeVisible()) {
				if (!view.isVisible()) view.show();
			} else if (view.isVisible()) view.hide();
		}
		super.render(guiGraphics, i, j, f);
	}

	public void showView(ScreenView view) {
		if (shownScreenViews.contains(view) && shownScreenViews.size() == 1) return;
		if (view.useVisibilityCheck()) return;
		hideViews();
		shownScreenViews.add(view);
		view.show();
	}

	public void changeViews(List<ScreenView> views) {
		hideViews();
		for (ScreenView view : views) {
			if (view.useVisibilityCheck()) continue;
			shownScreenViews.add(view);
			view.show();
		}
	}

	private void hideViews() {
		for (ScreenView view : shownScreenViews) {
			view.hide();
		}
		shownScreenViews.clear();
	}

	private void hideView(ScreenView view) {
		if (!shownScreenViews.contains(view)) return;
		view.hide();
		shownScreenViews.remove(view);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		EditorTestModClient.instance.processKeyEvent(keyEvent);
		return true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if (!super.mouseClicked(mouseButtonEvent, bl)) {
			EditorTestModClient.handleAttack();
		}
		return true;
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {

	}

	@Override
	public boolean isMouseOver(double d, double e) {
		for (GuiEventListener child : children()) {
			if (child.isMouseOver(d, e)) return true;
		}
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
