BOARD_VENDOR_KERNEL_MODULES += \
    vendor/rockchip/common/npu/$(TARGET_BOARD_PLATFORM)/rknpu.ko \
    vendor/rockchip/common/npu/$(TARGET_BOARD_PLATFORM)/rknpu-clang.ko

NpuModelFile := $(shell ls $(CUR_PATH)/npu/rk356x/touch_reading_data)
PRODUCT_COPY_FILES += \
        $(foreach file, $(NpuModelFile), $(CUR_PATH)/npu/rk356x/touch_reading_data/$(file):$(TARGET_COPY_OUT_VENDOR)/etc/touch_reading_data/$(file))
