package com.cout970.statistics.network

import com.cout970.statistics.gui.ContainerBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Created by cout970 on 04/08/2016.
 */
//Server -> client
class GuiPacket() : IMessage {

    var nbt: NBTTagCompound? = null

    constructor(data: NBTTagCompound) : this() {
        this.nbt = data
    }

    override fun fromBytes(buf: ByteBuf) {
        nbt = PacketBuffer(buf).readNBTTagCompoundFromBuffer()
    }

    override fun toBytes(buf: ByteBuf) {
        PacketBuffer(buf).writeNBTTagCompoundToBuffer(nbt)
    }

    companion object : IMessageHandler<GuiPacket, IMessage> {

        override fun onMessage(message: GuiPacket, ctx: MessageContext?): IMessage? {
            val container = Minecraft.getMinecraft().thePlayer.openContainer
            if (container is ContainerBase) {
                container.readPacket(message)
            }
            return null
        }
    }
}