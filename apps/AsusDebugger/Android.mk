LOCAL_PATH:= $(call my-dir)
###############################################################################
include $(CLEAR_VARS)

LOCAL_MODULE := AsusDebugger
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false

include $(BUILD_PREBUILT)

###############################################################################
include $(CLEAR_VARS)

LOCAL_MODULE := klogger
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_PRELINK_MODULE := false
LOCAL_CERTIFICATE := platform

include $(BUILD_PREBUILT)

###############################################################################
include $(CLEAR_VARS)

LOCAL_MODULE := tcpdump2
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_PRELINK_MODULE := false
LOCAL_CERTIFICATE := platform

include $(BUILD_PREBUILT)

###############################################################################
include $(CLEAR_VARS)

LOCAL_MODULE := asus-debugger-d
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_VENDOR_EXECUTABLES)
LOCAL_PRELINK_MODULE := false
LOCAL_CERTIFICATE := platform

include $(BUILD_PREBUILT)

# Debugger_WifiOnly.conf might be different by branches.
include $(CLEAR_VARS)

ifeq ($(TARGET_PRODUCT), $(filter $(TARGET_PRODUCT),rk3288))
	LOCAL_MODULE := debugger.conf
else
	LOCAL_MODULE := debugger_WifiOnly.conf
endif

ifeq ($(TARGET_PRODUCT),CN_Zenbo)
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/CN/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/CN/debug/$(LOCAL_MODULE)
	endif
else ifeq ($(TARGET_PRODUCT),CN1_Zenbo)
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/CN1/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/CN1/debug/$(LOCAL_MODULE)
	endif
else ifeq ($(TARGET_PRODUCT), $(filter $(TARGET_PRODUCT),nicola CN_Nicola))
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/Nicola/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/Nicola/debug/$(LOCAL_MODULE)
	endif
else ifeq ($(TARGET_PRODUCT),rk3288)
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/Tinker_Board/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/Tinker_Board/debug/$(LOCAL_MODULE)
	endif
else ifeq ($(TARGET_PRODUCT), $(filter $(TARGET_PRODUCT),hugo TW_Hugo CN_Hugo HK_Hugo))
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/Hugo/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/Hugo/debug/$(LOCAL_MODULE)
	endif
else ifeq ($(TARGET_PRODUCT), $(filter $(TARGET_PRODUCT),WW_Tinker_Board_2))
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/Tinker_Board_2/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/Tinker_Board_2/debug/$(LOCAL_MODULE)
	endif
else
	ifeq ($(TARGET_BUILD_VARIANT),user)
		LOCAL_SRC_FILES := config/COMMON/user/$(LOCAL_MODULE)
	else
		LOCAL_SRC_FILES := config/COMMON/debug/$(LOCAL_MODULE)
	endif
endif
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_TAGS := optional
LOCAL_PRELINK_MODULE := false
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)