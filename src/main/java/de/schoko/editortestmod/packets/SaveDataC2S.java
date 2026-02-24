package de.schoko.editortestmod.packets;

import de.schoko.editortestmod.codecs.LineCodecs;
import de.schoko.editortestmod.core.Line;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@PacketData(name = "save_data_c2s")
public record SaveDataC2S(String id) implements CustomPacketPayload {
	public static final Type<@NotNull SaveDataC2S> TYPE = Networking.getType(SaveDataC2S.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, SaveDataC2S> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, SaveDataC2S::id,
		SaveDataC2S::new
	);

	@Override
	public @NotNull Type<@NotNull SaveDataC2S> type() {
		return TYPE;
	}
}
