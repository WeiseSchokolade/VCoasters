scoreboard players operation #t train_math_score = #dist train_math_score
scoreboard players operation #t train_math_score *= #1000 train_math_score
scoreboard players operation #t train_math_score /= #line_length train_math_score

scoreboard players operation #out train_math_score = #dx train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
execute store result storage train:storage interpolated_point.Pos[0] double 0.000001 run scoreboard players operation #out train_math_score += #x train_math_score

scoreboard players operation #out train_math_score = #dy train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
execute store result storage train:storage interpolated_point.Pos[1] double 0.000001 run scoreboard players operation #out train_math_score += #y train_math_score

scoreboard players operation #out train_math_score = #dz train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
execute store result storage train:storage interpolated_point.Pos[2] double 0.000001 run scoreboard players operation #out train_math_score += #z train_math_score

scoreboard players operation #out train_math_score = #dyaw train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
scoreboard players operation #out train_math_score /= #1000 train_math_score
execute store result storage train:storage interpolated_point.Rotation[0] float 0.001 run scoreboard players operation #out train_math_score += #yaw train_math_score

scoreboard players operation #out train_math_score = #dpitch train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
scoreboard players operation #out train_math_score /= #1000 train_math_score
execute store result storage train:storage interpolated_point.Rotation[1] float 0.001 run scoreboard players operation #out train_math_score += #pitch train_math_score

scoreboard players operation #out train_math_score = #droll train_math_score
scoreboard players operation #out train_math_score *= #t train_math_score
scoreboard players operation #out train_math_score /= #1000 train_math_score
execute store result storage train:storage interpolated_point.transformation.left_rotation.angle float 0.001 run scoreboard players operation #out train_math_score += #roll train_math_score

