CUR_PATH := vendor/rockchip/common/bluetooth

ifeq ($(strip $(BLUETOOTH_USE_BPLUS)),true)
PRODUCT_PACKAGES += \
	libbt-client-api \
	com.broadcom.bt \
	com.broadcom.bt.xml
endif

#PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/realtek/firmware/rtl8723b_config:system/etc/firmware/rtl8723b_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723b_fw:system/etc/firmware/rtl8723b_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8723bs_config:system/etc/firmware/rtl8723bs_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723bs_fw:system/etc/firmware/rtl8723bs_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8723bs_VQ0_config:system/etc/firmware/rtl8723bs_VQ0_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723bs_VQ0_fw:system/etc/firmware/rtl8723bs_VQ0_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8723bu_config:system/etc/firmware/rtl8723bu_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723d_config:system/etc/firmware/rtl8723d_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723d_fw:system/etc/firmware/rtl8723d_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8723ds_config:system/etc/firmware/rtl8723ds_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8723ds_fw:system/etc/firmware/rtl8723ds_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761a_config:system/etc/firmware/rtl8761a_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8761a_fw:system/etc/firmware/rtl8761a_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761at_config:system/etc/firmware/rtl8761at_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8761at_fw:system/etc/firmware/rtl8761at_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761au8192ee_fw:system/etc/firmware/rtl8761au8192ee_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761au8812ae_fw:system/etc/firmware/rtl8761au8812ae_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761au_fw:system/etc/firmware/rtl8761au_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8761aw8192eu_config:system/etc/firmware/rtl8761aw8192eu_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8761aw8192eu_fw:system/etc/firmware/rtl8761aw8192eu_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8821a_config:system/etc/firmware/rtl8821a_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8821a_fw:system/etc/firmware/rtl8821a_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8821as_config:system/etc/firmware/rtl8821as_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8821as_fw:system/etc/firmware/rtl8821as_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8821c_config:system/etc/firmware/rtl8821c_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8821c_fw:system/etc/firmware/rtl8821c_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8821cs_config:system/etc/firmware/rtl8821cs_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8821cs_fw:system/etc/firmware/rtl8821cs_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8822b_config:system/etc/firmware/rtl8822b_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8822b_fw:system/etc/firmware/rtl8822b_fw \
	$(LOCAL_PATH)/realtek/firmware/rtl8822bs_config:system/etc/firmware/rtl8822bs_config \
	$(LOCAL_PATH)/realtek/firmware/rtl8822bs_fw:system/etc/firmware/rtl8822bs_fw

BT_FIRMWARE_FILES := $(shell ls $(CUR_PATH)/lib/firmware)
PRODUCT_COPY_FILES += \
    $(foreach file, $(BT_FIRMWARE_FILES), $(CUR_PATH)/lib/firmware/$(file):vendor/firmware/$(file))

#include vendor/rockchip/common/bluetooth/console_start_bt/console_start_bt.mk

