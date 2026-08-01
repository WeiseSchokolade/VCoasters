package de.schoko.editortestmod.client.editor;

public class EditorOptions {
	public static boolean autoSnap = true;
	public static int interactionRange = 50;
	public static boolean showAngleSharpness = false;
	public static boolean showRollAngle = false;
	public static int snapSettingIndex;
	public static SnapSetting getSnapSetting() {
		return SnapSetting.values()[snapSettingIndex];
	}
	public enum SnapSetting {
		BLOCK("Block", value -> Math.round(value + 0.5) - 0.5),
		HALF_BLOCK("0.5 blocks", value -> Math.round(2 * value - 0.5) / 2.0),
		PIXEL("Pixels", value -> Math.round(16 * value - 0.5) / 16.0),
		NONE("None", value -> value);

		private final DoubleToDoubleFunction function;
		private final String name;

		SnapSetting(String name, DoubleToDoubleFunction function) {
			this.name = name;
			this.function = function;
		}

		public String getName() {
			return name;
		}

		public double snap(double coordinate) {
			return function.apply(coordinate);
		}

		interface DoubleToDoubleFunction {
			double apply(double value);
		}
	}
}
