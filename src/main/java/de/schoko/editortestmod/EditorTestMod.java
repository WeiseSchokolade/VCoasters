package de.schoko.editortestmod;

import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.packets.ApplyTrackChangesC2S;
import de.schoko.editortestmod.packets.Networking;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorTestMod implements ModInitializer {
	public static final String MOD_ID = "editortestmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static EditorTestMod instance;

	private TrackManager trackManager;

	@Override
	public void onInitialize() {
		instance = this;
		LOGGER.info("Hello Fabric world!");
		Networking.register();
		TrackManagerCommands.register();

		trackManager = new TrackManager();

		ServerPlayNetworking.registerGlobalReceiver(ApplyTrackChangesC2S.TYPE, (payload, ctx) -> {
			if (!canUse(ctx.player())) return;
			payload.removedLines().forEach(line -> {
				TrackLineManager.removeLine(TrackLineManager.getLine(line.getId()));
				trackManager.removeLine(line);
			});
			payload.lines().forEach(line -> {
				if (TrackLineManager.contains(line)) {
					TrackLineManager.getLine(line.getId()).mergeData(line);
				} else {
					if (line.getOutputLineId() != null) TrackLineManager.setOutputOrAddAsPendingConnection(line, line.getOutputLineId());
					TrackLineManager.addLine(line);

					getTrackManager().getTrack(payload.id()).getLines().add(line);
				}
			});

		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

		});
	}

	public static boolean canUse(CommandSourceStack stack) {
		return stack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public static boolean canUse(ServerPlayer player) {
		return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public TrackManager getTrackManager() {
		return trackManager;
	}
}