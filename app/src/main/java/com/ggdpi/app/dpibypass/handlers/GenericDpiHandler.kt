package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager.DpiStrategy

class GenericDpiHandler {

    // ✅ ДОБАВИТЬ эти три метода:
    fun process(packet: ByteArray, length: Int, strategy: DpiStrategy): ByteArray {
        return packet  // заглушка: возвращаем без изменений
    }

    fun processTcp(packet: ByteArray, length: Int, strategy: DpiStrategy, sni: String?): ByteArray {
        return packet  // заглушка
    }

    fun processUdp(packet: ByteArray, length: Int, strategy: DpiStrategy): ByteArray {
        return packet  // заглушка
    }
}