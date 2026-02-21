package de.schoko.editortestmod.client;

import de.schoko.editortestmod.packets.LoadTrackS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class EditorClientPackets {
	public static void registerPackets() {
		register(LoadTrackS2C.TYPE, (payload, ctx) -> {
			EditorTestModClient.instance.getEditorCtx().load(payload.track());
		});
	}

	private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, handler);
	}
}
