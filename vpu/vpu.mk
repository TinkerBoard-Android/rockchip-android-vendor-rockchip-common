LOCAL_PATH := $(call my-dir)

PRODUCT_PACKAGES += \
    libffmpeg_58 \
    libiconv     \
    librttinyxml \
    librtopus    \
    librtmem     \
    librockit    \
    libmpp

# rk3588/rk3588s
ifneq ($(filter rk3588, $(TARGET_BOARD_PLATFORM)), )
    SOC_PLATFORM := rk3588
# rk3399/rk3399pro
else ifneq ($(filter rk3399, $(TARGET_BOARD_PLATFORM)), )
    SOC_PLATFORM := rk3399
# rk3326/rk3326s
else ifneq ($(filter rk3326, $(TARGET_BOARD_PLATFORM)), )
    SOC_PLATFORM := rk3326
else
    SOC_PLATFORM := $(TARGET_BOARD_PLATFORM)
endif

PRODUCT_COPY_FILES += \
    vendor/rockchip/common/vpu/etc/media_codecs_google_c2_$(SOC_PLATFORM).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_google_c2.xml \
    vendor/rockchip/common/vpu/etc/media_codecs_c2_$(SOC_PLATFORM).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_c2_base.xml \
    vendor/rockchip/common/vpu/etc/media_codecs_performance_$(SOC_PLATFORM).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_performance.xml

# For widevine L1
ifeq ($(BOARD_WIDEVINE_OEMCRYPTO_LEVEL), 1)
    PRODUCT_COPY_FILES += \
        vendor/rockchip/common/vpu/etc/media_codecs_c2_secure.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs.xml \
        vendor/rockchip/common/vpu/etc/media_codecs_secure_$(SOC_PLATFORM).xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs_secure_video.xml
else
    PRODUCT_COPY_FILES += \
        vendor/rockchip/common/vpu/etc/media_codecs_c2_regular.xml:$(TARGET_COPY_OUT_VENDOR)/etc/media_codecs.xml
endif
