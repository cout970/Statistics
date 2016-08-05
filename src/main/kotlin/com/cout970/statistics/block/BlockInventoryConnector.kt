package com.cout970.statistics.block

import com.cout970.statistics.tileentity.TileInventoryConnector
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * Created by cout970 on 05/08/2016.
 */
object BlockInventoryConnector : BlockBase(Material.IRON, "inventory_connector"), ITileEntityProvider {

    override fun createNewTileEntity(worldIn: World?, meta: Int): TileEntity = TileInventoryConnector()
}