package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.TrainMeta;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import java.util.Optional;

public enum TrainMetaCodecs {
	;
	public static final Codec<TrainMeta> V6 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("model").forGetter(cartModel -> cartModel.getModelId().toString()),
		EditorCodecs.vector3fCodec.optionalFieldOf("offset").forGetter((cartModel) -> Optional.ofNullable(cartModel.getOffset())),
		EditorCodecs.vector3fCodec.optionalFieldOf("pivot").forGetter((cartModel) -> Optional.ofNullable(cartModel.getPivot())),
		Codec.FLOAT.optionalFieldOf("yawOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getYawOffset())),
		Codec.FLOAT.optionalFieldOf("pitchOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getPitchOffset())),
		Codec.FLOAT.optionalFieldOf("rollOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getRollOffset())),
		Codec.INT.optionalFieldOf("segmentAmount").forGetter(cartModel -> cartModel.getSegmentAmount() == 1 ? Optional.empty() : Optional.of(cartModel.getSegmentAmount()))
	).apply(instance, (model, optionalOffset, pivot, yaw, pitch, roll, segmentAmount) ->
		new TrainMeta(Identifier.tryParse(model), 1.5f, optionalOffset.orElse(new Vector3f()), pivot.orElse(new Vector3f()), yaw.orElse(0f), pitch.orElse(0f), roll.orElse(0f), segmentAmount.orElse(1))));

	public static final Codec<TrainMeta> V11 = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("model").forGetter(TrainMeta::getModelId),
		Codec.INT.fieldOf("carDistance").forGetter(trainMeta -> (int) (trainMeta.getCarDistance() * LineCodecs.CURRENT_LINE_LENGTH_MODIFIER)),
		EditorCodecs.vector3fCodec.optionalFieldOf("offset").forGetter((cartModel) -> Optional.ofNullable(cartModel.getOffset())),
		EditorCodecs.vector3fCodec.optionalFieldOf("pivot").forGetter((cartModel) -> Optional.ofNullable(cartModel.getPivot())),
		Codec.FLOAT.optionalFieldOf("yawOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getYawOffset())),
		Codec.FLOAT.optionalFieldOf("pitchOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getPitchOffset())),
		Codec.FLOAT.optionalFieldOf("rollOffset").forGetter(cartModel -> EditorCodecs.emptyOptionalIfZero(cartModel.getRollOffset())),
		Codec.INT.fieldOf("segmentAmount").forGetter(TrainMeta::getSegmentAmount)
	).apply(instance, (model, carDistance, optionalOffset, pivot, yaw, pitch, roll, segmentAmount) ->
		new TrainMeta(model, ((float) carDistance) / LineCodecs.CURRENT_LINE_LENGTH_MODIFIER, optionalOffset.orElse(new Vector3f()), pivot.orElse(new Vector3f()), yaw.orElse(0f), pitch.orElse(0f), roll.orElse(0f), segmentAmount)));
}
