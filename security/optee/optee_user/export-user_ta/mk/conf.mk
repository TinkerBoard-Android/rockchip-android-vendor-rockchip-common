sm := ta_arm32
sm-ta_arm32 := y
CFG_TA_FLOAT_SUPPORT := y
CFG_ARM32_ta_arm32 := y
ta_arm32-platform-cppflags := -DARM32=1 -D__ILP32__=1
ta_arm32-platform-cflags := -mcpu=cortex-a15 -O0 -g3 -fpie -mthumb -mthumb-interwork -fno-short-enums -fno-common -mno-unaligned-access -mfloat-abi=hard -funsafe-math-optimizations
ta_arm32-platform-aflags := -mcpu=cortex-a15
CROSS_COMPILE32 ?= $(CROSS_COMPILE)
CROSS_COMPILE_ta_arm32 ?= $(CROSS_COMPILE32)

