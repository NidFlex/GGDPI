package com.ggdpi.app.dpibypass

import java.util.concurrent.ConcurrentLinkedQueue

data class Packet(
    val data: ByteArray,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Packet
        return data.contentEquals(other.data)
    }
    
    override fun hashCode(): Int = data.contentHashCode()
}

class PacketQueue(private val maxSize: Int = 1000) {
    
    private val queue = ConcurrentLinkedQueue<Packet>()
    
    fun offer(packet: Packet): Boolean {
        if (queue.size >= maxSize) {
            queue.poll() // Remove oldest
        }
        return queue.offer(packet)
    }
    
    fun poll(): Packet? = queue.poll()
    
    fun peek(): Packet? = queue.peek()
    
    fun isEmpty(): Boolean = queue.isEmpty()
    
    fun size(): Int = queue.size
    
    fun clear() = queue.clear()
}