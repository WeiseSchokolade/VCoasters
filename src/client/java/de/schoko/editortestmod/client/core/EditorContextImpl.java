package de.schoko.editortestmod.client.core;

import com.google.common.collect.ImmutableList;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.client.EmptyView;
import de.schoko.editortestmod.client.lines.CreateFirstLineView;
import de.schoko.editortestmod.client.lines.LineManager;
import de.schoko.editortestmod.client.points.LineEndPointView;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.packets.ApplyTrackChangesC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

public class EditorContextImpl implements EditorContext {
	private boolean editorActive;
	private LineManager lineManager;
	private GameType previousGameMode;
	private EditorObject selectedObject;
	private Screen currentScreen;
	private View view;
	private Track track;

	public EditorContextImpl() {
		lineManager = new LineManager();
		view = new EmptyView();
	}

	@Override
	public boolean editorActive() {
		return editorActive;
	}

	public void setEditorActive(boolean editorActive) {
		if (editorActive == this.editorActive) return;
		this.editorActive = editorActive;
		LocalPlayer player = Minecraft.getInstance().player;
		if (editorActive) {
			previousGameMode = player.gameMode();
			player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
			Minecraft.getInstance().mouseHandler.releaseMouse();
			Minecraft.getInstance().setScreen(getCurrentScreen());
			setView(new LineEndPointView());
		} else {
			if (player != null && player.gameMode() == GameType.SPECTATOR) {
				player.connection.send(new ServerboundChangeGameModePacket(previousGameMode));
				Minecraft.getInstance().mouseHandler.grabMouse();
			}
			setView(new EmptyView());
			setSelectedObject(null);
			Minecraft.getInstance().setScreen(null);
		}
	}

	@Override
	public boolean setSelectedObject(EditorObject selectedObject) {
		if (this.selectedObject == selectedObject) return false;
		this.selectedObject = selectedObject;
		return true;
	}

	@Override
	public void setView(View view) {
		this.view = view;
		this.view.setContext(this);
		this.view.load(this);
	}

	@Override
	public void load(Track track) {
		if (track == null) {
			lineManager.clearLines();
			setEditorActive(false);
			this.track = null;
			return;
		}
		lineManager.clearLines();
		lineManager.addLines(track.getLines());
		lineManager.updateOutputs(track.getLines());
		setEditorActive(false);
		setEditorActive(true);
		if (track.getLines().isEmpty()) {
			setView(new CreateFirstLineView());
		} else {
			setView(new LineEndPointView());
		}
		this.track = track;
	}

	@Override
	public void collectChanges() {
		List<Line> changedLines = new ArrayList<>();
		lineManager.getLines().forEach(line -> {
			if (line.getRenderer().isDirty()) {
				changedLines.add(line);
				line.getRenderer().setDirty(false);
			}
		});
		List<Line> removedLines = lineManager.getRemovedLines();
		if (changedLines.isEmpty() && removedLines.isEmpty()) return;
		removedLines.forEach(changedLines::remove);

		ClientPlayNetworking.send(new ApplyTrackChangesC2S(track.getId(), changedLines, List.copyOf(removedLines)));
		lineManager.clearRemovedLines();
	}

	@Override
	public LineManager getLineManager() {
		return lineManager;
	}

	@Override
	public EditorObject getSelectedObject() {
		return selectedObject;
	}

	@Override
	public void setCurrentScreen(Screen currentScreen) {
		this.currentScreen = currentScreen;
	}

	@Override
	public Screen getCurrentScreen() {
		return currentScreen;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public Track getTrack() {
		return track;
	}
}
