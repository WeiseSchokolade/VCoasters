package de.schoko.editortestmod.packets;

import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.codecs.LineCodecs;
import de.schoko.editortestmod.core.Line;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

@PacketData(name = "apply_track_changes_c2s")
public record ApplyTrackChangesC2S(String id, List<Line> lines, List<Line> removedLines) implements CustomPacketPayload {
	public static final Type<ApplyTrackChangesC2S> TYPE = Networking.getType(ApplyTrackChangesC2S.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyTrackChangesC2S> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ApplyTrackChangesC2S::id,
		ByteBufCodecs.fromCodec(LineCodecs.CURRENT.listOf()), ApplyTrackChangesC2S::lines,
		ByteBufCodecs.fromCodec(LineCodecs.CURRENT.listOf()), ApplyTrackChangesC2S::removedLines,
		ApplyTrackChangesC2S::new
	);

	@Override
	public Type<ApplyTrackChangesC2S> type() {
		return TYPE;
	}
}
