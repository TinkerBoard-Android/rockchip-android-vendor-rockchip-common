ifeq ($(strip $(BUILD_WITH_GO_OPT)),true)
    PRODUCT_PACKAGES += CaptivePortalLoginFrameworkOverlayGo
    PRODUCT_PACKAGES += RockchipNetworkStackConfigGoOverlay
else
    PRODUCT_PACKAGES += RockchipNetworkStackConfigOverlay
endif

PRODUCT_PACKAGES += RockchipTetheringConfigOverlay
PRODUCT_PACKAGES += RockchipWifiConfigOverlay


