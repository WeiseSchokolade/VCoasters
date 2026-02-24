package de.schoko.editortestmod.packets;

import de.schoko.editortestmod.EditorTestMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class Networking {
	public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType(Class<T> clazz) {
		if (clazz.isAnnotationPresent(PacketData.class)) {
			PacketData annotation = clazz.getAnnotation(PacketData.class);
			return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, annotation.name()));
		} else {
			return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(EditorTestMod.MOD_ID, clazz.getSimpleName()));
		}
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(OpenEditorToTrackS2C.TYPE, OpenEditorToTrackS2C.CODEC);
		PayloadTypeRegistry.playC2S().register(SaveDataC2S.TYPE, SaveDataC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(ApplyLineChangesC2S.TYPE, ApplyLineChangesC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(ApplyTrackMetaChangesC2S.TYPE, ApplyTrackMetaChangesC2S.CODEC);
	}
}
