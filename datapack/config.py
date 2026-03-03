parent_namespace = "ner"
target_namespace = "ride"

trains = 2
train_start_line_labels = {"t1": "station", "t2": "pre_station"}
train_segments = 8
train_segment_dist = 15000

train_item_model_setup = 'item replace entity @s container.0 with stone[item_model="editortestmod:ner_front"]'



math_scoreboard = f"{parent_namespace}.{target_namespace}.math"
data_scoreboard = f"{parent_namespace}.{target_namespace}.data"
track_storage = f"track:{parent_namespace}"
train_storage = f"{parent_namespace}:{target_namespace}"
train_cart_tag = f"{parent_namespace}.{target_namespace}"