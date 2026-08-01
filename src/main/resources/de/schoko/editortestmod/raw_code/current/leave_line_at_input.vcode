execute unless data storage train:storage current_line.input_line run return run scoreboard players set #dist train_math_score 0
function base:namespace/current/jump_to_input_line with storage train:storage current_line

# Line length retrieval
execute store result score #line_length train_math_score run data get storage train:storage current_line.length

scoreboard players operation #dist train_math_score += #line_length train_math_score

execute if score #dist train_math_score matches ..-1 run function base:namespace/current/leave_line_at_input
function base:namespace/current/load_interpolation_data
