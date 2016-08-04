package com.cout970.statistics

import com.cout970.statistics.util.info
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger

/**
 * Created by cout970 on 04/08/2016.
 */
@Mod(
        modid = "statistics",
        modLanguage = "kotlin",
        name = "Statistics",
        version = "@VERSION@",
        modLanguageAdapter = "com.cout970.statistics.KotlinAdapter"
)
object Statistics {


    @SidedProxy(
            serverSide = "com.cout970.statistics.CommonLoader",
            clientSide = "com.cout970.statistics.ClientLoader")
    var proxy: CommonLoader? = null

    lateinit var LOGGER: Logger


    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent){
        LOGGER = e.modLog
        info("Starting preInit")
        info("preInit done")
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent){
        info("Starting init")
        info("init done")
    }
}