# $(info 'in MaliT860.mk')
# $(info TARGET_BOARD_PLATFORM_GPU:$(TARGET_BOARD_PLATFORM_GPU) )
# $(info TARGET_ARCH:$(TARGET_ARCH) )

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-t860)

ifeq ($(strip $(BOARD_BUILD_GKI)), true)
# Move to Android.mk
BOARD_VENDOR_KERNEL_MODULES += \
	vendor/rockchip/common/gpu/MaliT860/lib/modules/midgard_kbase.ko
endif

PRODUCT_PACKAGES += \
	libGLES_mali

ifeq ($(strip $(ENABLE_STEREO_DEFORM)), true)
PRODUCT_COPY_FILES += \
	vendor/rockchip/common/gpu/MaliT860/lib/arm/libGLES_mali.so:system/lib/egl/libGLES_mali.so \
	vendor/rockchip/common/gpu/MaliT860/lib/arm64/libGLES_mali.so:system/lib64/egl/libGLES_mali.so
endif

ifneq ($(BUILD_WITH_GOOGLE_MARKET), true)
ifneq ($(DEVICE_IS_64BIT_ONLY), true)
PRODUCT_PACKAGES += \
    rk30board_libGLES_mali_vulkan_symlink32
endif
PRODUCT_PACKAGES += \
    rk30board_libGLES_mali_vulkan_symlink64
endif
ifneq ($(DEVICE_IS_64BIT_ONLY), true)
PRODUCT_PACKAGES += \
    rockchip_libGLES_mali_libOpenCL_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1_symlink32 \
    rockchip_libGLES_mali_libOpenCL.1.1_symlink32
endif
PRODUCT_PACKAGES += \
    rockchip_libGLES_mali_libOpenCL_symlink64 \
    rockchip_libGLES_mali_libOpenCL.1_symlink64 \
    rockchip_libGLES_mali_libOpenCL.1.1_symlink64
endif
