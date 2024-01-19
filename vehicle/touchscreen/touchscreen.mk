VENDOR_HIMAX_TP_KO_FILES := $(shell find $(CUR_PATH)/vehicle/touchscreen/ -name "himax_mmi.ko" -type f)
KERNEL_HIMAX_TP_KO_FILES := $(shell find $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/input/touchscreen/ -name "himax_mmi.ko" -type f)
HAVE_KERNEL_HIMAX_KO_FILE := $(shell test -d $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/input/touchscreen/himax_mmi.ko && echo yes)
ifeq ($(HAVE_KERNEL_HIMAX_KO_FILE),yes)
BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(KERNEL_HIMAX_TP_KO_FILES), $(file))
else
BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(VENDOR_HIMAX_TP_KO_FILES), $(file))
endif

VENDOR_ILI210X_TP_KO_FILES := $(shell find $(CUR_PATH)/vehicle/touchscreen/ -name "ili210x.ko" -type f)
KERNEL_ILI210X_TP_KO_FILES := $(shell find $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/input/touchscreen/ -name "ili210x.ko" -type f)
HAVE_KERNEL_ILI210X_KO_FILE := $(shell test -d $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/input/touchscreen/ili210x.ko && echo yes)
ifeq ($(HAVE_KERNEL_ILI210X_KO_FILE),yes)
BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(KERNEL_ILI210X_TP_KO_FILES), $(file))
else
BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(VENDOR_ILI210X_TP_KO_FILES), $(file))
endif
