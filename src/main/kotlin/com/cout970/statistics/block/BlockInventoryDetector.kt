package com.cout970.statistics.block

import com.cout970.statistics.tileentity.TileInventoryDetector
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

/**
 * Created by cout970 on 05/08/2016.
 */
object BlockInventoryDetector : BlockBase(Material.IRON, "inventory_detector"), ITileEntityProvider {

    override fun createNewTileEntity(worldIn: World?, meta: Int): TileEntity = TileInventoryDetector()

    override fun getStrongPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Int {
        return blockState.getWeakPower(blockAccess, pos, side)
    }

    override fun getWeakPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Int {
        val tile = blockAccess.getTileEntity(pos)
        if(tile is TileInventoryDetector) {
            return tile.getRedstoneLevel()
        }
        return 0
    }

    override fun canProvidePower(state: IBlockState): Boolean {
        return true
    }
}