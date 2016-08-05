package com.cout970.statistics.tileentity

import com.cout970.statistics.data.ItemIdentifier
import com.cout970.statistics.data.ItemStatistics
import com.cout970.statistics.data.identifier
import com.cout970.statistics.gui.forEach
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ITickable
import net.minecraftforge.common.util.Constants

/**
 * Created by cout970 on 04/08/2016.
 */
class TileController : TileBase(), ITickable {

    var type = 0

    val selected = IntArray(15, { it })
    val dataArray = mutableMapOf<Int, MutableList<Int>>()
    val items = mutableSetOf<ItemIdentifier>()

    var pointer = 0

    //server
    val shortUpdates = mutableMapOf<ItemIdentifier, ItemStatistics>()
    val mediumUpdates = mutableMapOf<ItemIdentifier, ItemStatistics>()
    val largeUpdates = mutableMapOf<ItemIdentifier, ItemStatistics>()

    override fun receiveMsg(id: Int, value: Int) {
        if (id == -1) {
            type = value
        } else {
            selected[id] = value
        }
    }

    override fun update() {
        if (worldObj.isRemote) return
        if ((worldObj.totalWorldTime + pos.hashCode()) % 20 == 0L) {
            val stacks = getStacks()
            collectData(shortUpdates, stacks)
            if ((worldObj.totalWorldTime + pos.hashCode()) % 200 == 0L) {
                collectData(mediumUpdates, stacks)
                if ((worldObj.totalWorldTime + pos.hashCode()) % 1200 == 0L) {
                    collectData(largeUpdates, stacks)
                }
            }
        }
    }

    fun collectData(map: MutableMap<ItemIdentifier, ItemStatistics>, stacks: List<ItemStack>) {

        val processed = mutableMapOf<ItemIdentifier, Int>()
        for (s in stacks) {
            val id = s.identifier
            if (id in processed) {
                processed[id] = processed[id]!! + s.stackSize
            } else {
                processed.put(id, s.stackSize)
            }
        }

        val allIds = processed.keys + map.keys
        for (id in allIds) {
            val inM = id in map
            val inP = id in processed
            if (inM && !inP) {
                map[id]!!.stackSize.addFirst(0)
                if (map[id]!!.stackSize.size > 50) {
                    map[id]!!.stackSize.removeLast()
                }
            } else if (!inM && inP) {
                val stat = ItemStatistics()
                stat.stackSize.addFirst(processed[id])
                map.put(id, stat)
            } else if (inM && inP) {
                map[id]!!.stackSize.addFirst(processed[id])
                if (map[id]!!.stackSize.size > 50) {
                    map[id]!!.stackSize.removeLast()
                }
            } else {
                throw IllegalStateException("Math is broken, this should never happen")
            }
        }
    }

    override fun readFromNBT(compound: NBTTagCompound?) {
        super.readFromNBT(compound)
        val data = compound!!
        type = data.getInteger("Type")

        val short = data.getTagList("short", Constants.NBT.TAG_COMPOUND)
        val shortTemp = mutableMapOf<ItemIdentifier, ItemStatistics>()
        short.forEach {
            val id = ItemIdentifier.deserializeNBT(it.getCompoundTag("ID"))
            val stacksize = it.getIntArray("stacksize")
            shortTemp.put(id, ItemStatistics(stacksize))
        }
        shortUpdates.clear()
        shortUpdates.putAll(shortTemp)

        val medium = data.getTagList("medium", Constants.NBT.TAG_COMPOUND)
        val mediumTemp = mutableMapOf<ItemIdentifier, ItemStatistics>()
        medium.forEach {
            val id = ItemIdentifier.deserializeNBT(it.getCompoundTag("ID"))
            val stacksize = it.getIntArray("stacksize")
            mediumTemp.put(id, ItemStatistics(stacksize))
        }
        mediumUpdates.clear()
        mediumUpdates.putAll(mediumTemp)

        val large = data.getTagList("large", Constants.NBT.TAG_COMPOUND)
        val largeTemp = mutableMapOf<ItemIdentifier, ItemStatistics>()
        large.forEach {
            val id = ItemIdentifier.deserializeNBT(it.getCompoundTag("ID"))
            val stacksize = it.getIntArray("stacksize")
            largeTemp.put(id, ItemStatistics(stacksize))
        }
        largeUpdates.clear()
        largeUpdates.putAll(largeTemp)
    }

    override fun writeToNBT(compound: NBTTagCompound?): NBTTagCompound {
        val data = compound!!
        data.setInteger("Type", type)

        val short = NBTTagList()
        for ((key, value) in shortUpdates) {
            val nbt = NBTTagCompound()
            nbt.setTag("ID", key.serializeNBT())
            nbt.setIntArray("stacksize", value.stackSize.toIntArray())
            short.appendTag(nbt)
        }
        data.setTag("short", short)

        val medium = NBTTagList()
        for ((key, value) in mediumUpdates) {
            val nbt = NBTTagCompound()
            nbt.setTag("ID", key.serializeNBT())
            nbt.setIntArray("stacksize", value.stackSize.toIntArray())
            medium.appendTag(nbt)
        }
        data.setTag("medium", medium)

        val large = NBTTagList()
        for ((key, value) in largeUpdates) {
            val nbt = NBTTagCompound()
            nbt.setTag("ID", key.serializeNBT())
            nbt.setIntArray("stacksize", value.stackSize.toIntArray())
            large.appendTag(nbt)
        }
        data.setTag("large", large)
        return super.writeToNBT(compound)
    }
}