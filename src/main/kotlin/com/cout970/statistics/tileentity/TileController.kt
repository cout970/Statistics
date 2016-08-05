package com.cout970.statistics.tileentity

import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import java.util.*

/**
 * Created by cout970 on 04/08/2016.
 */
class TileController : TileBase(), ITickable {

    val shortSnapshots = LinkedList<DataSnapshot>()
    val mediumSnapshots = LinkedList<DataSnapshot>()
    val largeSnapshots = LinkedList<DataSnapshot>()

    override fun update() {
        if (worldObj.isRemote) return
        if ((worldObj.totalWorldTime + pos.hashCode()) % 20 == 0L) {
            shortSnapshots.addFirst(DataSnapshot().apply { collectData(getStacks()) })
            if (shortSnapshots.size > 50) {
                shortSnapshots.removeLast()
            }
        }
        if ((worldObj.totalWorldTime + pos.hashCode()) % 200 == 0L) {
            mediumSnapshots.addFirst(DataSnapshot().apply { collectData(getStacks()) })
            if (mediumSnapshots.size > 50) {
                mediumSnapshots.removeLast()
            }
        }
        if ((worldObj.totalWorldTime + pos.hashCode()) % (20 * 60) == 0L) {
            largeSnapshots.addFirst(DataSnapshot().apply { collectData(getStacks()) })
            if (largeSnapshots.size > 50) {
                largeSnapshots.removeLast()
            }
        }
    }

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

    fun getInventories(): List<IItemHandler> {
        val list = mutableListOf<IItemHandler>()
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

        return list
    }

    class ItemIdentifier(
           val stack: ItemStack
    ) {



        override fun toString(): String{
            return "ItemIdentifier(stack=$stack)"
        }

        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other !is ItemIdentifier) return false

            if (stack.item != other.stack.item) return false
            if (stack.metadata != other.stack.metadata) return false
            if (stack.tagCompound != other.stack.tagCompound) return false

            return true
        }

        override fun hashCode(): Int{
            return ((stack.tagCompound?.hashCode() ?: 0)  * 31 + stack.item.hashCode()) * 31 + stack.metadata
        }
    }

    class ItemStatistics(
            val itemId: ItemIdentifier
    ) {
        var amount = 0
        override fun toString(): String {
            return "ItemStatistics(itemId=$itemId, amount=$amount)"
        }
    }

    class DataSnapshot() {
        val data = mutableMapOf<ItemIdentifier, ItemStatistics>()

        fun collectData(stacks: List<ItemStack>) {
            data.clear()
            for (i in stacks) {
                val id = i.identifier
                if (id in data) {
                    val sta = data[id]
                    sta?.run { amount += i.stackSize }
                } else {
                    val sta = ItemStatistics(id)
                    sta.amount = i.stackSize
                    data.put(id, sta)
                }
            }
        }
    }
}

val ItemStack.identifier: TileController.ItemIdentifier get() = TileController.ItemIdentifier(this)