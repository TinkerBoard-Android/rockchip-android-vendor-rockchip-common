###############################################################################
# libplayreadydrmplugin
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libplayreadydrmplugin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_SRC_FILES := $(LOCAL_MODULE).so
LOCAL_MODULE_SUFFIX := .so
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/vendor/lib/mediadrm
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
