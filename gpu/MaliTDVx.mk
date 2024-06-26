ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-tDVx)
PRODUCT_PACKAGES += \
    libGLES_mali \
    libgpudataproducer \
    vulkan.$(TARGET_BOARD_PLATFORM)

ifneq ($(DEVICE_IS_64BIT_ONLY), true)
PRODUCT_PACKAGES += \
    rockchip_libGLES_mali_libOpenCL_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1.1_symlink32
endif
ifeq ($(TARGET_ARCH), arm64)
PRODUCT_PACKAGES += \
    rockchip_libGLES_mali_libOpenCL_symlink64 \
    rockchip_libGLES_mali_libOpenCL.1_symlink64 \
    rockchip_libGLES_mali_libOpenCL.1.1_symlink64
endif
endif
