execute if score #train_vel train_math_score matches 500 run return run scoreboard players set #acceleration train_math_score 0

scoreboard players set #acceleration train_math_score 50
scoreboard players operation #temp train_math_score = #train_vel train_math_score
scoreboard players operation #temp train_math_score += #acceleration train_math_score

execute if score #temp train_math_score matches ..500 run return 1
execute store result score #acceleration train_math_score run scoreboard players remove #temp train_math_score 500
