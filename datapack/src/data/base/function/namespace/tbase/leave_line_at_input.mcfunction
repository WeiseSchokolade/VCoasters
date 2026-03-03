execute unless data storage train:storage t1.line.input_line run return run scoreboard players set #dist train_math_score 0
function base:namespace/tbase/jump_to_input_line with storage train:storage t1.line

# Line length retrieval
execute store result score #line_length train_math_score run data get storage train:storage t1.line.length

scoreboard players operation #dist train_math_score += #line_length train_math_score
