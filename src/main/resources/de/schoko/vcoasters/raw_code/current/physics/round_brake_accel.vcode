scoreboard players set #acceleration train_math_score -500
scoreboard players operation #temp train_math_score = #train_vel train_math_score
scoreboard players operation #temp train_math_score += #acceleration train_math_score

execute unless score #temp train_math_score matches ..1500 run return 1
scoreboard players set #acceleration train_math_score 1500
scoreboard players operation #acceleration train_math_score -= #train_vel train_math_score
