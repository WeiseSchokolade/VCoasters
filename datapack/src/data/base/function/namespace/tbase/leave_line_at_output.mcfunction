execute unless data storage train:storage tbase.line.output_line run return run scoreboard players operation #dist train_math_score = #line_length train_math_score
function base:namespace/tbase/jump_to_output_line with storage train:storage tbase.line
scoreboard players operation #dist train_math_score -= #line_length train_math_score

# Line length retrieval
execute store result score #line_length train_math_score run data get storage train:storage tbase.line.length

execute if data storage train:storage tbase.line.on_reach run function base:namespace/call_on_reach with storage train:storage tbase.line