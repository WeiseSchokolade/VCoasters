package de.schoko.editortestmod.client.editor;

import de.schoko.editortestmod.client.FollowerCar;
import de.schoko.editortestmod.client.RideCar;
import net.minecraft.client.renderer.item.ItemModel;

import java.util.function.Supplier;

public class EditorState {
	public static boolean isPreviewing = false;

	public static boolean endpointRotationMode = false;

	public static Supplier<FollowerCar> followerCarGetter = () -> null;
	public static boolean renderModel = true;
	public static ItemModel followerCarModel;

	public static RideCar rideCar;

	public static boolean doesImGuiCaptureMouseEvents;
}
