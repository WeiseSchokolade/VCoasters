from beet import Context, DataPack, DataPackNamespace, Function
from config import trains, train_start_line_labels, train_segments, train_segment_dist

train_functions = ["tick", "leave_line_at_output", "leave_line_at_input", "jump_to_input_line", "jump_to_output_line", "physics/get_physics_accel", "physics/station_accel", "physics/station_full_stop", "init", "on_halt"]

def train_func(train_name: str, path: str) -> str:
    return f"namespace/{train_name}/{path}"

def generate(ctx: Context):
    target = ctx.data["base"]
    init_function = target.functions["namespace/init"]
    tick_function = target.functions["namespace/tick"]
    spawn_function = target.functions["namespace/spawn_entities"]
    
    init_function.append(f"scoreboard players set #cart_amount train_data_score {train_segments}")
    
    for i in range(trains):
        train_name = f"t{i + 1}"
        for function_name in train_functions:
            parse_file(target, function_name, train_name)
        tick_function.lines.append(f"function base:namespace/{train_name}/tick")
        
        init_function.lines.append(f"""
data modify storage train:storage {train_name} set value {{id:"{train_name}",line:{{}}}}
data modify storage train:storage {train_name}.line set from storage track:storage lines[{{label:"{train_start_line_labels[train_name]}"}}]
scoreboard players set #{train_name}.vel train_data_score 0
scoreboard players set #{train_name}.dist train_data_score 0
scoreboard players set #{train_name}.halting train_data_score -1""")
        
        for i in range(train_segments):
            spawn_function.lines.append(f'summon item_display ~ ~ ~ {{teleport_duration:2,Tags:["train_cart_tag","cart{i + 1}","{train_name}"]}}')    
    spawn_function.lines.append("execute as @e[tag=train_cart_tag] run function item:model_setup")
    
    tick_entity_function = target.functions["namespace/tick_entity"]
    for i in range(train_segments):
        tick_entity_function.lines.append(f'execute if entity @s[tag=cart{i + 1}] run return run data modify entity @s {{}} merge from storage train:storage points[{i}]')
    tick_train_function = target.functions["namespace/tick_train"]
    for i in range(train_segments - 1):
        tick_train_function.lines.append(f"""
scoreboard players remove #dist train_math_score {train_segment_dist}
execute if score #dist train_math_score matches ..-1 run function base:namespace/current/leave_line_at_input
function base:namespace/interpolate
data modify storage train:storage points append from storage train:storage interpolated_point
execute if score #should_calc_total train_math_score matches 1 run function base:namespace/current/physics/get_physics_accel
scoreboard players operation #total_acceleration train_math_score += #acceleration train_math_score""")
    tick_train_function.lines.append("""
scoreboard players operation #acceleration train_math_score = #total_acceleration train_math_score
scoreboard players operation #acceleration train_math_score /= #cart_amount train_data_score

execute if score #train_vel train_math_score matches -100..100 run return 0
scoreboard players operation #applied_friction train_math_score = #train_vel train_math_score
scoreboard players operation #applied_friction train_math_score *= #friction train_data_score
scoreboard players operation #applied_friction train_math_score /= #10000 train_math_score
execute if score #applied_friction train_math_score matches -1..1 run scoreboard players set #applied_friction train_math_score 0
scoreboard players operation #acceleration train_math_score -= #applied_friction train_math_score""")
    
    for file_path in target.functions.match("namespace/tbase/*"):
        target.functions.pop(file_path)

def parse_file(target: DataPackNamespace, function_name: str, train_name: str):
    target.functions[train_func(train_name, function_name)] = target.functions[train_func("tbase", function_name)].copy()
    function: Function = target.functions[train_func(train_name, function_name)]
    for i, line in enumerate(function.lines):
        function.lines[i] = parse_train_line(line, train_name)
    pass

def parse_train_line(line: str, train_name: str) -> str:
    line = line.replace("tbase", train_name)
    return line
