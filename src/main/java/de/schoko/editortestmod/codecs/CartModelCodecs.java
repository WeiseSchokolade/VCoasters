package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.CartModel;
import org.joml.Vector3f;

import java.util.Optional;

public enum CartModelCodecs {
	;
	public static final Codec<CartModel> V6 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("model").forGetter(CartModel::getModelId),
		EditorCodecs.vector3fCodec.optionalFieldOf("offset").forGetter((cartModel) -> Optional.ofNullable(cartModel.getOffset())),
		EditorCodecs.vector3fCodec.optionalFieldOf("pivot").forGetter((cartModel) -> Optional.ofNullable(cartModel.getPivot())),
		Codec.FLOAT.optionalFieldOf("yawOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getYawOffset())),
		Codec.FLOAT.optionalFieldOf("pitchOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getPitchOffset())),
		Codec.FLOAT.optionalFieldOf("rollOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getRollOffset())),
		Codec.INT.optionalFieldOf("segmentAmount").forGetter(cartModel -> cartModel.getSegmentAmount() == 1 ? Optional.empty() : Optional.of(cartModel.getSegmentAmount()))
	).apply(instance, (model, optionalOffset, pivot, yaw, pitch, roll, segmentAmount) ->
		new CartModel(model, optionalOffset.orElse(new Vector3f()), pivot.orElse(new Vector3f()), yaw.orElse(0f), pitch.orElse(0f), roll.orElse(0f), segmentAmount.orElse(1))));
}
