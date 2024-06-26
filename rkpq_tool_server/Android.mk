LOCAL_PATH := $(call my-dir)

# rkpq_tool_server
include $(CLEAR_VARS)
LOCAL_MODULE := rkpq_tool_server
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_CHECK_ELF_FILES := false
LOCAL_POST_INSTALL_CMD := cp $(LOCAL_PATH)/pq_setting_config.json $(PRODUCT_OUT)/vendor/etc/;
include $(BUILD_PREBUILT)
