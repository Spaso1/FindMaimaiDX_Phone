package org.astral.findmaimaiultra.utill;

import android.os.Build;

public class DeviceInfoUtils {

    /**
     * 获取设备的机型
     *
     * @return 设备的机型
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备的Android版本号
     *
     * @return 设备的Android版本号
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取设备的Android版本号（整数形式）
     *
     * @return 设备的Android版本号（整数形式）
     */
    public static int getAndroidVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取完整的设备信息
     *
     * @return 包含机型和Android版本的字符串
     */
    public static String getDeviceInfo() {
        return "设备机型: " + getDeviceModel() + ", Android版本: " + getAndroidVersion();
    }
}
