execute if data storage train:storage current_line{physics_type:"LIFT"} if score #train_vel train_math_score matches ..1000 run return run function base:namespace/current/physics/round_lift_accel
execute if data storage train:storage current_line{physics_type:"BRAKE"} if score #train_vel train_math_score matches 1501.. run return run function base:namespace/current/physics/round_brake_accel

# Accel length retrieval
execute store result score #acceleration train_math_score run data get storage train:storage current_line.acceleration