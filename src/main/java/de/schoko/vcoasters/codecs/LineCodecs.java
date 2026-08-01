package de.schoko.vcoasters.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.schoko.vcoasters.core.InterpolatedPoint;
import de.schoko.vcoasters.core.Line;
import de.schoko.vcoasters.core.LinePhysicsType;

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

		private static final Codec<V3DeltaChange> V4_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("a_x").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aX)),
			Codec.INT.optionalFieldOf("a_y").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aY)),
			Codec.INT.optionalFieldOf("a_z").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aZ)),
			Codec.INT.optionalFieldOf("a_yaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aYaw)),
			Codec.INT.optionalFieldOf("a_pitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aPitch)),
			Codec.INT.optionalFieldOf("a_roll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aRoll)),
			Codec.INT.optionalFieldOf("dx").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bX - c.aX)),
			Codec.INT.optionalFieldOf("dy").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bY - c.aY)),
			Codec.INT.optionalFieldOf("dz").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bZ - c.aZ)),
			Codec.INT.optionalFieldOf("dyaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("dpitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("droll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bRoll - c.aRoll))
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
			Codec.INT.optionalFieldOf("a_x").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aX)),
			Codec.INT.optionalFieldOf("a_y").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aY)),
			Codec.INT.optionalFieldOf("a_z").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aZ)),
			Codec.INT.optionalFieldOf("a_yaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aYaw)),
			Codec.INT.optionalFieldOf("a_pitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aPitch)),
			Codec.INT.optionalFieldOf("a_roll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aRoll)),
			Codec.INT.optionalFieldOf("dx").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bX - c.aX)),
			Codec.INT.optionalFieldOf("dy").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bY - c.aY)),
			Codec.INT.optionalFieldOf("dz").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bZ - c.aZ)),
			Codec.INT.optionalFieldOf("dyaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("dpitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("droll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bRoll - c.aRoll))
		).apply(instance, (aX, aY, aZ, aYaw, aPitch, aRoll, dX, dY, dZ, dYaw, dPitch, dRoll) ->
			new V5DeltaChange(aX.orElse(0), aY.orElse(0), aZ.orElse(0), aYaw.orElse(0), aPitch.orElse(0), aRoll.orElse(0),
				aX.orElse(0) + dX.orElse(0), aY.orElse(0) + dY.orElse(0), aZ.orElse(0) + dZ.orElse(0), aYaw.orElse(0) + dYaw.orElse(0), aPitch.orElse(0) + dPitch.orElse(0), aRoll.orElse(0) + dRoll.orElse(0)
			)));
	}

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
		Codec.FLOAT.fieldOf("length").forGetter(line -> line.getLength() * 1000), // Increase to LINE_LENGTH_MODIFIER
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

	private record V8DeltaChange(int aX, int aY, int aZ, int aYaw, int aPitch, int aRoll, int bX, int bY, int bZ, int bYaw, int bPitch, int bRoll) {
		private static final float COORDINATE_SCALE = 1000000;

		private static int castCoord(float val) {
			return Math.round(val * COORDINATE_SCALE);
		}

		private static int castAngle(float val) {
			return (int) Math.round(Math.toDegrees(val) * SCALE);
		}

		private static int castRoll(float val) {
			return Math.round(val * SCALE);
		}

		private V8DeltaChange(float aX, float aY, float aZ, float aYaw, float aPitch, float aRoll, float bX, float bY, float bZ, float bYaw, float bPitch, float bRoll) {
			this(castCoord(aX), castCoord(aY), castCoord(aZ), castAngle(aYaw), castAngle(aPitch), castRoll(aRoll), castCoord(bX), castCoord(bY), castCoord(bZ), castAngle(bYaw), castAngle(bPitch), castRoll(bRoll));
		}


		private V8DeltaChange(Line line) {
			this(line.getInputEndPoint().x(), line.getInputEndPoint().y(), line.getInputEndPoint().z(), line.getInputEndPoint().yaw(), line.getInputEndPoint().pitch(), line.getInputEndPoint().roll(),
				line.getOutputEndPoint().x(), line.getOutputEndPoint().y(), line.getOutputEndPoint().z(), line.getOutputEndPoint().yaw(), line.getOutputEndPoint().pitch(), line.getOutputEndPoint().roll());
		}

		private InterpolatedPoint getA() {
			return new InterpolatedPoint(aX / COORDINATE_SCALE, aY / COORDINATE_SCALE, aZ / COORDINATE_SCALE, (float) Math.toRadians(aYaw / SCALE), (float) Math.toRadians(aPitch / SCALE), aRoll / SCALE);
		}

		private InterpolatedPoint getB() {
			return new InterpolatedPoint(bX / COORDINATE_SCALE, bY / COORDINATE_SCALE, bZ / COORDINATE_SCALE, (float) Math.toRadians(bYaw / SCALE), (float) Math.toRadians(bPitch / SCALE), bRoll / SCALE);
		}

		private static final Codec<V8DeltaChange> V8_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("x").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aX)),
			Codec.INT.optionalFieldOf("y").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aY)),
			Codec.INT.optionalFieldOf("z").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aZ)),
			Codec.INT.optionalFieldOf("yaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aYaw)),
			Codec.INT.optionalFieldOf("pitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aPitch)),
			Codec.INT.optionalFieldOf("roll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.aRoll)),
			Codec.INT.optionalFieldOf("dx").forGetter(c -> EditorCodecs.emptyOptionalIfZero(Math.round((c.bX - c.aX) / 1000f))),
			Codec.INT.optionalFieldOf("dy").forGetter(c -> EditorCodecs.emptyOptionalIfZero(Math.round((c.bY - c.aY) / 1000f))),
			Codec.INT.optionalFieldOf("dz").forGetter(c -> EditorCodecs.emptyOptionalIfZero(Math.round((c.bZ - c.aZ) / 1000f))),
			Codec.INT.optionalFieldOf("dyaw").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bYaw - c.aYaw)),
			Codec.INT.optionalFieldOf("dpitch").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bPitch - c.aPitch)),
			Codec.INT.optionalFieldOf("droll").forGetter(c -> EditorCodecs.emptyOptionalIfZero(c.bRoll - c.aRoll))
		).apply(instance, (aX, aY, aZ, aYaw, aPitch, aRoll, dX, dY, dZ, dYaw, dPitch, dRoll) ->
			new V8DeltaChange(aX.orElse(0), aY.orElse(0), aZ.orElse(0), aYaw.orElse(0), aPitch.orElse(0), aRoll.orElse(0),
				aX.orElse(0) + dX.orElse(0) * 1000, aY.orElse(0) + dY.orElse(0) * 1000, aZ.orElse(0) + dZ.orElse(0) * 1000, aYaw.orElse(0) + dYaw.orElse(0), aPitch.orElse(0) + dPitch.orElse(0), aRoll.orElse(0) + dRoll.orElse(0)
			)));

	}

	public static final Codec<Line> V8 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		V8DeltaChange.V8_CODEC.fieldOf("delta").forGetter(V8DeltaChange::new),
		Codec.FLOAT.fieldOf("length").forGetter(line -> line.getLength() * 10000),
		Codec.STRING.optionalFieldOf("label").forGetter(line -> Optional.ofNullable(line.getLabel() != null && !line.getLabel().isBlank() ? line.getLabel() : null)),
		Codec.STRING.optionalFieldOf("output_line").forGetter(line -> Optional.ofNullable(line.getOutputLineId())),
		Codec.STRING.optionalFieldOf("input_line").forGetter(line -> Optional.ofNullable(line.getInputLineId())),
		Codec.STRING.optionalFieldOf("physics_type").forGetter(line -> Optional.ofNullable(line.getPhysicsType() != null ? line.getPhysicsType().name() : null)),
		Codec.BOOL.optionalFieldOf("fullstop").forGetter(line -> Optional.ofNullable(line.getPhysicsType() != null && line.getPhysicsType().supportsFullstop() ? line.isFullStop() : null)),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(line -> Optional.ofNullable(line.getOnReachFunction())),
		Codec.STRING.optionalFieldOf("on_halt").forGetter(line -> Optional.ofNullable(line.getOnHaltFunction())),
		Codec.FLOAT.optionalFieldOf("acceleration").forGetter(l -> l.isAccelerationCalculated() ? Optional.of((float) (l.getAcceleration() * 10000)) : Optional.empty())
	).apply(instance, (id, delta,
					   lengthIgnored, label, outputLineId, inputLineId, physicsType, fullStop, onReachFunction, onHaltFunction, accelerationIgnored) -> {
		Line line = new Line(id, delta.getA(), delta.getB());
		line.setLabel(label.orElse(null));
		line.setPhysicsType(physicsType.isPresent() ? LinePhysicsType.valueOf(physicsType.get()) : null);
		line.setFullStop(line.getPhysicsType() != null && line.getPhysicsType().supportsFullstop() && fullStop.isPresent() ? fullStop.get() : false);
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOnHaltFunction(onHaltFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));


	public static final int V10_LINE_LENGTH_MODIFIER = 10000;

	public static final Codec<Line> V10 = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(Line::getId),
		V8DeltaChange.V8_CODEC.fieldOf("delta").forGetter(V8DeltaChange::new),
		Codec.FLOAT.fieldOf("length").forGetter(line -> line.getLength() * V10_LINE_LENGTH_MODIFIER),
		Codec.STRING.optionalFieldOf("label").forGetter(line -> Optional.ofNullable(line.getLabel() != null && !line.getLabel().isBlank() ? line.getLabel() : null)),
		Codec.STRING.optionalFieldOf("output_line_id").forGetter(line -> Optional.ofNullable(line.getOutputLineId())),
		Codec.STRING.optionalFieldOf("output_line").forGetter(line -> Optional.ofNullable(line.getOutputLine() != null ? line.getOutputLine().getLabel() != null ? line.getOutputLine().getLabel() : line.getOutputLine().getId() : null)),
		Codec.STRING.optionalFieldOf("input_line_id").forGetter(line -> Optional.ofNullable(line.getInputLineId())),
		Codec.STRING.optionalFieldOf("input_line").forGetter(line -> Optional.ofNullable(line.getInputLine() != null ? line.getInputLine().getLabel() != null ? line.getInputLine().getLabel() : line.getInputLine().getId() : null)),
		Codec.STRING.optionalFieldOf("physics_type").forGetter(line -> Optional.ofNullable(line.getPhysicsType() != null ? line.getPhysicsType().name() : null)),
		Codec.BOOL.optionalFieldOf("fullstop").forGetter(line -> Optional.ofNullable(line.getPhysicsType() != null && line.getPhysicsType().supportsFullstop() ? line.isFullStop() : null)),
		Codec.STRING.optionalFieldOf("on_reach").forGetter(line -> Optional.ofNullable(line.getOnReachFunction())),
		Codec.STRING.optionalFieldOf("on_halt").forGetter(line -> Optional.ofNullable(line.getOnHaltFunction())),
		Codec.FLOAT.optionalFieldOf("acceleration").forGetter(l -> l.isAccelerationCalculated() ? Optional.of((float) (l.getAcceleration() * V10_LINE_LENGTH_MODIFIER)) : Optional.empty())
	).apply(instance, (id, delta,
	                   lengthIgnored, label, outputLineId, outputLine, inputLineId, inputLine, physicsType, fullStop, onReachFunction, onHaltFunction, accelerationIgnored) -> {
		Line line = new Line(id, delta.getA(), delta.getB());
		line.setLabel(label.orElse(null));
		line.setPhysicsType(physicsType.isPresent() ? LinePhysicsType.valueOf(physicsType.get()) : null);
		line.setFullStop(line.getPhysicsType() != null && line.getPhysicsType().supportsFullstop() && fullStop.isPresent() ? fullStop.get() : false);
		line.setOnReachFunction(onReachFunction.orElse(null));
		line.setOnHaltFunction(onHaltFunction.orElse(null));
		line.setOutputLineId(outputLineId.orElse(null));
		return line;
	}));

	public static final int CURRENT_LINE_LENGTH_MODIFIER = V10_LINE_LENGTH_MODIFIER;
	public static final Codec<Line> CURRENT = V10;
}
