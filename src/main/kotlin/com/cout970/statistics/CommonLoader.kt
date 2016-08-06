package com.cout970.statistics

import com.cout970.statistics.block.BlockCable
import com.cout970.statistics.block.BlockController
import com.cout970.statistics.block.BlockInventoryConnector
import com.cout970.statistics.block.BlockInventoryDetector
import com.cout970.statistics.gui.GuiHandler
import com.cout970.statistics.network.ClientEventPacket
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.registry.registerBlocks
import com.cout970.statistics.registry.registerTileEntities
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.oredict.ShapedOreRecipe

/**
 * Created by cout970 on 04/08/2016.
 */
open class CommonLoader {

    open fun preInit() {
        registerBlocks()
        registerTileEntities()
    }

    open fun init() {

        GameRegistry.addRecipe(ShapedOreRecipe(ItemStack(BlockCable, 8), "idi", "dgd", "idi", 'i', "ingotIron", 'd', "dyeBlack", 'g', Blocks.CHEST))
        GameRegistry.addRecipe(ShapedOreRecipe(ItemStack(BlockController), "iii", "rdg", "iii", 'i', "ingotIron", 'r', "dustRedstone", 'd', Items.DIAMOND, 'g', Blocks.GLASS))
        GameRegistry.addRecipe(ShapedOreRecipe(ItemStack(BlockInventoryConnector), "cic", "isi", "cic", 'c', BlockCable, 's', Items.NETHER_STAR, 'i', Blocks.CHEST))
        GameRegistry.addRecipe(ShapedOreRecipe(ItemStack(BlockInventoryDetector), "crc", "rbr", "crc", 'r', "dustRedstone", 'r', "dustRedstone", 'c', BlockCable, 'b', Blocks.REDSTONE_BLOCK))

        NetworkRegistry.INSTANCE.registerGuiHandler(Statistics, GuiHandler)
        Statistics.NETWORK.registerMessage(GuiPacket.Companion, GuiPacket::class.java, 0, Side.CLIENT)
        Statistics.NETWORK.registerMessage(ClientEventPacket.Companion, ClientEventPacket::class.java, 1, Side.SERVER)
    }
}