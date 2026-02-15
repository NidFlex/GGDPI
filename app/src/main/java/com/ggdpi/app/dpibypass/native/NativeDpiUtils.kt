package com.ggdpi.app.dpibypass.native

class NativeDpiUtils {
    
    init {
        System.loadLibrary("ggdpi")
    }
    
    external fun extractSni(payload: ByteArray, offset: Int, length: Int): String?
}