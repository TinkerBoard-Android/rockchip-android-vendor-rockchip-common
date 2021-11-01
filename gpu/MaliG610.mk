ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-G610)
# libs of libGLES_mali.so are installed in ./Android.mk
PRODUCT_PACKAGES += \
        libGLES_mali \
        vulkan.$(TARGET_BOARD_PLATFORM)
endif
