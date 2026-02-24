package de.schoko.editortestmod;

import com.mojang.serialization.DataResult;
import de.schoko.editortestmod.codecs.TrackCodecs;
import de.schoko.editortestmod.packets.ApplyLineChangesC2S;
import de.schoko.editortestmod.packets.ApplyTrackMetaChangesC2S;
import de.schoko.editortestmod.packets.Networking;
import de.schoko.editortestmod.packets.SaveDataC2S;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		ServerPlayNetworking.registerGlobalReceiver(ApplyLineChangesC2S.TYPE, (payload, ctx) -> {
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
		ServerPlayNetworking.registerGlobalReceiver(ApplyTrackMetaChangesC2S.TYPE, (payload, ctx) -> {
			if (!canUse(ctx.player())) return;
			trackManager.getTrack(payload.track().getId()).mergeMetaFrom(payload.track());
		});
		ServerPlayNetworking.registerGlobalReceiver(SaveDataC2S.TYPE, (payload, ctx) -> {
			if (!canUse(ctx.player())) return;
			Identifier identifier = Identifier.parse(payload.id());
			CompoundTag data = ctx.server().getCommandStorage().get(identifier);
			/*if (data.isEmpty()) {
				ctx.player().sendSystemMessage(Component.literal("Track " + identifier + " not found!").withColor(0xFF0000));
				return;
			}*/
			Track track = EditorTestMod.instance.getTrackManager().getTrack(identifier.toString());
			track.setAcceleration(track.getGravity(), 1.0 / track.getTicksInHertz());

			DataResult<Tag> result = TrackCodecs.CURRENT.encodeStart(NbtOps.INSTANCE, track);
			ctx.server().getCommandStorage().set(identifier, (CompoundTag) result.getOrThrow());
			ctx.player().sendSystemMessage(Component.literal("Saved " + identifier));
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