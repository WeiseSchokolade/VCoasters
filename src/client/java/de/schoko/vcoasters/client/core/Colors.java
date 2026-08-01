package de.schoko.vcoasters.client.core;

import org.joml.Vector4f;

public enum Colors {
	;
	public static final Vector4f
		RED     = new Vector4f(1f, 0f, 0f, 1f),
		GREEN   = new Vector4f(0f, 1f, 0f, 1f),
		BLUE    = new Vector4f(0f, 0f, 1f, 1f),
		YELLOW  = new Vector4f(1f, 1f, 0f, 1f),
		CYAN    = new Vector4f(0f, 1f, 1f, 1f),
		MAGENTA = new Vector4f(1f, 0f, 1f, 1f),

		LIGHT_RED 	= new Vector4f(1f, 0.25f, 0.25f, 1f),
		LIGHT_GREEN = new Vector4f(0.25f, 1f, 0.25f, 1f),
		LIGHT_BLUE  = new Vector4f(0.25f, 0.25f, 1f, 1f),

		LIGHT_YELLOW = new Vector4f(1f, 1f, 0.25f, 1f),
		LIGHT_CYAN = new Vector4f(0.25f, 1f, 1f, 1f),
		LIGHT_MAGENTA = new Vector4f(1f, 0.25f, 1f, 1f),


		WHITE           = monochrome(1f),
		VERY_LIGHT_GRAY = monochrome(0.875f),
		LIGHT_GRAY      = monochrome(0.75f),
		SEMI_LIGHT_GRAY = monochrome(0.625f),
		GRAY            = monochrome(0.5f),
		SEMI_DARK_GRAY  = monochrome(0.375f),
		DARK_GRAY       = monochrome(0.25f),
		VERY_DARK_GRAY  = monochrome(0.125f),
		BLACK           = monochrome(0f)
	;

	private static Vector4f monochrome(float shade) {
		return new Vector4f(shade, shade, shade, 1f);
	}
}
