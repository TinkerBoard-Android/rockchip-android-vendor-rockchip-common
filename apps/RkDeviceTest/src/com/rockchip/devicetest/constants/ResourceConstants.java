package com.rockchip.devicetest.constants;

import android.os.SystemProperties;

public class ResourceConstants {

	public static final String AGING_CONFIG_PRODUCT_FILE = "agingconfig_" + SystemProperties.get("ro.product.name") + ".ini";

	public static final String AGING_CONFIG_FILE = "agingconfig.ini";

}
