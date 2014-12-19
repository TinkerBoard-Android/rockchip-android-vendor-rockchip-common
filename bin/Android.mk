###############################################################################
# All static executable files
# We only have arm version now
###############################################################################
LOCAL_PATH := $(call my-dir)
#ifeq ($(strip $(TARGET_ARCH)), arm)

###############################################################################
# busybox
include $(CLEAR_VARS)
LOCAL_MODULE := busybox
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_SRC_FILES := $(TARGET_ARCH)/$(LOCAL_MODULE)
include $(BUILD_PREBUILT)

###############################################################################
# mkdosfs
include $(CLEAR_VARS)
LOCAL_MODULE := mkdosfs
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT_SBIN)
LOCAL_SRC_FILES := $(TARGET_ARCH)/$(LOCAL_MODULE)
include $(BUILD_PREBUILT)

###############################################################################
# sdtool
include $(CLEAR_VARS)
LOCAL_MODULE := sdtool
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT_SBIN)
LOCAL_SRC_FILES := $(TARGET_ARCH)/$(LOCAL_MODULE)
include $(BUILD_PREBUILT)

#endif
