package com.cout970.statistics.tileentity

import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import java.util.*

/**
 * Created by cout970 on 04/08/2016.
 */
abstract class TileBase : TileEntity() {

    open fun receiveMsg(id: Int, value: Int) {}

    fun getStacks(): List<ItemStack> {
        val invs = getInventories()
        val items = mutableListOf<ItemStack>()
        for (i in invs) {
            for (j in 0 until i.slots) {
                val item = i.getStackInSlot(j)
                if (item != null) {
                    items.add(item)
                }
            }
        }
        return items
    }

    open fun getInventories(): List<IItemHandler> {
        val list = mutableSetOf<IItemHandler>()
        val queue = LinkedList<Pair<BlockPos, EnumFacing>>()
        val map = HashSet<Pair<BlockPos, EnumFacing>>()
        for (dir in EnumFacing.VALUES) {
            queue.add(pos to dir)
            map.add(pos to dir)
        }

        while (!queue.isEmpty()) {
            val pair = queue.pop()
            val pos = pair.first.offset(pair.second)
            val block = worldObj.getBlockState(pos).block
            if (block == BlockCable || block == BlockController) {
                for (dir in EnumFacing.VALUES) {
                    if ((pos to dir) !in map) {
                        queue.add(pos to dir)
                        map.add(pos to dir)
                    }
                }
            } else {
                val tile = worldObj.getTileEntity(pos)
                if (tile != null) {
                    val inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, pair.second.opposite)
                    if (inventory != null) {
                        list.add(inventory)
                    }
                }
            }
        }
        return LinkedList(list)
    }
}