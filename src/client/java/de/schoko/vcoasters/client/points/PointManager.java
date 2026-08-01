package de.schoko.vcoasters.client.points;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointManager {
	private final List<Point> points;

	public PointManager() {
		this.points = new ArrayList<>();
	}

	public void addPoint(Point point) {
		this.points.add(point);
	}

	public void addPoints(List<Point> points) {
		this.points.addAll(points);
	}

	public List<Point> getBoxes() {
		return points;
	}

	public Point getTargetedPoint() {
		LocalPlayer player = Minecraft.getInstance().player;
		Vec3 from = player.getEyePosition();
		Vec3 direction = player.getViewVector(1);
		double entityInteractionRange = player.entityInteractionRange();
		Vec3 to = from.add(direction.scale(entityInteractionRange));

		double minDistanceSQ = Double.MAX_VALUE;
		Point closesetPoint = null;
		for (Point point : this.points) {
			Optional<Vec3> clip = point.getAABB().clip(from, to);
			if (clip.isPresent()) {
				Vec3 intersectionPoint = clip.get();
				double distanceSQ = intersectionPoint.distanceToSqr(from);
				if (distanceSQ < minDistanceSQ) {
					minDistanceSQ = distanceSQ;
					closesetPoint = point;
				}
			}
		}
		return closesetPoint;
	}

	public boolean targetedPointExists() {
		LocalPlayer player = Minecraft.getInstance().player;
		Vec3 from = player.getEyePosition();
		Vec3 to = from.add(player.getViewVector(1).scale(player.entityInteractionRange()));

		for (Point point : this.points) {
			if (point.getAABB().clip(from, to).isPresent()) {
				return true;
			}
		}
		return false;
	}
}
