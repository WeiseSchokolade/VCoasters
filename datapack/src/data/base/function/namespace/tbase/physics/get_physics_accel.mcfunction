execute if data storage train:storage tbase.line{physics_type:"LIFT"} if score #train_vel train_math_score matches ..500 run return run function base:namespace/current/physics/round_lift_accel
execute if data storage train:storage tbase.line{physics_type:"BRAKE"} if score #train_vel train_math_score matches 1001.. run return run function base:namespace/current/jump_to_input_line
execute if data storage train:storage tbase.line{physics_type:"STATION"} run return run function base:namespace/tbase/physics/station_accel

# Accel length retrieval
execute store result score #acceleration train_math_score run data get storage train:storage tbase.line.acceleration