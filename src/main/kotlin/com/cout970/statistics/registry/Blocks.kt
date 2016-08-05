package com.cout970.statistics.registry

import com.cout970.statistics.Statistics
import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import com.cout970.statistics.block.BlockInventoryConnector
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import wiresegal.zenmodelloader.client.core.ModelHandler
import java.util.*

/**
 * Created by cout970 on 04/08/2016.
 */
val blocks = listOf(
        BlockCable,
        BlockController,
        BlockInventoryConnector
)

fun registerBlocks() {
    ModelHandler.resourceLocations.put(Statistics.MOD_ID, HashMap<String, ModelResourceLocation>())
    for (i in blocks) {
        ModelHandler.addToCache(i)
        ModelHandler.resourceLocations[Statistics.MOD_ID]!!.put(i.itemForm!!.registryName.toString(), ModelResourceLocation(i.itemForm!!.registryName, "inventory"))
    }
}