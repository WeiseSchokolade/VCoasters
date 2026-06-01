parent_namespace = "vrs"
target_namespace = "coaster"

trains = 2
train_start_line_labels = {"t1": "station", "t2": "droptrack"}
train_segments = 5
train_segment_dist = 35000

train_item_model_setup = 'item replace entity @s container.0 with stone[item_model="vrs:front"]'



math_scoreboard = f"{parent_namespace}.{target_namespace}.math"
data_scoreboard = f"{parent_namespace}.{target_namespace}.data"
track_storage = f"minecraft:coaster"
train_storage = f"{parent_namespace}:{target_namespace}"
train_cart_tag = f"{parent_namespace}.{target_namespace}"