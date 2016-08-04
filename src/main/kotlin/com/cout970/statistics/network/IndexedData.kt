package com.cout970.statistics.network

import io.netty.buffer.ByteBuf

/**
 * Created by cout970 on 04/08/2016.
 */
class IndexedData {

    private val map = mutableMapOf<Int, Any>()

    fun setBoolean(id: Int, value: Boolean) {
        map.put(id, value)
    }

    fun setFloat(id: Int, value: Float) {
        map.put(id, value)
    }

    fun setInt(id: Int, value: Int) {
        map.put(id, value)
    }

    operator fun get(id: Int): Any {
        if (id !in map) throw IllegalArgumentException("Invalid ID: $id")
        return map[id]!!
    }


    fun fromBytes(buf: ByteBuf) {
        val size = buf.readInt()
        for (i in 0 until size) {
            val type = buf.readByte().toInt()
            val id = buf.readInt()
            when (type) {
                1 -> map.put(id, buf.readBoolean())
                2 -> map.put(id, buf.readFloat())
                3 -> map.put(id, buf.readInt())
                else -> throw IllegalStateException("Unknown type: $type")
            }
        }
    }

    fun toBytes(buf: ByteBuf) {
        buf.writeInt(map.size)
        for ((id, value) in map) {
            when (value) {
                is Boolean -> {
                    buf.writeByte(1)
                    buf.writeInt(id)
                    buf.writeBoolean(value)
                }
                is Float -> {
                    buf.writeByte(2)
                    buf.writeInt(id)
                    buf.writeFloat(value)
                }
                is Int -> {
                    buf.writeByte(3)
                    buf.writeInt(id)
                    buf.writeInt(value)
                }
                else -> throw IllegalStateException("Unknown type: $value, class:${value.javaClass}")
            }
        }
    }
}