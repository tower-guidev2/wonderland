package org.alice.rabbit.hole.core.surveillance.provider

interface IBuildPropertyProvider {
    fun manufacturer(): String
    fun brand(): String
    fun device(): String
    fun sdkVersion(): Int
}
