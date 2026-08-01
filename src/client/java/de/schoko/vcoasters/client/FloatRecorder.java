package de.schoko.vcoasters.client;

import java.util.ArrayList;
import java.util.List;

public record 	FloatRecorder(List<Float> values, int maxValues) {
	public FloatRecorder(int maxValues) {
		this(new ArrayList<>(maxValues), maxValues);
	}

	public int getLength() {
		return values.size();
	}

	public void add(float value) {
		if (values.size() >= maxValues) {
			values.removeFirst();
		}
		values.add(value);
	}

	public float[] getValues() {
		float[] arr = new float[values.size()];
		for (int i = 0; i < values.size(); i++) {
			arr[i] = values.get(i);
		}
		return arr;
	}

	public void reset() {
		values.clear();
	}

	public float getMax() {
		float max = Float.MIN_VALUE;
		for (Float value : values) {
			if (value > max) max = value;
		}
		return max;
	}

	public float getMin() {
		float min = Float.MAX_VALUE;
		for (Float value : values) {
			if (value < min) min = value;
		}
		return min;
	}
}
