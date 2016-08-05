package com.cout970.statistics.data

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class ItemIdentifier(
        val stack: ItemStack
) {

    companion object{
        fun deserializeNBT(nbt: NBTTagCompound): ItemIdentifier {
            return ItemStack.loadItemStackFromNBT(nbt).identifier
        }
    }

    fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        stack.writeToNBT(nbt)
        return nbt
    }

    override fun toString(): String {
        return "ItemIdentifier(stack=$stack)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemIdentifier) return false

        if (stack.item != other.stack.item) return false
        if (stack.metadata != other.stack.metadata) return false
        if (stack.tagCompound != other.stack.tagCompound) return false

        return true
    }

    override fun hashCode(): Int {
        return ((stack.tagCompound?.hashCode() ?: 0) * 31 + stack.item.hashCode()) * 31 + stack.metadata
    }
}

val ItemStack.identifier: ItemIdentifier get() = ItemIdentifier(this)