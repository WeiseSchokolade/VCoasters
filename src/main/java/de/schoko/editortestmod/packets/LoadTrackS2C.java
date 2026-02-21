package de.schoko.editortestmod.packets;

import de.schoko.editortestmod.EditorTestMod;
import de.schoko.editortestmod.Track;
import de.schoko.editortestmod.codecs.TrackCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@PacketData(name = "load_track_s2c")
public record LoadTrackS2C(Track track) implements CustomPacketPayload {
	public static final Type<LoadTrackS2C> TYPE = Networking.getType(LoadTrackS2C.class);
	public static final StreamCodec<RegistryFriendlyByteBuf, LoadTrackS2C> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(TrackCodecs.CURRENT), LoadTrackS2C::track,
		LoadTrackS2C::new
	);

	@Override
	public Type<LoadTrackS2C> type() {
		return TYPE;
	}
}
