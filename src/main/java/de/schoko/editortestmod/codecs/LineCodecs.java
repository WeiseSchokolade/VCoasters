package de.schoko.editortestmod.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.editortestmod.core.InterpolatedPoint;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.LinePhysicsType;

import java.util.Optional;

public enum LineCodecs {;
	;
	public static final Codec<Line> V1 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		Codec.FLOAT.fieldOf("a_x").forGetter(line -> line.getInputEndPoint().x()),
		Codec.FLOAT.fieldOf("a_y").forGetter(line -> line.getInputEndPoint().y()),
		Codec.FLOAT.fieldOf("a_z").forGetter(line -> line.getInputEndPoint().z()),
		Codec.FLOAT.fieldOf("a_yaw").forGetter(line -> line.getInputEndPoint().yaw()),
		Codec.FLOAT.fieldOf("a_pitch").forGetter(line -> line.getInputEndPoint().pitch()),
		Codec.FLOAT.fieldOf("a_roll").forGetter(line -> line.getInputEndPoint().roll()),
		Codec.FLOAT.fieldOf("b_x").forGetter(line -> line.getOutputEndPoint().x()),
		Codec.FLOAT.fieldOf("b_y").forGetter(line -> line.getOutputEndPoint().y()),
		Codec.FLOAT.fieldOf("b_z").forGetter(line -> line.getOutputEndPoint().z()),
		Codec.FLOAT.fieldOf("b_yaw").forGetter(line -> line.getOutputEndPoint().yaw()),
		Codec.FLOAT.fieldOf("b_pitch").forGetter(line -> line.getOutputEndPoint().pitch()),
		Codec.FLOAT.fieldOf("b_roll").forGetter(line -> line.getOutputEndPoint().roll()),
		Codec.FLOAT.fieldOf("length").forGetter(Line::getLength),
		Codec.STRING.optionalFieldOf("output_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getOutputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(o -> o instanceof Line line && line.getOnReachFunction() != null ? Optional.of(line.getOnReachFunction()) : Optional.empty())
	).apply(instance, (id,
					   aX, aY, aZ, aYaw, aPitch, aRoll,
					   bX, bY, bZ, bYaw, bPitch, bRoll,
					   length, outputLineId, onReachFunction) -> {
		Line line = new Line(id, new InterpolatedPoint(aX, aY, aZ, aYaw, aPitch, aRoll), new InterpolatedPoint(bX, bY, bZ, bYaw, bPitch, bRoll));
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	private static Optional<Integer> emptyOptionalIfZero(int value) {
		if (value == 0) return Optional.empty();
		else return Optional.of(value);
	}

	private static Optional<Float> emptyOptionalIfZero(float value) {
		if (value == 0) return Optional.empty();
		else return Optional.of(value);
	}

	public static final Codec<Line> V2 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		Codec.FLOAT.fieldOf("a_x").forGetter(line -> line.getInputEndPoint().x()),
		Codec.FLOAT.fieldOf("a_y").forGetter(line -> line.getInputEndPoint().y()),
		Codec.FLOAT.fieldOf("a_z").forGetter(line -> line.getInputEndPoint().z()),
		Codec.FLOAT.fieldOf("a_yaw").forGetter(line -> line.getInputEndPoint().yaw()),
		Codec.FLOAT.fieldOf("a_pitch").forGetter(line -> line.getInputEndPoint().pitch()),
		Codec.FLOAT.fieldOf("a_roll").forGetter(line -> line.getInputEndPoint().roll()),
		Codec.FLOAT.optionalFieldOf("b_x").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().x() - line.getInputEndPoint().x())),
		Codec.FLOAT.optionalFieldOf("b_y").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().y() - line.getInputEndPoint().y())),
		Codec.FLOAT.optionalFieldOf("b_z").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().z() - line.getInputEndPoint().z())),
		Codec.FLOAT.optionalFieldOf("b_yaw").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().yaw() - line.getInputEndPoint().yaw())),
		Codec.FLOAT.optionalFieldOf("b_pitch").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().pitch() - line.getInputEndPoint().pitch())),
		Codec.FLOAT.optionalFieldOf("b_roll").forGetter(line -> emptyOptionalIfZero(line.getOutputEndPoint().roll() - line.getInputEndPoint().roll())),
		Codec.FLOAT.fieldOf("length").forGetter(Line::getLength),
		Codec.STRING.optionalFieldOf("output_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getOutputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(o -> o instanceof Line line && line.getOnReachFunction() != null ? Optional.of(line.getOnReachFunction()) : Optional.empty())
	).apply(instance, (id,
					   aX, aY, aZ, aYaw, aPitch, aRoll,
					   bX, bY, bZ, bYaw, bPitch, bRoll,
					   length, outputLineId, onReachFunction) -> {
		Line line = new Line(id, new InterpolatedPoint(aX, aY, aZ, aYaw, aPitch, aRoll), new InterpolatedPoint(aX + bX.orElse(0f), aY + bY.orElse(0f), aZ + bZ.orElse(0f), aYaw + bYaw.orElse(0f), aPitch + bPitch.orElse(0f), aRoll + bRoll.orElse(0f)));
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	private static final float SCALE = 1000;

	private record V3DeltaChange(int aX, int aY, int aZ, int aYaw, int aPitch, int aRoll, int bX, int bY, int bZ, int bYaw, int bPitch, int bRoll) {
		private static int cast(float val) {
			return Math.round(val * SCALE);
		}

		private static int castAngle(float val) {
			return (int) Math.round(Math.toDegrees(val) * SCALE);
		}
		private V3DeltaChange(float aX, float aY, float aZ, float aYaw, float aPitch, float aRoll, float bX, float bY, float bZ, float bYaw, float bPitch, float bRoll) {
			this(cast(aX), cast(aY), cast(aZ), castAngle(aYaw), castAngle(aPitch), castAngle(aRoll), cast(bX), cast(bY), cast(bZ), castAngle(bYaw), castAngle(bPitch), castAngle(bRoll));
		}

		private V3DeltaChange(Line line) {
			this(line.getInputEndPoint().x(), line.getInputEndPoint().y(), line.getInputEndPoint().z(), line.getInputEndPoint().yaw(), line.getInputEndPoint().pitch(), line.getInputEndPoint().roll(),
				line.getOutputEndPoint().x(), line.getOutputEndPoint().y(), line.getOutputEndPoint().z(), line.getOutputEndPoint().yaw(), line.getOutputEndPoint().pitch(), line.getOutputEndPoint().roll());
		}

		private InterpolatedPoint getA() {
			return new InterpolatedPoint(aX / SCALE, aY / SCALE, aZ / SCALE, (float) Math.toRadians(aYaw / SCALE), (float) Math.toRadians(aPitch / SCALE), (float) Math.toRadians(aRoll / SCALE));
		}

		private InterpolatedPoint getB() {
			return new InterpolatedPoint(bX / SCALE, bY / SCALE, bZ / SCALE, (float) Math.toRadians(bYaw / SCALE), (float) Math.toRadians(bPitch / SCALE), (float) Math.toRadians(bRoll / SCALE));
		}

		private static final Codec<V3DeltaChange> V3_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("a_x").forGetter(V3DeltaChange::aX),
			Codec.INT.fieldOf("a_y").forGetter(V3DeltaChange::aY),
			Codec.INT.fieldOf("a_z").forGetter(V3DeltaChange::aZ),
			Codec.INT.fieldOf("a_yaw").forGetter(V3DeltaChange::aYaw),
			Codec.INT.fieldOf("a_pitch").forGetter(V3DeltaChange::aPitch),
			Codec.INT.fieldOf("a_roll").forGetter(V3DeltaChange::aRoll),
			Codec.INT.optionalFieldOf("b_x").forGetter(c -> emptyOptionalIfZero(c.bX - c.aX)),
			Codec.INT.optionalFieldOf("b_y").forGetter(c -> emptyOptionalIfZero(c.bY - c.aY)),
			Codec.INT.optionalFieldOf("b_z").forGetter(c -> emptyOptionalIfZero(c.bZ - c.aZ)),
			Codec.INT.optionalFieldOf("b_yaw").forGetter(c -> emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("b_pitch").forGetter(c -> emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("b_roll").forGetter(c -> emptyOptionalIfZero(c.bRoll - c.aRoll))
		).apply(instance, (aX, aY, aZ, aYaw, aPitch, aRoll, dX, dY, dZ, dYaw, dPitch, dRoll) ->
			new V3DeltaChange(aX, aY, aZ, aYaw, aPitch, aRoll,
				aX + dX.orElse(0), aY + dY.orElse(0), aZ + dZ.orElse(0), aYaw + dYaw.orElse(0), aPitch + dPitch.orElse(0), aRoll + dRoll.orElse(0)
				)));

		private static final Codec<V3DeltaChange> V4_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("a_x").forGetter(c -> emptyOptionalIfZero(c.aX)),
			Codec.INT.optionalFieldOf("a_y").forGetter(c -> emptyOptionalIfZero(c.aY)),
			Codec.INT.optionalFieldOf("a_z").forGetter(c -> emptyOptionalIfZero(c.aZ)),
			Codec.INT.optionalFieldOf("a_yaw").forGetter(c -> emptyOptionalIfZero(c.aYaw)),
			Codec.INT.optionalFieldOf("a_pitch").forGetter(c -> emptyOptionalIfZero(c.aPitch)),
			Codec.INT.optionalFieldOf("a_roll").forGetter(c -> emptyOptionalIfZero(c.aRoll)),
			Codec.INT.optionalFieldOf("dx").forGetter(c -> emptyOptionalIfZero(c.bX - c.aX)),
			Codec.INT.optionalFieldOf("dy").forGetter(c -> emptyOptionalIfZero(c.bY - c.aY)),
			Codec.INT.optionalFieldOf("dz").forGetter(c -> emptyOptionalIfZero(c.bZ - c.aZ)),
			Codec.INT.optionalFieldOf("dyaw").forGetter(c -> emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("dpitch").forGetter(c -> emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("droll").forGetter(c -> emptyOptionalIfZero(c.bRoll - c.aRoll))
		).apply(instance, (aX, aY, aZ, aYaw, aPitch, aRoll, dX, dY, dZ, dYaw, dPitch, dRoll) ->
			new V3DeltaChange(aX.orElse(0), aY.orElse(0), aZ.orElse(0), aYaw.orElse(0), aPitch.orElse(0), aRoll.orElse(0),
				aX.orElse(0) + dX.orElse(0), aY.orElse(0) + dY.orElse(0), aZ.orElse(0) + dZ.orElse(0), aYaw.orElse(0) + dYaw.orElse(0), aPitch.orElse(0) + dPitch.orElse(0), aRoll.orElse(0) + dRoll.orElse(0)
			)));
	}

	private record V5DeltaChange(int aX, int aY, int aZ, int aYaw, int aPitch, int aRoll, int bX, int bY, int bZ, int bYaw, int bPitch, int bRoll) {
		private static int cast(float val) {
			return Math.round(val * SCALE);
		}

		private static int castAngle(float val) {
			return (int) Math.round(Math.toDegrees(val) * SCALE);
		}

		private static int castRoll(float val) {
			return Math.round(val * SCALE);
		}

		private V5DeltaChange(float aX, float aY, float aZ, float aYaw, float aPitch, float aRoll, float bX, float bY, float bZ, float bYaw, float bPitch, float bRoll) {
			this(cast(aX), cast(aY), cast(aZ), castAngle(aYaw), castAngle(aPitch), castRoll(aRoll), cast(bX), cast(bY), cast(bZ), castAngle(bYaw), castAngle(bPitch), castRoll(bRoll));
		}


		private V5DeltaChange(Line line) {
			this(line.getInputEndPoint().x(), line.getInputEndPoint().y(), line.getInputEndPoint().z(), line.getInputEndPoint().yaw(), line.getInputEndPoint().pitch(), line.getInputEndPoint().roll(),
				line.getOutputEndPoint().x(), line.getOutputEndPoint().y(), line.getOutputEndPoint().z(), line.getOutputEndPoint().yaw(), line.getOutputEndPoint().pitch(), line.getOutputEndPoint().roll());
		}

		private InterpolatedPoint getA() {
			return new InterpolatedPoint(aX / SCALE, aY / SCALE, aZ / SCALE, (float) Math.toRadians(aYaw / SCALE), (float) Math.toRadians(aPitch / SCALE), aRoll / SCALE);
		}

		private InterpolatedPoint getB() {
			return new InterpolatedPoint(bX / SCALE, bY / SCALE, bZ / SCALE, (float) Math.toRadians(bYaw / SCALE), (float) Math.toRadians(bPitch / SCALE), bRoll / SCALE);
		}

		private static final Codec<V5DeltaChange> V5_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("a_x").forGetter(c -> emptyOptionalIfZero(c.aX)),
			Codec.INT.optionalFieldOf("a_y").forGetter(c -> emptyOptionalIfZero(c.aY)),
			Codec.INT.optionalFieldOf("a_z").forGetter(c -> emptyOptionalIfZero(c.aZ)),
			Codec.INT.optionalFieldOf("a_yaw").forGetter(c -> emptyOptionalIfZero(c.aYaw)),
			Codec.INT.optionalFieldOf("a_pitch").forGetter(c -> emptyOptionalIfZero(c.aPitch)),
			Codec.INT.optionalFieldOf("a_roll").forGetter(c -> emptyOptionalIfZero(c.aRoll)),
			Codec.INT.optionalFieldOf("dx").forGetter(c -> emptyOptionalIfZero(c.bX - c.aX)),
			Codec.INT.optionalFieldOf("dy").forGetter(c -> emptyOptionalIfZero(c.bY - c.aY)),
			Codec.INT.optionalFieldOf("dz").forGetter(c -> emptyOptionalIfZero(c.bZ - c.aZ)),
			Codec.INT.optionalFieldOf("dyaw").forGetter(c -> emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("dpitch").forGetter(c -> emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("droll").forGetter(c -> emptyOptionalIfZero(c.bRoll - c.aRoll))
		).apply(instance, (aX, aY, aZ, aYaw, aPitch, aRoll, dX, dY, dZ, dYaw, dPitch, dRoll) ->
			new V5DeltaChange(aX.orElse(0), aY.orElse(0), aZ.orElse(0), aYaw.orElse(0), aPitch.orElse(0), aRoll.orElse(0),
				aX.orElse(0) + dX.orElse(0), aY.orElse(0) + dY.orElse(0), aZ.orElse(0) + dZ.orElse(0), aYaw.orElse(0) + dYaw.orElse(0), aPitch.orElse(0) + dPitch.orElse(0), aRoll.orElse(0) + dRoll.orElse(0)
			)));
	}
	public static final Codec<Line> V3 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		V3DeltaChange.V3_CODEC.fieldOf("delta").forGetter(V3DeltaChange::new),
		Codec.FLOAT.fieldOf("length").forGetter(Line::getLength),
		Codec.STRING.optionalFieldOf("output_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getOutputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("input_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getInputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(o -> o instanceof Line line && line.getOnReachFunction() != null ? Optional.of(line.getOnReachFunction()) : Optional.empty())
	).apply(instance, (id, delta,
					   length, outputLineId, inputLineId, onReachFunction) -> {
		Line line = new Line(id, delta.getA(), delta.getB());
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	public static final Codec<Line> V4 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		V3DeltaChange.V4_CODEC.fieldOf("delta").forGetter(V3DeltaChange::new),
		Codec.FLOAT.fieldOf("length").forGetter(line -> line.getLength() * 1000),
		Codec.STRING.optionalFieldOf("output_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getOutputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("input_line").forGetter(o -> o instanceof Line line ? Optional.ofNullable(line.getInputLineId()) : Optional.empty()),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(o -> o instanceof Line line && line.getOnReachFunction() != null ? Optional.of(line.getOnReachFunction()) : Optional.empty())
	).apply(instance, (id, delta,
					   length, outputLineId, inputLineId, onReachFunction) -> {
		Line line = new Line(id, delta.getA(), delta.getB());
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	public static final Codec<Line> V5 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		V5DeltaChange.V5_CODEC.fieldOf("delta").forGetter(V5DeltaChange::new),
		Codec.FLOAT.fieldOf("length").forGetter(line -> line.getLength() * 1000),
		Codec.STRING.optionalFieldOf("output_line").forGetter(line -> Optional.ofNullable(line.getOutputLineId())),
		Codec.STRING.optionalFieldOf("input_line").forGetter(line -> Optional.ofNullable(line.getInputLineId())),
		Codec.STRING.optionalFieldOf("physics_type").forGetter(line -> Optional.ofNullable(line.getPhysicsType() != null ? line.getPhysicsType().name() : null)),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(line -> Optional.ofNullable(line.getOnReachFunction())),
		Codec.FLOAT.optionalFieldOf("acceleration").forGetter(l -> l.isAccelerationCalculated() ? Optional.of((float) (l.getAcceleration())) : Optional.empty())
	).apply(instance, (id, delta,
					   length, outputLineId, inputLineId, physicsType, onReachFunction, acceleration) -> {
		Line line = new Line(id, delta.getA(), delta.getB());
		line.setPhysicsType(physicsType.isPresent() ? LinePhysicsType.valueOf(physicsType.get()) : null);
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	public static final Codec<Line> CURRENT = V5;
}
