package com.cout970.statistics

import com.cout970.statistics.gui.GuiHandler
import com.cout970.statistics.network.ClientEventPacket
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.registry.registerBlocks
import com.cout970.statistics.registry.registerTileEntities
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side

/**
 * Created by cout970 on 04/08/2016.
 */
open class CommonLoader {

    open fun preInit(){
        registerBlocks()
        registerTileEntities()
    }

    open fun init(){
        NetworkRegistry.INSTANCE.registerGuiHandler(Statistics, GuiHandler)
        Statistics.NETWORK.registerMessage(GuiPacket.Companion, GuiPacket::class.java, 0, Side.CLIENT)
        Statistics.NETWORK.registerMessage(ClientEventPacket.Companion, ClientEventPacket::class.java, 1, Side.SERVER)
    }
}