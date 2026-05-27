data remove storage train:storage interpolated_point
data remove storage train:storage points
scoreboard objectives remove train_math_score
scoreboard objectives remove train_data_score

execute store result score data_version train_data_score run data get storage track:storage data_version
execute unless score data_version train_data_score matches 11 run return run say Uh oh! Incompatible data version!

scoreboard objectives add train_math_score dummy
scoreboard objectives add train_data_score dummy

scoreboard players set #-1 train_math_score -1
scoreboard players set #10 train_math_score 10
scoreboard players set #1000 train_math_score 1000
scoreboard players set #10000 train_math_score 10000

execute store result score #friction train_data_score run data get storage track:storage friction

data modify storage train:storage interpolated_point set value {Pos:[0d,0d,0d], Rotation: [0f, 0f], transformation: {left_rotation:{axis:[0f, 0f, 1f], angle: 0f}}}

