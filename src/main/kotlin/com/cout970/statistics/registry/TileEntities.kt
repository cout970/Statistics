package com.cout970.statistics.registry

import com.cout970.statistics.Statistics
import com.cout970.statistics.tileentity.TileController
import com.cout970.statistics.tileentity.TileInventoryConnector
import com.cout970.statistics.tileentity.TileInventoryDetector
import net.minecraftforge.fml.common.registry.GameRegistry

/**
 * Created by cout970 on 04/08/2016.
 */
val tileEntities = mapOf(
         TileController::class.java to "controller",
         TileInventoryConnector::class.java to "inventory_connector",
         TileInventoryDetector::class.java to "inventory_detector"
)

fun registerTileEntities(){
    for((t, name) in tileEntities) {
        GameRegistry.registerTileEntity(t, Statistics.MOD_ID+"."+name)
    }
}