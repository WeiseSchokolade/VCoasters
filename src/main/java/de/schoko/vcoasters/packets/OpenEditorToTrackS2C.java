package de.schoko.vcoasters.packets;

import de.schoko.vcoasters.Track;
import de.schoko.vcoasters.codecs.TrackCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@PacketData(name = "open_editor_to_track_s2c")
public record OpenEditorToTrackS2C(Track track) implements CustomPacketPayload {
	public static final Type<@NotNull OpenEditorToTrackS2C> TYPE = Networking.getType(OpenEditorToTrackS2C.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenEditorToTrackS2C> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(TrackCodecs.CURRENT_CODEC), OpenEditorToTrackS2C::track,
		OpenEditorToTrackS2C::new
	);

	@Override
	public @NotNull Type<@NotNull OpenEditorToTrackS2C> type() {
		return TYPE;
	}
}
