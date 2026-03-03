scoreboard players operation #total_acceleration train_math_score = #acceleration train_math_score

data modify storage train:storage points set value []

function base:namespace/current/load_interpolation_data

function base:namespace/interpolate
data modify storage train:storage points append from storage train:storage interpolated_point