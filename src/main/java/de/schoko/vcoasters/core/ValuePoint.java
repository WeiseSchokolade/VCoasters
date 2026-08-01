package de.schoko.vcoasters.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;

public interface ValuePoint {
	Codec<ValuePoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.fieldOf("x").forGetter(ValuePoint::x),
		Codec.FLOAT.fieldOf("y").forGetter(ValuePoint::y),
		Codec.FLOAT.fieldOf("z").forGetter(ValuePoint::z),
		Codec.FLOAT.fieldOf("yaw").forGetter(ValuePoint::yaw),
		Codec.FLOAT.fieldOf("pitch").forGetter(ValuePoint::pitch),
		Codec.FLOAT.fieldOf("roll").forGetter(ValuePoint::roll)
	).apply(instance, InterpolatedPoint::new));

	float x();
	float y();
	float z();
	float yaw();
	float pitch();
	float roll();

	default ValuePoint valueCopy() {
		return new InterpolatedPoint(x(), y(), z(), yaw(), pitch(), roll());
	}

	default float getX() {
		return x();
	}

	default float getY() {
		return y();
	}

	default float getZ() {
		return z();
	}

	default float getYaw() {
		return yaw();
	}

	default float getPitch() {
		return pitch();
	}

	default float getRoll() {
		return roll();
	}

	default Vector3f posToVector3f() {
		return new Vector3f(x(), y(), z());
	}
}
