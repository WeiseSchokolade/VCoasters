#scoreboard players set acceleration train_math_score 500
scoreboard players set #target_speed train_math_score 500
execute if data storage train:storage tbase.line{fullstop:true} if function base:namespace/tbase/physics/station_full_stop run return 1
scoreboard players set #acceleration train_math_score 0

execute if score #train_vel train_math_score <= #target_speed train_math_score run scoreboard players set #acceleration train_math_score 30

scoreboard players operation #temp train_math_score = #train_vel train_math_score
scoreboard players operation #temp train_math_score += #acceleration train_math_score

execute if score #temp train_math_score <= #target_speed train_math_score run return 1
scoreboard players operation #temp train_math_score = #target_speed train_math_score
scoreboard players operation #temp train_math_score -= #train_vel train_math_score
scoreboard players operation #acceleration train_math_score = #temp train_math_score
