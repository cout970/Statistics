package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.network.IndexedData
import com.cout970.statistics.tileentity.TileController
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

/**
 * Created by cout970 on 04/08/2016.
 */

val CONTROLLER_BACKGROUND = ResourceLocation(Statistics.MOD_ID, "textures/gui/controller.png")

class GuiController(val controller: ContainerController) : GuiBase(controller) {

    init {
        xSize = 280
        ySize = 166
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
//        bindTexture(CONTROLLER_BACKGROUND)
//        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0f, 0f, 280, 166, 512f, 512f)

        val xDisp = 32

        Gui.drawRect(guiLeft + xDisp - 1, guiTop + 19, guiLeft + xDisp + 1 + 49 * 4, guiTop + 22 + 100, 0xFF000000.toInt())
        Gui.drawRect(guiLeft + xDisp, guiTop + 20, guiLeft + xDisp + 49 * 4, guiTop + 21 + 100, 0xFF4C4C4C.toInt())

        for (i in 0..100 step 5) {
            drawHorizontalLine(guiLeft + xDisp, guiLeft + xDisp + 49 * 4 - 1, guiTop + 20 + i, 0xFF3C3C3C.toInt())
        }

        for (i in 0..49 * 4 step 5) {
            drawVerticalLine(guiLeft + xDisp + i, guiTop + 20, guiTop + 20 + 100, 0xFF3C3C3C.toInt())
        }

        synchronized(controller.tileEntity as TileController) {

            val snapshots = controller.tileEntity!!.snapshots
            val datas = snapshots.map { it.data.entries }.flatten().groupBy { it.key }
            if (datas.values.isEmpty()) return@synchronized
            var scale = 0.5f
            datas.values.forEach {
                scale = Math.max(scale, it.maxBy { it.value.amount }!!.value.amount.toFloat())
            }

            scale = (Math.ceil(scale / 64.0) * 64).toFloat()
            scale = 100f / scale

            for ((key) in datas) {

                drawStack(key.toStack(), 0,0)
                val points = mutableListOf<Pair<Float, Float>>()
                for (i in 0..49) {
                    val value: Float
                    if (snapshots.size > 49 - i) {
                        val stat = snapshots[49 - i].data.getOrElse(key, { null })
                        if (stat != null) {
                            value = stat.amount.toFloat() * scale
                        } else {
                            value = 0.0f
                        }
                    } else {
                        value = 0.0f
                    }
                    points.add(Pair(i.toFloat() * 4 + guiLeft + xDisp, guiTop + 20f + 100 - value))
                }
                GL11.glLineWidth(2f)
                val hash = key.hashCode() * 31 + key.item.hashCode()

                GlStateManager.color((hash and 265) / 256f, ((hash shr 8) and 265) / 256f, ((hash shr 16) and 265) / 256f)
                drawLine(points)
            }
        }
    }
}

class ContainerController(world: World, pos: BlockPos, player: EntityPlayer) : ContainerBase(world, pos, player) {

    companion object {
        val SIZE_ID = 0
    }

    val tileEntity by lazy {
        val tile = world.getTileEntity(pos)
        if (tile is TileController) tile else null
    }

    override fun readPacket(pkt: GuiPacket) {
        if (tileEntity != null) {
            synchronized(tileEntity as TileController) {
                val data = pkt.data!!
                val size = data[SIZE_ID] as Int
                val snapshots = mutableListOf<TileController.DataSnapshot>()

                for (index in 1..size) {
                    val stats = mutableMapOf<TileController.ItemIdentifier, TileController.ItemStatistics>()
                    val amount = data[index] as Int
                    val pos = index shl 16
                    var subIndex = 0

                    for (j in 0 until amount) {
                        val itemId = data[subIndex++ or pos] as Int
                        val metadata = data[subIndex++ or pos] as Int
                        val stacksize = data[subIndex++ or pos] as Int

                        val item = Item.getItemById(itemId)
                        if (item != null) {
                            val id = TileController.ItemIdentifier(item, metadata)
                            stats.put(id, TileController.ItemStatistics(id).apply { this.amount = stacksize })
                        }
                    }
                    snapshots.add(TileController.DataSnapshot().apply { this.data.putAll(stats) })
                }
                tileEntity!!.snapshots.clear()
                tileEntity!!.snapshots.addAll(snapshots)
            }
        }
    }

    override fun writePacket(): GuiPacket {
        val data = IndexedData()
        if (tileEntity != null) {

            data.setInt(SIZE_ID, tileEntity!!.snapshots.size)
            var index = 1

            for (i in tileEntity!!.snapshots) {

                data.setInt(index, i.data.size)
                val pos = index shl 16
                var subIndex = 0

                for ((key, value) in i.data) {

                    data.setInt(subIndex++ or pos, Item.getIdFromItem(key.item))
                    data.setInt(subIndex++ or pos, key.metadata)
                    data.setInt(subIndex++ or pos, value.amount)
                }
                index++
            }
        }
        return GuiPacket(data)
    }

}