package org.alice.rabbit.hole.core.surveillance.provider

import android.os.Build

class BuildPropertyProvider : IBuildPropertyProvider {
    override fun manufacturer(): String = Build.MANUFACTURER
    override fun brand(): String = Build.BRAND
    override fun device(): String = Build.DEVICE
    override fun sdkVersion(): Int = Build.VERSION.SDK_INT
}
