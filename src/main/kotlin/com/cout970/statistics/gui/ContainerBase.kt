package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.network.GuiPacket
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by cout970 on 04/08/2016.
 */
abstract class ContainerBase(
        val world: World,
        val pos: BlockPos,
        val player: EntityPlayer
) : Container() {

    override fun canInteractWith(playerIn: EntityPlayer?): Boolean = true

    override fun detectAndSendChanges() {
        super.detectAndSendChanges()
        if(player is EntityPlayerMP) {
            val packet = writePacket()
            if (packet != null) {
                Statistics.NETWORK.sendTo(packet, player)
            }
        }
    }

    abstract fun readPacket(pkt: GuiPacket)

    abstract fun writePacket(): GuiPacket?
}