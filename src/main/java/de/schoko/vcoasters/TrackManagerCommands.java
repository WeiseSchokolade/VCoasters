package de.schoko.vcoasters;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.schoko.vcoasters.codecs.EditorCodecs;
import de.schoko.vcoasters.codecs.TrackCodecs;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.packets.OpenEditorToTrackS2C;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class TrackManagerCommands {

	private static final boolean EXPORT_VELOCITIES = true;

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, selection) -> {
			dispatcher.register(
				Commands.literal("editor:open")
					.requires(VCoasters::canUse)
					.then(
						Commands.argument("identifier", IdentifierArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE)
							.executes(ctx -> {
								Identifier identifier = ctx.getArgument("identifier", Identifier.class);
								CompoundTag data = ctx.getSource().getServer().getCommandStorage().get(identifier);
								if (data.isEmpty()) {
									ctx.getSource().sendFailure(Component.literal("Track " + identifier + " not found!"));
									return 0;
								}
								Optional<Integer> dataVersion = data.getInt("data_version");
								if (dataVersion.isEmpty()) return fail(ctx, "Track " + identifier + " provides no data version information!");
								int version = dataVersion.get();
								if (!EditorCodecs.isDataStorageVersionCompatible(version)) return fail(ctx, "Track " + identifier + " has incompatible data version! (Given: " + version + " / Required: " + TrackCodecs.CURRENT_VERSION + ")");

								try {
									Track track = EditorCodecs.loadTrack(data, identifier, version);
									VCoasters.instance.getTrackManager().addTrack(track);
									ServerPlayer player = ctx.getSource().getPlayer();
									if (player != null) {
										ServerPlayNetworking.send(player, new OpenEditorToTrackS2C(track));
									}
								} catch (Exception e) {
									VCoasters.LOGGER.error("Couldn't open track " + identifier, e);
									return fail(ctx, "An error occurred trying to execute that command");
								}
								return 1;
							})
					)
			);
			dispatcher.register(
				Commands.literal("editor:create")
					.requires(VCoasters::canUse)
					.then(
						Commands.argument("identifier", IdentifierArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE)
							.executes(ctx -> {
								Identifier identifier = ctx.getArgument("identifier", Identifier.class);
								CompoundTag data = ctx.getSource().getServer().getCommandStorage().get(identifier);
								if (!data.isEmpty()) {
									Optional<Integer> dataVersion = data.getInt("data_version");
									if (dataVersion.isPresent()) return fail(ctx, "Track " + identifier + " already exists!");
								}
								ServerPlayer player = ctx.getSource().getPlayer();
								if (player == null) {
									return fail(ctx, "Only players can create tracks!");
								}
								Track track = new Track(identifier.toString());
								VCoasters.instance.getTrackManager().addTrack(track);
								ServerPlayNetworking.send(player, new OpenEditorToTrackS2C(track));
								return 1;
							})
						)
				);
			dispatcher.register(
				Commands.literal("editor:tptoline")
					.requires(stack -> stack.isPlayer() && VCoasters.canUse(stack))
					.then(
						Commands.argument("id", StringArgumentType.string())
							.executes(ctx -> {
								ServerPlayer player = ctx.getSource().getPlayer();
								String id = ctx.getArgument("id", String.class);
								Line line = TrackLineManager.getLine(id);

								assert player != null;
								player.teleportTo(line.getCenter().x, line.getCenter().y, line.getCenter().z);

								return 1;
							})
					)
			);
		});
	}

	public static int fail(CommandContext<CommandSourceStack> ctx, String message) {
		ctx.getSource().sendFailure(Component.literal(message));
		return 0;
	}
}