package com.cout970.statistics.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

/**
 * Created by cout970 on 04/08/2016.
 */
object GuiHandler : IGuiHandler {

    override fun getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? {
        if (ID == 0) {
            return GuiController(getServerGuiElement(ID, player, world, x, y, z) as ContainerController)
        } else if (ID == 1) {
            return GuiInventoryDetector(getServerGuiElement(ID, player, world, x, y, z) as ContainerInventoryDetector)
        }
        return null
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? {
        if (ID == 0) {
            return ContainerController(world, BlockPos(x, y, z), player)
        } else if (ID == 1) {
            return ContainerInventoryDetector(world, BlockPos(x, y, z), player)
        }
        return null
    }
}