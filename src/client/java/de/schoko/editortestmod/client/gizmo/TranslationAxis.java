package de.schoko.editortestmod.client.gizmo;

import de.schoko.editortestmod.client.core.TargetTester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Function;

public enum TranslationAxis {
		X((center) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return null;
			Vec3 eyePosition = player.getEyePosition();
			Vec3 direction = TargetTester.getMouseViewDirection();
			Vec3 normal = direction.cross(new Vec3(0, 1, 0));

			//double k = -(normal.x * eyePosition.x + normal.y * eyePosition.y + normal.z * eyePosition.z);
			//return normal.y * center.y + normal.z * center.z + k + center.x;

			double offset = normal.dot(eyePosition.scale(-1).add(center.x, center.y, center.z)) / normal.dot(new Vec3(1, 0, 0));

			return new Vector3f(center).add(new Vector3f((float) -offset, 0, 0));
		}),
		Y(center -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return null;
			Vec3 eyePosition = player.getEyePosition();
			Vec3 direction = TargetTester.getMouseViewDirection();
			Vec3 normal = direction.cross(direction.cross(new Vec3(0, 1, 0)));

			double offset = normal.dot(eyePosition.scale(-1).add(center.x, center.y, center.z)) / normal.dot(new Vec3(0, 1, 0));

			return new Vector3f(center).add(0, (float) -offset, 0);
		}),
		Z(center -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return null;
			Vec3 eyePosition = player.getEyePosition();
			Vec3 direction = TargetTester.getMouseViewDirection();
			Vec3 normal = direction.cross(new Vec3(0, 1, 0));

			double offset = normal.dot(eyePosition.scale(-1).add(center.x, center.y, center.z)) / normal.dot(new Vec3(0, 0, 1));

			return new Vector3f(center).add(new Vector3f(0, 0, (float) -offset));
		});

		private final Function<Vector3f, Vector3f> intersection;

		TranslationAxis(Function<Vector3f, Vector3f> intersection) {
			this.intersection = intersection;
		}

		public Vector3f getIntersection(Vector3f center) {
			return intersection.apply(center);
		}
	}