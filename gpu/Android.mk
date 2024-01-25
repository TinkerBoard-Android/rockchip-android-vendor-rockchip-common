LOCAL_PATH := $(call my-dir)
# $(info 'in MaliT860.mk')
# $(info TARGET_BOARD_PLATFORM_GPU:$(TARGET_BOARD_PLATFORM_GPU) )
# $(info TARGET_ARCH:$(TARGET_ARCH) )

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-t860)
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliT860/lib/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliT860/lib/$(TARGET_2ND_ARCH)/libGLES_mali.so
LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)
endif # ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-t860)

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-t760)
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_VENDOR_MODULE := true
LOCAL_CHECK_ELF_FILES := false
LOCAL_SRC_FILES := MaliT760/lib/arm/rk3288w/libGLES_mali.so
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)
endif

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-G52)
# install libs of mali_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3562)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib/$(TARGET_2ND_ARCH)/libGLES_mali.so
else

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3576)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_2ND_ARCH)/libGLES_mali.so
else
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52/lib/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52/lib/$(TARGET_2ND_ARCH)/libGLES_mali.so
endif # RK3576

endif # RK3562

LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)

# install libs of vulkan_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := vulkan.$(TARGET_BOARD_PLATFORM)
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3562)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib/$(TARGET_ARCH)/vulkan.mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib/$(TARGET_2ND_ARCH)/vulkan.mali.so
else

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3576)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_ARCH)/vulkan.mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_2ND_ARCH)/vulkan.mali.so
else
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52/lib/$(TARGET_ARCH)/vulkan.mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52/lib/$(TARGET_2ND_ARCH)/vulkan.mali.so
endif # RK3576

endif # RK3562

LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := hw
include $(BUILD_PREBUILT)

# install libs of libgpudataproducer so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libgpudataproducer
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3562)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib/$(TARGET_ARCH)/libgpudataproducer.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib/$(TARGET_2ND_ARCH)/libgpudataproducer.so
else

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3576)
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_ARCH)/libgpudataproducer.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52_for_a53/lib_for_disable_sha1/$(TARGET_2ND_ARCH)/libgpudataproducer.so
else
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG52/lib/$(TARGET_ARCH)/libgpudataproducer.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG52/lib/$(TARGET_2ND_ARCH)/libgpudataproducer.so
endif # RK3576

endif # RK3562

LOCAL_CHECK_ELF_FILES := false
include $(BUILD_PREBUILT)
endif

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali400)
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_VENDOR_MODULE := true
LOCAL_CHECK_ELF_FILES := false
LOCAL_SRC_FILES := Mali400/lib/arm/libGLES_mali.so
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)
endif

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-tDVx)
# install libs of mali_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliTDVx/lib/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliTDVx/lib/$(TARGET_2ND_ARCH)/libGLES_mali.so
LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)

# install libs of vulkan_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := vulkan.$(TARGET_BOARD_PLATFORM)
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliTDVx/lib/$(TARGET_ARCH)/vulkan.mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliTDVx/lib/$(TARGET_2ND_ARCH)/vulkan.mali.so
LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := hw
include $(BUILD_PREBUILT)

endif

# ---------------------------- #

ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), mali-G610)
# install libs of mali_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libGLES_mali
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG610/lib/$(TARGET_ARCH)/libGLES_mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG610/lib/$(TARGET_2ND_ARCH)/libGLES_mali.so
LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := egl
include $(BUILD_PREBUILT)

# install libs of vulkan_so
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := vulkan.$(TARGET_BOARD_PLATFORM)
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG610/lib/$(TARGET_ARCH)/vulkan.mali.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG610/lib/$(TARGET_2ND_ARCH)/vulkan.mali.so
LOCAL_CHECK_ELF_FILES := false
LOCAL_MODULE_RELATIVE_PATH := hw
include $(BUILD_PREBUILT)

# install libs of libgpudataproducer
include $(CLEAR_VARS)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libgpudataproducer
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MULTILIB := both
LOCAL_VENDOR_MODULE := true
LOCAL_SRC_FILES_$(TARGET_ARCH) := MaliG610/lib/$(TARGET_ARCH)/libgpudataproducer.so
LOCAL_SRC_FILES_$(TARGET_2ND_ARCH) := MaliG610/lib/$(TARGET_2ND_ARCH)/libgpudataproducer.so
LOCAL_CHECK_ELF_FILES := false
include $(BUILD_PREBUILT)

endif

# ---------------------------- #
