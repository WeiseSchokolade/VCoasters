parent_namespace = "my"
target_namespace = "mouse"

trains = 3
train_start_line_labels = {"t1": "station1", "t2": "station2", "t3": "station3"}
train_segments = 1
train_segment_dist = 15000

train_item_model_setup = 'item replace entity @s container.0 with stone[item_model="editortestmod:ner_front"]'



math_scoreboard = f"{parent_namespace}.{target_namespace}.math"
data_scoreboard = f"{parent_namespace}.{target_namespace}.data"
track_storage = f"minecraft:mouse"
train_storage = f"{parent_namespace}:{target_namespace}"
train_cart_tag = f"{parent_namespace}.{target_namespace}"