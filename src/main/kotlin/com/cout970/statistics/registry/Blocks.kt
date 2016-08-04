package com.cout970.statistics.registry

import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import wiresegal.zenmodelloader.client.core.ModelHandler

/**
 * Created by cout970 on 04/08/2016.
 */
val blocks = listOf(
        BlockCable,
        BlockController
)


fun registerBlocks(){
    for(i in blocks) {
        ModelHandler.addToCache(i)
    }
}