package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.network.IndexedData
import com.cout970.statistics.tileentity.TileController
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Created by cout970 on 04/08/2016.
 */

val CONTROLLER_BACKGROUND = ResourceLocation(Statistics.MOD_ID, "textures/gui/controller.png")

class GuiController(val controller: ContainerController) : GuiBase(controller) {

    val selected = IntArray(15, { it })
    var pointer = 0
    var offset = 0
    var count = 0

    init {
        xSize = 280
        ySize = 166
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {

        Gui.drawRect(guiLeft - 1, guiTop - 1, guiLeft + 282, guiTop + 122, 0xFF000000.toInt())
        Gui.drawRect(guiLeft, guiTop, guiLeft + 280, guiTop + 121, 0xFF4C4C4C.toInt())

        for (i in 0..120 step 5) {
            drawHorizontalLine(guiLeft, guiLeft + 280, guiTop + i, 0xFF3C3C3C.toInt())
        }

        for (i in 0..280 step 5) {
            drawVerticalLine(guiLeft + i, guiTop, guiTop + 120, 0xFF3C3C3C.toInt())
        }

        Gui.drawRect(guiLeft + 4, guiTop + 123, guiLeft + 276, guiTop + 125 + 54, 0x4F4C4C4C.toInt())
        var topValue: Int = 64

        synchronized(controller.tileEntity as TileController) {

            val snapshots = controller.tileEntity!!.snapshots
            val datas = snapshots.map { it.data.entries }.flatten().groupBy { it.key }
            if (datas.values.isEmpty()) return@synchronized

            var max = 64
            var tempIndex = 0
            for ((key) in datas) {
                if (tempIndex in selected) {
                    max = Math.max(max, datas[key]!!.maxBy { it.value.amount }!!.value.amount)
                }
                tempIndex++
            }

            topValue = (Math.ceil(max / 64.0) * 64).toInt()
            val scale = 120f / topValue.toFloat()

            var pos = 0
            var index = 0

            for ((key) in datas) {

                if (index in selected) {

                    //draw line
                    val points = mutableListOf<Pair<Float, Float>>()
                    for (i in 0 until 50) {
                        val value: Float
                        if (snapshots.size > 49 - i) {
                            val stat = snapshots[49 - i].data.getOrElse(key, { null })
                            if (stat != null) {
                                value = Math.min(stat.amount.toFloat() * scale, 120.0f)
                            } else {
                                value = 0.0f
                            }
                        } else {
                            value = 0.0f
                        }
                        points.add(Pair(i.toFloat() * 279f / 49f + guiLeft + 1, guiTop + 121 - value))
                    }

                    val dye = EnumDyeColor.byMetadata(selected.indexOf(index) % 16)
                    val color = Color(dye.mapColor.colorValue)
                    GlStateManager.color(color.red / 256f, color.green / 256f, color.blue / 256f)
                    GL11.glLineWidth(1.5f)
                    drawLine(points)
                }
                if (index / 15 < 3 + offset && index / 15 - offset >= 0) {
                    val dye = EnumDyeColor.byMetadata(selected.indexOf(index) % 16)
                    val x = index % 15 * 18 + 6
                    val y = (index / 15 - offset) * 18 + 125
                    val stack = key.toStack()

                    if (index in selected) {
                        if (stack.item != Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) {
                            GlStateManager.pushMatrix()
                            GlStateManager.translate(0.0, 0.0, -20.0)
                            drawStack(ItemStack(Blocks.STAINED_GLASS_PANE, 1, dye.metadata), guiLeft + x, guiTop + y)
                            GlStateManager.popMatrix()
                        } else {
//                            GlStateManager.pushMatrix()
//                            GlStateManager.translate(0.0, 0.0, 0.0)
                            Gui.drawRect(guiLeft + x - 1, guiTop + y - 1, guiLeft + x + 17, guiTop + y + 17, dye.mapColor.colorValue or 0xFF000000.toInt())
//                            GlStateManager.popMatrix()
                        }
                    }
                    drawStack(stack, guiLeft + x, guiTop + y)
                }
                index++
            }
            count = index
        }

        val top = guiTop.toDouble() - 1
        val bottom = guiTop + 120.0 - 1

        val s = 0.5f
        val max = 16
        for (i in 0..max) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(guiLeft.toDouble(), bottom + (top - bottom) * (i / max.toFloat()), 0.0)
            GlStateManager.scale(s, s, s)
            drawRightString("%d -".format(topValue * i / max), 0, 0)
            GlStateManager.popMatrix()
        }

        if (count > 45) {

        }
    }

    fun drawRightString(text: String, posX: Int, posY: Int) {
        fontRendererObj.drawString(text, posX - fontRendererObj.getStringWidth(text), posY, Color.WHITE.rgb)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val dwheel = Mouse.getDWheel()
        if (dwheel < 0) {
            if (count / 15 > 3 + offset) {
                offset++
            }
        } else if (dwheel > 0) {
            if (offset > 0) {
                offset--
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_DOWN) {
            if (count / 15 > 3 + offset) {
                offset++
            }
        } else if (keyCode == Keyboard.KEY_UP) {
            if (offset > 0) {
                offset--
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        println("x: $mouseX, y: $mouseY")
        start@for (x in 0 until 15) {
            for (y in 0 until 3) {
                if (inside(guiLeft + x * 18 + 6, guiTop + y * 18 + 125, 18, 18, mouseX, mouseY)) {
                    val index = x + (y + offset) * 15
                    println(mouseButton)
                    if (mouseButton == 0) {
                        if (index !in selected) {
                            val indexOf = selected.indexOf(-1)
                            if (indexOf == -1) {
                                selected[pointer] = index
                                pointer = (pointer + 1) % selected.size
                            } else {
                                selected[indexOf] = index
                            }
                        }
                    } else {
                        val indexOf = selected.indexOf(index)
                        if (indexOf != -1) {
                            selected[indexOf] = -1
                        }
                    }
                    break@start
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun inside(x: Int, y: Int, sizeX: Int, sizeY: Int, mx: Int, my: Int): Boolean {
        return mx >= x && mx <= x + sizeX && my >= y && my <= y + sizeY
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