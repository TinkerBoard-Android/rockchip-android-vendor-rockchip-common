OHCI_KO_FILES := $(shell find $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/usb/host -name "*.ko" -type f)

BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(OHCI_KO_FILES), $(file))
