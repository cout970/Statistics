package com.cout970.statistics.network

import com.cout970.statistics.tileentity.TileBase
import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Created by cout970 on 05/08/2016.
 */
class ClientEventPacket() : IMessage {

    var dimension = 0
    var pos = BlockPos.ORIGIN!!
    var id = 0
    var value = 0

    constructor(dim: Int, pos: BlockPos, id: Int, value: Int) : this() {
        dimension = dim
        this.pos = pos
        this.id = id
        this.value = value
    }

    override fun fromBytes(buf: ByteBuf?) {
        buf?.run {
            dimension = readInt()
            id = readInt()
            value = readInt()
        }
        PacketBuffer(buf).run {
            pos = readBlockPos()
        }
    }

    override fun toBytes(buf: ByteBuf?) {
        //I'm an idiot
        buf?.run {
            writeInt(dimension)
            writeInt(id)
            writeInt(value)
        }
        PacketBuffer(buf).run {
            writeBlockPos(pos)
        }
    }

    companion object : IMessageHandler<ClientEventPacket, IMessage> {

        override fun onMessage(msg: ClientEventPacket, ctx: MessageContext?): IMessage? {
            val world = DimensionManager.getWorld(msg.dimension)
            val tile = world.getTileEntity(msg.pos)
            if (tile is TileBase) {
                tile.receiveMsg(msg.id, msg.value)
            }

            return null
        }
    }
}