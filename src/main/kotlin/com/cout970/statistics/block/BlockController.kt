package com.cout970.statistics.block

import com.cout970.statistics.Statistics
import com.cout970.statistics.tileentity.TileController
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by cout970 on 04/08/2016.
 */

val propertyDirection: PropertyDirection = PropertyDirection.create("facing", listOf(*EnumFacing.HORIZONTALS))

object BlockController : BlockBase(Material.IRON, "controller"), ITileEntityProvider {

    override fun createNewTileEntity(worldIn: World?, meta: Int): TileEntity = TileController()

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack?) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
        worldIn.setBlockState(pos, state.withProperty(propertyDirection, placer.horizontalFacing.opposite))
    }

    override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, propertyDirection)

    override fun getMetaFromState(state: IBlockState): Int {
        return state.getValue(propertyDirection).horizontalIndex
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        return defaultState.withProperty(propertyDirection, EnumFacing.getHorizontal(meta))
    }

    override fun onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState?, playerIn: EntityPlayer, hand: EnumHand?, heldItem: ItemStack?, side: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        if (!playerIn.isSneaking) {
            if (!worldIn.isRemote) {
                playerIn.openGui(Statistics, 0, worldIn, pos.x, pos.y, pos.z)
            }
            return true
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ)
    }
}