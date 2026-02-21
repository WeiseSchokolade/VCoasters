package de.schoko.editortestmod.client.editor;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.schoko.editortestmod.client.EditorTestModClient;
import de.schoko.editortestmod.client.FollowerCar;
import de.schoko.editortestmod.client.RideCar;
import de.schoko.editortestmod.core.EditorObject;
import de.schoko.editortestmod.core.EndPoint;
import de.schoko.editortestmod.core.Line;
import de.schoko.editortestmod.core.LinePhysicsType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EditorCommands {
	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
			dispatcher.register(
				ClientCommandManager.literal("editor:setcar")
					.then(ClientCommandManager.literal("model")
						.then(ClientCommandManager.argument("identifier", new IdentifierArgument())
							.executes(ctx -> {
								FollowerCar car = EditorState.followerCarGetter.get();
								if (car == null) {
									ctx.getSource().sendError(Component.literal("No car spawned!"));
									return 1;
								}
								Identifier identifier = ctx.getArgument("identifier", Identifier.class);
								car.setModel(identifier);
								ctx.getSource().sendFeedback(Component.literal("Car spawned"));
								return 1;
							})
						)
					)
					.then(ClientCommandManager.literal("offset")
						.then(ClientCommandManager.argument("offsetValue", new Vec3Argument(false))
							.executes(ctx -> {
								FollowerCar car = EditorState.followerCarGetter.get();
								if (car == null) {
									ctx.getSource().sendError(Component.literal("No car spawned!"));
									return 1;
								}
								Coordinates coordinates = ctx.getArgument("offsetValue", Coordinates.class);
								if (coordinates instanceof LocalCoordinates) {
									ctx.getSource().sendError(Component.literal("Cannot use local coordinates in offset!"));
								} else if (coordinates instanceof WorldCoordinates(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z)) {
									Vector3f offset = new Vector3f((float) x.value(), (float) y.value(), (float) z.value());
									car.setModelOffset(offset);
									ctx.getSource().sendFeedback(Component.literal("Set offset: " + offset));
								} else {
									ctx.getSource().sendError(Component.literal("Unknown coordinate format!"));
								}
								return 1;
							})
						)
					)
					.then(ClientCommandManager.literal("pivot")
						.then(ClientCommandManager.argument("pivotValue", new Vec3Argument(false))
							.executes(ctx -> {
								FollowerCar car = EditorState.followerCarGetter.get();
								if (car == null) {
									ctx.getSource().sendError(Component.literal("No car spawned!"));
									return 1;
								}
								Coordinates coordinates = ctx.getArgument("pivotValue", Coordinates.class);
								if (coordinates instanceof LocalCoordinates) {
									ctx.getSource().sendError(Component.literal("Cannot use local coordinates for pivot!"));
								} else if (coordinates instanceof WorldCoordinates(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z)) {
									Vector3f offset = new Vector3f((float) x.value(), (float) y.value(), (float) z.value());
									car.setModelOffset(offset);
									ctx.getSource().sendFeedback(Component.literal("Set pivot: " + offset));
								} else {
									ctx.getSource().sendError(Component.literal("Unknown coordinate format!"));
								}
								return 1;
							})
						)
					)
			);

			dispatcher.register(
				ClientCommandManager.literal("editor:selectedline")
					.then(ClientCommandManager.literal("get")
						.then(
							ClientCommandManager.literal("id")
								.executes(ctx -> {
									Line line = verifyLineInput(ctx);
									if (line == null) return 0;
									String id = line.getId();
									ctx.getSource().sendFeedback(Component.literal("Selected line's id: ")
										.append(ComponentUtils.copyOnClickText(id)));
									return 1;
								})
						)
						.then(
							ClientCommandManager.literal("onReachFunction")
								.executes(ctx -> {
									Line line = verifyLineInput(ctx);
									if (line == null) return 0;
									String onReachFunction = line.getOnReachFunction();
									ctx.getSource().sendFeedback(Component.literal("Selected line's reach function: ")
										.append(ComponentUtils.copyOnClickText(onReachFunction)));
									return 1;
								})
						)
					)
					.then(
						ClientCommandManager.literal("set")
							.then(
								ClientCommandManager.literal("outputLine")
									.then(
										ClientCommandManager.argument("id", StringArgumentType.string())
											.executes(ctx -> {
												Line line = verifyLineInput(ctx);
												if (line == null) return 0;
												String id = ctx.getArgument("id", String.class);
												Line outputLine = EditorTestModClient.instance.getEditorCtx().getLineManager().getLine(id);
												if (outputLine == null) {
													ctx.getSource().sendError(Component.literal("Unknown output line '" + id + "'"));
													return 0;
												}
												line.setOutputLine(outputLine);
												ctx.getSource().sendFeedback(Component.literal("Set selected line's output line to ''" + id + "'"));
												return 1;
											})
									)
							)
							.then(
								ClientCommandManager.literal("onReachFunction")
									.then(
										ClientCommandManager.argument("name", StringArgumentType.string())
											.executes(ctx -> {
												Line line = verifyLineInput(ctx);
												if (line == null) return 0;
												String name = ctx.getArgument("name", String.class);
												line.setOnReachFunction(name.isBlank() ? null : name);
												line.markRendererAsDirty();
												ctx.getSource().sendFeedback(Component.literal("Set selected line's on reach function to ''" + name + "'"));
												return 1;
											})
									)
							)
							.then(
								ClientCommandManager.literal("physicsType")
									.then(
										ClientCommandManager.argument("type", StringArgumentType.word()).suggests((context1, builder) -> {
											for (LinePhysicsType value : LinePhysicsType.values()) {
												if (value.name().toLowerCase().startsWith(builder.getRemainingLowerCase())) builder.suggest(value.name().toLowerCase());
											}
											return builder.buildFuture();
										}).executes(ctx -> {
											Line line = verifyLineInput(ctx);
											if (line == null) return 0;
											String physicsTypeName = ctx.getArgument("type", String.class).toUpperCase();
											line.setPhysicsType(LinePhysicsType.valueOf(physicsTypeName));
											line.markRendererAsDirty();
											ctx.getSource().sendFeedback(Component.literal("Set selected line's physics type to '" + physicsTypeName + "'"));
											return 1;
										})
									)
							)
					)
			);
			dispatcher.register(
				ClientCommandManager.literal("editor:selectedendpoint")
					.then(ClientCommandManager.literal("set")
						.then(
							ClientCommandManager.literal("rotation")
								.then(
									ClientCommandManager.argument("yaw", FloatArgumentType.floatArg())
										.then(
											ClientCommandManager.argument("pitch", FloatArgumentType.floatArg())
												.then(
													ClientCommandManager.argument("roll", FloatArgumentType.floatArg())
														.executes(ctx -> {
															EndPoint endPoint = verifyEndpointInput(ctx);
															if (endPoint == null) return 0;

															float yaw = ctx.getArgument("yaw", Float.class);
															float pitch = ctx.getArgument("pitch", Float.class);
															float roll = ctx.getArgument("roll", Float.class);
															endPoint.setYaw((float) Math.toRadians(yaw));
															endPoint.setPitch((float) Math.toRadians(pitch));
															endPoint.setRoll((float) Math.toRadians(roll));
															ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint to " + yaw + " " + pitch + " " + roll));
															return 1;
														})
												)
												.executes(ctx -> {
													EndPoint endPoint = verifyEndpointInput(ctx);
													if (endPoint == null) return 0;

													float yaw = ctx.getArgument("yaw", Float.class);
													float pitch = ctx.getArgument("pitch", Float.class);
													endPoint.setYaw((float) Math.toRadians(yaw));
													endPoint.setPitch((float) Math.toRadians(pitch));
													ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint to " + yaw + " " + pitch));
													return 1;
												})
										)
								)
								.then(
									ClientCommandManager.literal("fromPlayer")
										.executes(ctx -> {
											EndPoint endPoint = verifyEndpointInput(ctx);
											if (endPoint == null) return 0;
											LocalPlayer player = ctx.getSource().getPlayer();
											if (player == null) return 0;

											float yaw = player.getYRot();
											float pitch = player.getXRot();
											endPoint.setYaw(yaw);
											endPoint.setPitch(pitch);
											ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint from ").append(player.getDisplayName()));
											return 1;
										})
								)
								.then(
									ClientCommandManager.literal("fromTangent")
										.executes(ctx -> {
											EndPoint endPoint = verifyEndpointInput(ctx);
											if (endPoint == null) return 0;
											Vector3f direction = getAverageDirection(endPoint);

											float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
											float pitch = (float) -Math.asin(direction.y);

											endPoint.setYaw(yaw);
											endPoint.setPitch(pitch);
											endPoint.updateCorrespondingEndpoint();
											ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint to " + yaw + " " + pitch));
											return 1;
										})
								)
								.then(
									ClientCommandManager.literal("fromWeightedTangent")
										.executes(ctx -> {
											EndPoint endPoint = verifyEndpointInput(ctx);
											if (endPoint == null) return 0;
											Vector3f direction = getWeightedAverageDirection(endPoint);

											float yaw = (float) (Math.atan2(direction.z, direction.x) - Math.PI * 0.5);
											float pitch = (float) -Math.asin(direction.y);

											endPoint.setYaw(yaw);
											endPoint.setPitch(pitch);
											endPoint.updateCorrespondingEndpoint();
											ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint to " + yaw + " " + pitch));
											return 1;
										})
								)
								.then(ClientCommandManager.literal("recursiveFromTangent")
									.executes(ctx -> {
										EndPoint endPoint = verifyEndpointInput(ctx);
										if (endPoint == null) return 0;
										Line originLine = endPoint.getLine();
										if (!endPoint.isOutputEndPoint()) {
											endPoint = originLine.getOutputEndPoint();
										}
										Line currentLine = originLine;
										Function<EndPoint, Vector3f> directionFunction = EditorCommands::getWeightedAverageDirection;

										Vector3f previousDirection = directionFunction.apply(endPoint);
										double previousYaw = Math.atan2(previousDirection.z, previousDirection.x) - Math.PI * 0.5;
										double previousPitch = -Math.asin(previousDirection.y);

										double accumulatedDYaw = previousYaw;
										double accumulatedDPitch = previousPitch;

										endPoint.setYaw((float) accumulatedDYaw);
										endPoint.setPitch((float) accumulatedDPitch);
										endPoint.updateCorrespondingEndpoint();

										int i = 0;
										while ((currentLine = currentLine.getOutputLine()) != null && currentLine != originLine && (i++) < 10000) {
											EndPoint outputEndPoint = currentLine.getOutputEndPoint();
											Vector3f direction = directionFunction.apply(outputEndPoint);
											double yaw = Math.atan2(direction.z, direction.x) - Math.PI * 0.5;
											double pitch = -Math.asin(direction.y);
											double dYaw = yaw - previousYaw;
											if (dYaw < -1 * Math.PI) {
												dYaw += 2 * Math.PI;
											}
											double dPitch = pitch - previousPitch;
											if (dYaw > 1 * Math.PI) {
												dYaw -= 2 * Math.PI;
											}
											accumulatedDYaw += dYaw;
											accumulatedDPitch += dPitch;

											previousYaw = yaw;
											previousPitch = pitch;

											outputEndPoint.setYaw((float) accumulatedDYaw);
											outputEndPoint.setPitch((float) accumulatedDPitch);
											outputEndPoint.updateCorrespondingEndpoint();
										}
										if (i >= 10000) ctx.getSource().sendFeedback(Component.literal("Broke out of algorithm to protect from infinite loop!"));

										if (currentLine == originLine) {
											EndPoint inputEndPoint = originLine.getInputEndPoint();
											Vector3f direction = directionFunction.apply(endPoint);
											double yaw = Math.atan2(direction.z, direction.x) - Math.PI * 0.5;
											double pitch = -Math.asin(direction.y);
											inputEndPoint.setYaw((float) yaw);
											inputEndPoint.setPitch((float) pitch);
										}

										ctx.getSource().sendFeedback(Component.literal("Applied recursive weighted tangent rotation algorithm!"));
										return 1;
									}))
						)
					)
					.then(ClientCommandManager.literal("copyRotationFromPlayer")
						.executes(ctx -> {
							EndPoint endPoint = verifyEndpointInput(ctx);
							if (endPoint == null) return 0;
							LocalPlayer player = ctx.getSource().getPlayer();
							if (player == null) return 0;

							float yaw = player.getYRot();
							float pitch = player.getXRot();
							endPoint.setYaw(yaw);
							endPoint.setPitch(pitch);
							ctx.getSource().sendFeedback(Component.literal("Set rotation of endpoint from ").append(player.getDisplayName()));
							return 1;
						})
					)
					.then(ClientCommandManager.literal("updateCorrespondingEndpoint")
						.executes(ctx -> {
							EndPoint endPoint = verifyEndpointInput(ctx);
							if (endPoint == null) return 0;

							endPoint.updateCorrespondingEndpoint();
							ctx.getSource().sendFeedback(Component.literal("Tried to update corresponding endpoint"));
							return 1;
						})
					)
			);

			dispatcher.register(
				ClientCommandManager.literal("editor:simulate")
					.executes(ctx -> {
						Line line = verifyLineInput(ctx);
						if (line == null) return 0;
						EditorTestModClient.instance.getEditorCtx().getTrack().setAcceleration(0.5, 1.0 / 20);
						EditorState.rideCar = new RideCar(line);
						ctx.getSource().sendFeedback(Component.literal("Spawned ride simulation!"));
						return 1;
					})
			);
		});
	}

	private static Line verifyLineInput(CommandContext<FabricClientCommandSource> ctx) {
		EditorObject object = EditorTestModClient.instance.getEditorCtx().getSelectedObject();
		if (object == null) {
			ctx.getSource().sendError(Component.literal("No line selected!"));
			return null;
		}
		if (!(object instanceof Line line)) {
			ctx.getSource().sendError(Component.literal("Selected object is not a line!"));
			return null;
		}
		return line;
	}

	private static EndPoint verifyEndpointInput(CommandContext<FabricClientCommandSource> ctx) {
		EditorObject object = EditorTestModClient.instance.getEditorCtx().getSelectedObject();
		if (object == null) {
			ctx.getSource().sendError(Component.literal("Nothing selected!"));
			return null;
		}
		if (!(object instanceof EndPoint endPoint)) {
			ctx.getSource().sendError(Component.literal("Selected object is not an endpoint!"));
			return null;
		}
		return endPoint;
	}

	private static Vector3f getAverageDirection(EndPoint endPoint) {
		Line line = endPoint.getLine();
		Vector3f direction = endPoint.getLine().getDirection(1f);
		if (endPoint.isOutputEndPoint()) {
			if (line.getOutputLine() != null) {
				direction.add(line.getOutputLine().getDirection(1f));
				direction.div(2);
			}
		} else {
			if (line.getInputLine() != null) {
				direction.add(line.getInputLine().getDirection(1f));
				direction.div(2);
			}
		}
		direction.normalize();
		return direction;
	}

	private static Vector3f getWeightedAverageDirection(EndPoint endPoint) {
		Line line = endPoint.getLine();
		Vector3f direction = line.getDirection(1f).mul(line.getLength());
		if (endPoint.isOutputEndPoint()) {
			if (line.getOutputLine() != null) {
				direction.add(line.getOutputLine().getDirection(1f).mul(line.getOutputLine().getLength()));
			}
		} else {
			if (line.getInputLine() != null) {
				direction.add(line.getInputLine().getDirection(1f).mul(line.getInputLine().getLength()));
			}
		}
		direction.normalize();
		return direction;
	}
}
