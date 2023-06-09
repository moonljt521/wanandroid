package com.moon.lib_safe

data class JavaAirBagConfig(
    val crashType: String,
    val crashMessage: String,
    val crashClass: String,
    val crashMethod: String,
    val crashAndroidVersion: Int = 0
)