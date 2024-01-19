SATA_KO_FILES := $(shell find $(TOPDIR)$(PRODUCT_KERNEL_PATH)/drivers/ata -name "*.ko" -type f)

BOARD_VENDOR_KERNEL_MODULES += \
        $(foreach file, $(SATA_KO_FILES), $(file))
