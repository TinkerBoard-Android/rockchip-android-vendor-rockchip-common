#PRODUCT_COPY_FILES += \
#        vendor/rockchip/common/security/optee/optee.ko:system/lib/modules/optee.ko \
#	vendor/rockchip/common/security/optee/optee_armtz.ko:system/lib/modules/optee_armtz.ko	
#ifneq ($(filter rk312x rk3126c, $(strip $(TARGET_BOARD_PLATFORM))), )
ifneq ($(filter tablet, $(strip $(TARGET_BOARD_PLATFORM_PRODUCT))), )
PRODUCT_COPY_FILES += \
	vendor/rockchip/common/security/optee/ta/258be795-f9ca-40e6-a8699ce6886c5d5d.ta:vendor/lib/optee_armtz/258be795-f9ca-40e6-a8699ce6886c5d5d.ta	\
	vendor/rockchip/common/security/optee/ta/0b82bae5-0cd0-49a5-9521516dba9c43ba.ta:vendor/lib/optee_armtz/0b82bae5-0cd0-49a5-9521516dba9c43ba.ta	\
	vendor/rockchip/common/security/optee/lib/arm/libkeymaster2.so:vendor/lib/libkeymaster2.so	\
	vendor/rockchip/common/security/optee/lib/arm/libRkTeeKeymaster.so:vendor/lib/libRkTeeKeymaster.so	\
	vendor/rockchip/common/security/optee/lib/arm/libkeymaster_messages2.so:vendor/lib/libkeymaster_messages2.so	\
	vendor/rockchip/common/security/optee/lib/arm/keystore.rk30board.so:vendor/lib/hw/keystore.rk30board.so	\
	vendor/rockchip/common/security/optee/lib/arm/libRkTeeGatekeeper.so:vendor/lib/libRkTeeGatekeeper.so	\
	vendor/rockchip/common/security/optee/lib/arm/librkgatekeeper.so:vendor/lib/librkgatekeeper.so	\
	vendor/rockchip/common/security/optee/lib/arm/gatekeeper.rk30board.so:vendor/lib/hw/gatekeeper.rk30board.so	
#endif

ifeq ($(strip $(TARGET_ARCH)), arm64)
PRODUCT_COPY_FILES += \
	vendor/rockchip/common/security/optee/lib/arm64/libkeymaster2.so:vendor/lib64/libkeymaster2.so	\
	vendor/rockchip/common/security/optee/lib/arm64/libRkTeeKeymaster.so:vendor/lib64/libRkTeeKeymaster.so	\
	vendor/rockchip/common/security/optee/lib/arm64/libkeymaster_messages2.so:vendor/lib64/libkeymaster_messages2.so	\
	vendor/rockchip/common/security/optee/lib/arm64/keystore.rk30board.so:vendor/lib64/hw/keystore.rk30board.so	\
	vendor/rockchip/common/security/optee/lib/arm64/libRkTeeGatekeeper.so:vendor/lib64/libRkTeeGatekeeper.so	\
	vendor/rockchip/common/security/optee/lib/arm64/librkgatekeeper.so:vendor/lib64/librkgatekeeper.so	\
	vendor/rockchip/common/security/optee/lib/arm64/gatekeeper.rk30board.so:vendor/lib64/hw/gatekeeper.rk30board.so
endif
endif

ifeq ($(strip $(TARGET_ARCH)), arm64)
PRODUCT_COPY_FILES += \
	vendor/rockchip/common/security/optee/lib/arm64/tee-supplicant:vendor/bin/tee-supplicant        \
        vendor/rockchip/common/security/optee/lib/arm64/libteec.so:vendor/lib64/libteec.so
else
PRODUCT_COPY_FILES += \
        vendor/rockchip/common/security/optee/lib/arm/tee-supplicant:vendor/bin/tee-supplicant	\
	vendor/rockchip/common/security/optee/lib/arm/libteec.so:vendor/lib/libteec.so
endif

#LOCAL_PATH := $(call my-dir)
#OPTEE_KO_FILES := $(shell ls $(LOCAL_PATH)/*.ko)
#PRODUCT_COPY_FILES += \
#    $(foreach file, $(OPTEE_KO_FILES), $(LOCAL_PATH)/$(file):system/lib/modules/$(file))

# new gatekeeper HAL (atv or box no need)
ifeq ($(filter atv box, $(strip $(TARGET_BOARD_PLATFORM_PRODUCT))), )
PRODUCT_PACKAGES += \
    android.hardware.gatekeeper@1.0-impl \
    android.hardware.gatekeeper@1.0-service
endif
