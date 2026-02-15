package com.ggdpi.app.dpibypass

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class ByteBufferPool(private val poolSize: Int, private val bufferSize: Int) {
    
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()
    
    init {
        repeat(poolSize) {
            pool.offer(ByteBuffer.allocateDirect(bufferSize))
        }
    }
    
    fun acquire(): ByteBuffer {
        return pool.poll() ?: ByteBuffer.allocateDirect(bufferSize)
    }
    
    fun release(buffer: ByteBuffer) {
        buffer.clear()
        pool.offer(buffer)
    }
}