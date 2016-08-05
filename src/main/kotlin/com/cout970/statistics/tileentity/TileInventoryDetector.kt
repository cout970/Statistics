package com.cout970.statistics.tileentity

import net.minecraft.util.ITickable

/**
 * Created by cout970 on 05/08/2016.
 */
class TileInventoryDetector : TileBase(), ITickable {


    override fun update() {

    }

    fun getRedstoneLevel(): Int {
        return 15
    }

}