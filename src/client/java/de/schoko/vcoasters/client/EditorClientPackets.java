package de.schoko.vcoasters.client;

import de.schoko.vcoasters.packets.OpenEditorToTrackS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class EditorClientPackets {
	public static void registerPackets() {
		register(OpenEditorToTrackS2C.TYPE, (payload, ctx) -> {
			VCoastersClient.instance.openTo(payload.track());
		});
	}

	private static <T extends @NotNull CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, handler);
	}
}
