

PRODUCT_PACKAGES += \
    RKUpdateService

ifeq ($(strip $(TARGET_BOARD_HARDWARE)), rk30board)
ifeq ($(strip $(TARGET_ARCH)), arm)
PRODUCT_COPY_FILES += \
       vendor/rockchip/common/apps/RKUpdateService/lib/arm/librockchip_update_jni.so:system/lib/librockchip_update_jni.so
else ifeq ($(strip $(TARGET_ARCH)), arm64)
PRODUCT_COPY_FILES += \
       vendor/rockchip/common/apps/RKUpdateService/lib/arm/librockchip_update_jni.so:system/lib/librockchip_update_jni.so \
       vendor/rockchip/common/apps/RKUpdateService/lib/arm64/librockchip_update_jni.so:system/lib64/librockchip_update_jni.so
endif
endif

ifneq ($(strip $(TARGET_BOARD_PLATFORM_PRODUCT)), vr)
PRODUCT_PACKAGES += \
    RkApkinstaller  \
    userExperienceService
ifneq ($(strip $(TARGET_BOARD_PLATFORM)), sofia3gr)
PRODUCT_PACKAGES += \
    MediaFloat      \
endif

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3288)
PRODUCT_PACKAGES += \
    WinStartService \
    projectX
endif

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk312x)
PRODUCT_PACKAGES += \
    RkVideoPlayer
else
ifeq ($(strip $(TARGET_BOARD_PLATFORM_PRODUCT)), box)
PRODUCT_PACKAGES += \
    RkBoxVideoPlayer
else
ifeq ($(strip $(PRODUCT_BUILD_MODULE)), px5car)
PRODUCT_PACKAGES += \
	Rk3grVideoPlayer
else
PRODUCT_PACKAGES += \
    Rk4kVideoPlayer
endif
endif
endif
endif

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), sofia3gr)
PRODUCT_PACKAGES += \
    Rk3grExplorer
else
ifeq ($(strip $(PRODUCT_BUILD_MODULE)), px5car)
PRODUCT_PACKAGES += \
    Rk3grExplorer
else
PRODUCT_PACKAGES += \
    RkExplorer
endif
endif
endif



ifeq ($(strip $(BOARD_HAS_STRESSTEST_APP)), true)
    PRODUCT_PACKAGES += \
    StressTest \
    DeviceTest
endif


ifeq ($(strip $(TARGET_BOARD_PLATFORM)), sofia3gr)
PRODUCT_PACKAGES += \
    ituxd\
    com.rockchip.alarmhelper \
    RFTest

#PRODUCT_COPY_FILES += \
#        $(LOCAL_PATH)/ituxd/lib/x86/libthermalJNI.so:system/lib/libthermalJNI.so
endif

##################for vr app#####################
ifeq ($(strip $(TARGET_BOARD_PLATFORM_PRODUCT)), vr)
PRODUCT_PACKAGES += \
		RockVRHome	\
		RKVRSettings	\
		RKVRExplorer	\
		RKVRGlobalActions	\
                RKVRPanorama	\
		RKVRPlayer	
endif

###########for box app ################
ifeq ($(strip $(TARGET_BOARD_PLATFORM_PRODUCT)), box)
PRODUCT_PACKAGES += \
    AllApp	\
    eHomeMediaCenter_box	\
    ITVLauncher	\
    RkMusic	\
    Settings2
  ifeq ($(strip $(BOARD_USE_LOW_MEM256)), true)
        PRODUCT_PACKAGES += \
              SimpleLauncher
  endif
endif
