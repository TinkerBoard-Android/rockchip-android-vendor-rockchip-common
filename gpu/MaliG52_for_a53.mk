ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3562)

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-G52)

# libs of libGLES_mali.so are installed in ./Android.mk
PRODUCT_PACKAGES += \
        libGLES_mali \
        libgpudataproducer \
        vulkan.$(TARGET_BOARD_PLATFORM)

ifeq ($(strip $(BOARD_BUILD_GKI)), true)
BOARD_VENDOR_KERNEL_MODULES += \
	vendor/rockchip/common/gpu/MaliG52_for_a53/lib/modules/bifrost_kbase.ko
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
endif
