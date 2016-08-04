package com.cout970.statistics

import com.cout970.statistics.util.info
import net.minecraft.client.Minecraft
import net.minecraft.util.Timer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import org.apache.logging.log4j.Logger

/**
 * Created by cout970 on 04/08/2016.
 */
@Mod(
        modid = Statistics.MOD_ID,
        modLanguage = "kotlin",
        name = "Statistics",
        version = "@VERSION@",
        modLanguageAdapter = "com.cout970.statistics.KotlinAdapter"
)
object Statistics {

    const val MOD_ID = "statistics"

    @SidedProxy(
            serverSide = "com.cout970.statistics.CommonLoader",
            clientSide = "com.cout970.statistics.ClientLoader")
    var proxy: CommonLoader? = null

    lateinit var LOGGER: Logger
    val NETWORK = SimpleNetworkWrapper(MOD_ID)

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent){
        LOGGER = e.modLog
        info("Starting preInit")
        proxy?.preInit()
        info("preInit done")
    }

    //useful function to change the amount of tick per second used in minecraft
    fun setTicksPerSecond(tps: Int) {
        val timerField = Minecraft::class.java.getDeclaredField("timer")
        timerField.isAccessible = true
        val timer = timerField.get(Minecraft.getMinecraft()) as Timer
        val tickField = Timer::class.java.getDeclaredField("ticksPerSecond")
        tickField.isAccessible = true
        tickField.set(timer, tps.toFloat())
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent){
        info("Starting init")
        proxy?.init()
        info("init done")
    }
}