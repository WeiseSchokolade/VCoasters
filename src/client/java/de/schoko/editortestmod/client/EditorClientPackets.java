package de.schoko.editortestmod.client;

import de.schoko.editortestmod.packets.OpenEditorToTrackS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class EditorClientPackets {
	public static void registerPackets() {
		register(OpenEditorToTrackS2C.TYPE, (payload, ctx) -> {
			EditorTestModClient.instance.openTo(payload.track());
		});
	}

	private static <T extends @NotNull CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, handler);
	}
}
