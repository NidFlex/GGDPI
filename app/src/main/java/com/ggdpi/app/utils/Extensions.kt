package com.ggdpi.app.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun CoroutineScope.launchMain(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.Main, block = block)
}

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.IO, block = block)
}

fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

fun String.fromHex(): ByteArray {
    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}