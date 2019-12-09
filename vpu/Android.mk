LOCAL_PATH := $(call my-dir)

$(info TARGET_ARCH:$(TARGET_ARCH))
$(info TARGET_2ND_ARCH:$(TARGET_2ND_ARCH) )
$(info TARGET_ARCH:$(TARGET_BOARD_PLATFORM) )

ifneq ($(filter rk3326, $(TARGET_BOARD_PLATFORM)), )
	include $(LOCAL_PATH)/media_rk3326.mk
endif

ifneq ($(filter rk3126c, $(TARGET_BOARD_PLATFORM)), )
	include $(LOCAL_PATH)/media_rk3126c.mk
endif

ifneq ($(filter rk3399 rk3399pro, $(TARGET_BOARD_PLATFORM)), )
	include $(LOCAL_PATH)/media_rk3399.mk
endif

ifneq ($(filter rk3328, $(TARGET_BOARD_PLATFORM)), )
	include $(LOCAL_PATH)/media_rk3328.mk
endif

ifneq ($(filter rk3368, $(TARGET_BOARD_PLATFORM)), )
        include $(LOCAL_PATH)/media_rk3368.mk
endif

ifneq ($(filter rk322x, $(TARGET_BOARD_PLATFORM)), )
        include $(LOCAL_PATH)/media_rk322x.mk
endif
