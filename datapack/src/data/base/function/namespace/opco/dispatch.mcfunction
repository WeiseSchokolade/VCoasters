data remove storage train:storage t1.line{label:"station"}.fullstop
data remove storage train:storage t2.line{label:"station"}.fullstop

data remove storage track:storage lines[{label:"pre_station"}].fullstop

data remove storage train:storage t1.line{label:"pre_station"}.fullstop
data remove storage train:storage t2.line{label:"pre_station"}.fullstop