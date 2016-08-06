package com.cout970.statistics.tileentity

import com.cout970.statistics.data.ItemIdentifier
import com.cout970.statistics.data.identifier
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable

/**
 * Created by cout970 on 05/08/2016.
 */
class TileInventoryDetector : TileBase(), ITickable {

    var items = mutableMapOf<ItemIdentifier, Int>()
    private var redstoneLevel = 0
    var limit = 0
    var operation = Operation.DIFFERENT
    var itemCache: ItemIdentifier? = null

    //client only
    var active = false
    var amount = -1

    fun getItem(index: Int): ItemIdentifier? {
        val keys = items.keys.sortedBy { it.stack.item.registryName.toString() }
        if (index in keys.indices) {
            return keys[index]
        }
        return null
    }

    override fun receiveMsg(id: Int, value: Int) {
        if (id == 0) {
            limit = value
        } else if (id == 1) {
            operation = Operation.values()[value % Operation.values().size]
        } else if (id == 2) {
            val key = getItem(value)
            if (key != null) {
                amount = items[key]!!
                itemCache = key
            } else {
                amount = 0
                itemCache = null
            }
        }
    }

    override fun update() {
        if (worldObj.isRemote) return
        if ((worldObj.totalWorldTime + pos.hashCode()) % 20 == 0L) {

            items.clear()
            for (s in getStacks()) {
                val id = s.identifier
                if (id in items) {
                    items[id] = items[id]!! + s.stackSize
                } else {
                    items.put(id, s.stackSize)
                }
            }

            val key = itemCache
            if (key != null && key in items) {
                amount = items[key]!!
            } else {
                amount = 0
            }
            if (operation.func(amount, limit)) {
                redstoneLevel = 15
            } else {
                redstoneLevel = 0
            }
            markDirty()
            worldObj.notifyNeighborsOfStateChange(pos, getBlockType())
        }
    }

    fun getRedstoneLevel(): Int {
        return redstoneLevel
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("Cache", itemCache?.serializeNBT() ?: NBTTagCompound())
        compound.setInteger("Operation", operation.ordinal)
        compound.setInteger("Limit", limit)
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        if (compound.hasKey("Cache")) {
            if (!compound.getCompoundTag("Cache").hasNoTags()) {
                itemCache = ItemIdentifier.deserializeNBT(compound.getCompoundTag("Cache"))
            } else {
                itemCache = null
            }
        }
        if (compound.hasKey("Limit")) {
            limit = compound.getInteger("Limit")
        }
        if (compound.hasKey("Operation")) {
            operation = Operation.values()[compound.getInteger("Operation")]
        }
        super.readFromNBT(compound)
    }

    enum class Operation(val func: (value: Int, limit: Int) -> Boolean, val text: String) {
        EQUALS({ value, limit -> value == limit }, "="),
        GREATER({ value, limit -> value > limit }, ">"),
        GREATER_EQUALS({ value, limit -> value >= limit }, ">="),
        LESS({ value, limit -> value < limit }, "<"),
        LESS_EQUALS({ value, limit -> value <= limit }, "<="),
        DIFFERENT({ value, limit -> value != limit }, "!=");

        override fun toString(): String {
            return text
        }
    }
}