ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-t760)
PRODUCT_PACKAGES += \
    libGLES_mali \
    rk30board_libGLES_mali_vulkan_symlink32 \
    rockchip_libGLES_mali_libOpenCL_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1.1_symlink32
endif
