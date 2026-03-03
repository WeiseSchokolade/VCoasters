from beet import Context, DataPack, DataPackNamespace, Function
from config import parent_namespace, target_namespace, track_storage, train_storage, data_scoreboard, math_scoreboard, train_cart_tag, train_item_model_setup

def generate(ctx: Context):
    source = ctx.data["base"]
    target = ctx.data[parent_namespace]
    target.functions = source.functions
    for file in target.functions.values():
        for i, line in enumerate(file.lines):
            file.lines[i] = parse_line(line)
    source.clear()
    for file_path in target.functions.match("namespace/*"):
        target.functions[target_namespace + file_path[len("namespace"):]] = target.functions[file_path]
        target.functions.pop(file_path)

def parse_line(line: str):
    line = line.replace("track:storage", track_storage)
    line = line.replace("train:storage", train_storage)
    line = line.replace("base:namespace", f"{parent_namespace}:{target_namespace}")
    line = line.replace("train_data_score", data_scoreboard)
    line = line.replace("train_math_score", math_scoreboard)
    line = line.replace("train_cart_tag", train_cart_tag)
    line = line.replace("function item:model_setup", train_item_model_setup)
    return line
