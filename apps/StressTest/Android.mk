###############################################################################
# StressTest
LOCAL_PATH := $(call my-dir)

ifeq ($(strip $(TARGET_BOARD_HARDWARE)), rk30board)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PACKAGE_NAME := StressTest
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_DEX_PREOPT := false

LOCAL_REQUIRED_MODULES := \
    getbootmode.sh

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := getbootmode.sh
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
endif

