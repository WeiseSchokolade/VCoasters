package de.schoko.editortestmod.client.core;

import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.core.EditorObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TargetTester {
	static boolean isTargeted(AABB aabb) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;
		Vec3 from = player.getEyePosition();
		Vec3 direction = player.getViewVector(1);
		double entityInteractionRange = player.entityInteractionRange();
		Vec3 to = from.add(direction.scale(entityInteractionRange));

		return aabb.clip(from, to).isPresent();
	}

	static Vec3 getPlayerViewDirection() {
		return new Vec3(EditorTestModClient.instance.getLastCamera().forwardVector());
	}

	static Vec3 getMouseViewDirection() {
		Minecraft minecraftInstance = Minecraft.getInstance();
		MouseHandler handler = minecraftInstance.mouseHandler;
		float x = (float) (handler.xpos() / minecraftInstance.getWindow().getWidth()) * 2f - 1f;
		float y = (float) (handler.ypos() / minecraftInstance.getWindow().getHeight()) * 2f - 1f;
		Matrix4f matrix4f = new Matrix4f(EditorTestModClient.instance.getLastProjectionMatrix());
		matrix4f.invert();
		Vector4f forwards = new Vector4f(x, y, 0f, 1f);
		forwards.mul(matrix4f);
		Vector3f view = new Vector3f(forwards.x, -forwards.y, forwards.z).normalize();
		view.rotate(EditorTestModClient.instance.getLastCamera().rotation());
		//EditorTestModClient.lastResult = rawVec;
		return new Vec3(view).normalize();
	}

	static Vec3 getEyePosition() {
		return EditorTestModClient.instance.getLastCamera().position();
	}

	static Vec3 getCurrentFrom() {
		return getEyePosition();
	}

	static Vec3 getCurrentTo(float d) {
		return getEyePosition().add(getMouseViewDirection().scale(d));
	}

	static AABB getClosestTarget(AABB... aabbs) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return null;
		Vec3 from = player.getEyePosition();
		double entityInteractionRange = player.entityInteractionRange();
		Vec3 to = from.add(getMouseViewDirection().scale(entityInteractionRange));

		double minDistanceSQ = Double.MAX_VALUE;
		AABB closesetAABB = null;
		for (AABB aabb : aabbs) {
			Optional<Vec3> clip = aabb.clip(from, to);
			if (clip.isPresent()) {
				Vec3 intersectionPoint = clip.get();
				double distanceSQ = intersectionPoint.distanceToSqr(from);
				if (distanceSQ < minDistanceSQ) {
					minDistanceSQ = distanceSQ;
					closesetAABB = aabb;
				}
			}
		}
		return closesetAABB;
	}

	private static <T extends IntersectionHandler> QueryResult<T> resolveClosestTarget(T[] handlers) {
		Minecraft minecraftInstance = Minecraft.getInstance();
		LocalPlayer player = minecraftInstance.player;
		if (player == null) return null;
		Vec3 from = player.getEyePosition();

		double entityInteractionRange = player.entityInteractionRange();
		Vec3 to = from.add(getMouseViewDirection().scale(entityInteractionRange));

		double minDistanceSQ = Double.MAX_VALUE;
		T closestHandler = null;
		int closestIndex = -1;

		for (T handler : handlers) {
			for (int i = 0; i < handler.size(); i++) {
				Optional<Vec3> clip = handler.intersectionProvider().getIntersection(i, from, to);
				if (clip.isPresent()) {
					Vec3 intersectionPoint = clip.get();
					double distanceSQ = intersectionPoint.distanceToSqr(from);
					if (distanceSQ < minDistanceSQ) {
						minDistanceSQ = distanceSQ;
						closestHandler = handler;
						closestIndex = i;
					}
				}
			}
		}
		if (closestHandler == null) return null;
		return new QueryResult<>(closestHandler, closestIndex);
	}

	static <T extends EditorObject> Optional<T> getClosestTarget(ProvidingTargetProvider<?>... providers) {
		QueryResult<ProvidingTargetProvider<?>> provider = resolveClosestTarget(providers);
		if (provider == null) return Optional.empty();
		return Optional.of((T) provider.handler.objectProvider.apply(provider.index));
	}

	static boolean consumeClosestTarget(ConsumingTargetProvider<?>... providers) {
		QueryResult<ConsumingTargetProvider<?>> provider = resolveClosestTarget(providers);
		if (provider == null) return false;
		provider.handler.objectConsumer.accept(provider.index);
		return true;
	}

	@FunctionalInterface
	interface IntersectionProvider {
		Optional<Vec3> getIntersection(int index, Vec3 from, Vec3 to);
	}

	static <T extends EditorObject> ProvidingTargetProvider<T> provider(int size, IntersectionProvider intersectionProvider, Function<Integer, T> objectProvider) {
		return new ProvidingTargetProvider<>(size, intersectionProvider, objectProvider);
	}

	static <T> ConsumingTargetProvider<T> consumer(int size, IntersectionProvider intersectionProvider, Consumer<Integer> objectConsumer) {
		return new ConsumingTargetProvider<>(size, intersectionProvider, objectConsumer);
	}

	interface IntersectionHandler {
		int size();
		IntersectionProvider intersectionProvider();
	}

	record ProvidingTargetProvider<T extends EditorObject>(int size, IntersectionProvider intersectionProvider, Function<Integer, T> objectProvider) implements IntersectionHandler {

	}

	record ConsumingTargetProvider<T>(int size, IntersectionProvider intersectionProvider, Consumer<Integer> objectConsumer) implements IntersectionHandler {

	}

	record QueryResult<T extends IntersectionHandler>(T handler, int index) {

	}
}
