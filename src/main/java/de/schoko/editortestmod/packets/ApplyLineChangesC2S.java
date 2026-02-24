package de.schoko.editortestmod.packets;

import de.schoko.editortestmod.codecs.LineCodecs;
import de.schoko.editortestmod.core.Line;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

@PacketData(name = "apply_line_changes_c2s")
public record ApplyLineChangesC2S(String id, List<Line> lines, List<Line> removedLines) implements CustomPacketPayload {
	public static final Type<ApplyLineChangesC2S> TYPE = Networking.getType(ApplyLineChangesC2S.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyLineChangesC2S> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ApplyLineChangesC2S::id,
		ByteBufCodecs.fromCodec(LineCodecs.CURRENT.listOf()), ApplyLineChangesC2S::lines,
		ByteBufCodecs.fromCodec(LineCodecs.CURRENT.listOf()), ApplyLineChangesC2S::removedLines,
		ApplyLineChangesC2S::new
	);

	@Override
	public Type<ApplyLineChangesC2S> type() {
		return TYPE;
	}
}
