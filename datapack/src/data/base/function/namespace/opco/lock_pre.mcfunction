# Engage pre station brake

data modify storage track:storage lines[{label:"pre_station"}].fullstop set value 1b

data modify storage train:storage t1.line{label:"pre_station"}.fullstop set value 1b
data modify storage train:storage t2.line{label:"pre_station"}.fullstop set value 1b