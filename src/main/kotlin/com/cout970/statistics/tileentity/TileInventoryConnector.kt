package com.cout970.statistics.tileentity

import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import java.util.*

/**
 * Created by cout970 on 05/08/2016.
 */
class TileInventoryConnector : TileBase(), ITickable {

    val inventory = Inventory()

    override fun update() {
        if ((worldObj.totalWorldTime + pos.hashCode()) % 20 == 0L) {
            inventory.subInventories.clear()
            inventory.subInventories.addAll(getInventories().filter { it !is Inventory })
        }
    }

    override fun getInventories(): List<IItemHandler> {
        val list = mutableSetOf<IItemHandler>()
        val forbiden = setOf(pos.offset(EnumFacing.DOWN), pos.offset(EnumFacing.UP), pos.offset(EnumFacing.NORTH), pos.offset(EnumFacing.SOUTH),
                pos.offset(EnumFacing.EAST), pos.offset(EnumFacing.WEST))

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
            } else if (pos !in forbiden) {
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

    override fun <T : Any?> getCapability(capability: Capability<T>?, facing: EnumFacing?): T {
        @Suppress("UNCHECKED_CAST")
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return inventory as T
        return super.getCapability(capability, facing)
    }

    override fun hasCapability(capability: Capability<*>?, facing: EnumFacing?): Boolean {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true
        return super.hasCapability(capability, facing)
    }

    inner class Inventory : IItemHandler {

        val subInventories = mutableListOf<IItemHandler>()

        fun getSlot(index: Int): Pair<Int, IItemHandler>? {
            var acum = 0
            for (i in subInventories) {
                val slots = i.slots
                if (index >= acum + slots) {
                    acum += i.slots
                } else {
                    return (index - acum) to i
                }
            }
            return null
        }

        override fun getStackInSlot(slot: Int): ItemStack? {
            val pair = getSlot(slot) ?: return null
            return pair.second.getStackInSlot(pair.first)
        }

        override fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
            val pair = getSlot(slot) ?: return stack
            return pair.second.insertItem(pair.first, stack, simulate)
        }

        override fun getSlots(): Int = subInventories.sumBy { it.slots }

        override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? {
            val pair = getSlot(slot) ?: return null
            return pair.second.extractItem(pair.first, amount, simulate)
        }

    }
}