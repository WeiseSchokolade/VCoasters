package de.schoko.vcoasters.packets;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.codecs.TrackCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@PacketData(name = "apply_track_meta_changes_c2s")
public record ApplyTrackMetaChangesC2S(Track track) implements CustomPacketPayload {
	public static final Type<@NotNull ApplyTrackMetaChangesC2S> TYPE = Networking.getType(ApplyTrackMetaChangesC2S.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyTrackMetaChangesC2S> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(TrackCodecs.CURRENT_CODEC), ApplyTrackMetaChangesC2S::track,
		ApplyTrackMetaChangesC2S::new
	);

	@Override
	public @NotNull Type<@NotNull ApplyTrackMetaChangesC2S> type() {
		return TYPE;
	}
}
