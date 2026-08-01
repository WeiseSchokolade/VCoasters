scoreboard players set #target_speed train_math_score 0
execute if score #train_vel train_math_score <= #target_speed train_math_score run return fail
scoreboard players set #acceleration train_math_score -30
#execute if score #train_vel train_math_score < #target_speed train_math_score run scoreboard players operation #acceleration train_math_score *= #-1 train_math_score

scoreboard players operation #temp train_math_score = #train_vel train_math_score
scoreboard players operation #temp train_math_score += #acceleration train_math_score

execute if score #temp train_math_score matches 1.. run return 1
scoreboard players operation #acceleration train_math_score = #temp train_math_score
return 1