package dev.pegasus.whatsappfilerecovery.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Project Name: Save Status
 * Created by: Sohaib Ahmed
 * Created on: 1/8/2025, 9:30 AM
 *
 * Social Links:
 *     https://github.com/epegasus
 *     https://linkedin.com/in/epegasus
 */


/**
 * @return true if device is running API >= 23, Android 6
 */
@SuppressLint("ObsoleteSdkInt")
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
fun hasMarshmallow(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

/**
 * @return true if device is running API >= 24, Android 7
 */
@SuppressLint("ObsoleteSdkInt")
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun hasNougat(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

/**
 * @return true if device is running API >= 25, Android 7.1
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N_MR1)
fun hasNougatMR(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
}

/**
 * @return true if device is running API >= 26, Android 8
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun hasOreo(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

/**
 * @return true if device is running API >= 27, Android 8.1
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun hasOreoMR1(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
}

/**
 * @return true if device is running API >= 28. Android 9
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun hasP(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}

/**
 * @return true if device is running API >= 29, Android 10
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun hasQ(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

/**
 * @return true if device is running API >= 30, Android 11
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun hasR(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}

/**
 * @return true if device is running API >= 31, Android 12
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun hasS(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

/**
 * @return true if device is running API >= 32, Android 12L
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S_V2)
fun hasSV2(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2
}

/**
 * @return true if device is running API >= 33, Android 13
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun hasT(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

/**
 * @return true if device is running API >= 34, Android 14
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun hasU(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}