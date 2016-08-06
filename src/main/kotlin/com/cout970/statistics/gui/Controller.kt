package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.data.ItemIdentifier
import com.cout970.statistics.data.ItemStatistics
import com.cout970.statistics.network.ClientEventPacket
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.tileentity.TileController
import com.cout970.statistics.util.orElse
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Created by cout970 on 04/08/2016.
 */

class GuiController(val controller: ContainerController) : GuiBase(controller) {

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
        val tile = controller.tileEntity!!

        synchronized(controller.tileEntity as TileController) {

            if (tile.items.isEmpty()) return@synchronized
            val itemMap = mutableMapOf<Int, ItemIdentifier>()
            val allItems = tile.items.sortedBy { it.stack.item.registryName.toString() }
            allItems.forEachIndexed { i, id -> if (i in tile.selected) itemMap.put(i, id) }

            for (index in itemMap.keys) {
                if (index !in tile.dataArray) {
                    tile.dataArray.put(index, mutableListOf<Int>())
                } else if (tile.dataArray[index] == null) {
                    tile.dataArray.put(index, mutableListOf<Int>())
                }
            }

            var max = 64
            for (index in itemMap.keys) {
                max = Math.max(max, tile.dataArray[index]!!.maxBy { it }.orElse(0))
            }

            topValue = (Math.ceil(max / 64.0) * 64).toInt()
            val scale = 120f / topValue.toFloat()

            for (index in itemMap.keys) {

                //draw line
                val points = mutableListOf<Pair<Float, Float>>()
                for (i in 0 until 50) {
                    val value: Float
                    if (tile.dataArray[index]!!.size > 49 - i) {
                        val stat = tile.dataArray[index]!![49 - i]
                        value = Math.min(stat * scale, 120.0f)
                    } else {
                        value = 0.0f
                    }
                    points.add(Pair(i.toFloat() * 279f / 49f + guiLeft + 1, guiTop + 121 - value))
                }

                val dye = EnumDyeColor.byMetadata(tile.selected.indexOf(index) % 16)
                val color = Color(dye.mapColor.colorValue)
                GlStateManager.color(color.red / 256f, color.green / 256f, color.blue / 256f)
                GL11.glLineWidth(1.5f)
                drawLine(points)
            }

            var index = 0
            for (i in allItems) {
                if (index / 15 < 3 + offset && index / 15 - offset >= 0) {
                    val dye = EnumDyeColor.byMetadata(tile.selected.indexOf(index) % 16)
                    val x = index % 15 * 18 + 6
                    val y = (index / 15 - offset) * 18 + 125
                    val stack = i.stack.copy().apply { stackSize = 1 }

                    if (index in tile.selected) {
                        if (stack.item != Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) {
                            GlStateManager.pushMatrix()
                            GlStateManager.translate(0.0, 0.0, -20.0)
                            drawStack(ItemStack(Blocks.STAINED_GLASS_PANE, 1, dye.metadata), guiLeft + x, guiTop + y)
                            GlStateManager.popMatrix()
                        } else {
                            Gui.drawRect(guiLeft + x - 1, guiTop + y - 1, guiLeft + x + 17, guiTop + y + 17, dye.mapColor.colorValue or 0xFF000000.toInt())
                        }
                    }
                    drawStack(stack, guiLeft + x, guiTop + y)
                }
                index++
            }
            count = index
        }

        val s = 0.5f

        GlStateManager.pushMatrix()
        GlStateManager.translate(guiLeft.toDouble(), guiTop.toDouble() + 130, 0.0)
        GlStateManager.scale(s, s, s)
        val text = when (tile.type) {
            0 -> "1 sec"
            1 -> "10 sec"
            else -> "1 min"
        }
        drawRightString(text, 0, 0)
        GlStateManager.popMatrix()

        val top = guiTop.toDouble() - 1
        val bottom = guiTop + 120.0 - 1

        val max = 16
        for (i in 0..max) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(guiLeft.toDouble(), bottom + (top - bottom) * (i / max.toFloat()), 0.0)
            GlStateManager.scale(s, s, s)
            drawRightString("%d -".format(topValue * i / max), 0, 0)
            GlStateManager.popMatrix()
        }

        if (count > 45) {
            val pos = (46f * (offset / ((count / 15) - 3f))).toInt()
            Gui.drawRect(378, 167, 384, 177 + 46, 0x4F4C4C4C.toInt())
            Gui.drawRect(378, 167 + pos, 384, 177 + pos, 0xFF1C1C1C.toInt())
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
        } else if (keyCode == Keyboard.KEY_BACK) {
            controller.tileEntity!!.selected.fill(-1)
        } else if (keyCode == Keyboard.KEY_1) {

            Statistics.NETWORK.sendToServer(ClientEventPacket(controller.tileEntity!!.world.provider.dimension,
                    controller.tileEntity!!.pos, -1, 0))
        } else if (keyCode == Keyboard.KEY_2) {

            Statistics.NETWORK.sendToServer(ClientEventPacket(controller.tileEntity!!.world.provider.dimension,
                    controller.tileEntity!!.pos, -1, 1))
        } else if (keyCode == Keyboard.KEY_3) {

            Statistics.NETWORK.sendToServer(ClientEventPacket(controller.tileEntity!!.world.provider.dimension,
                    controller.tileEntity!!.pos, -1, 2))
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val tile = controller.tileEntity!!
        start@for (x in 0 until 15) {
            for (y in 0 until 3) {
                if (inside(guiLeft + x * 18 + 6, guiTop + y * 18 + 125, 18, 18, mouseX, mouseY)) {
                    val index = x + (y + offset) * 15
                    if (mouseButton == 0) {
                        if (index !in tile.selected) {
                            val indexOf = tile.selected.indexOf(-1)
                            if (indexOf == -1) {
                                setSelected(tile.pointer, index)
                                tile.pointer = (tile.pointer + 1) % tile.selected.size
                            } else {
                                setSelected(indexOf, index)
                            }
                        }
                    } else {
                        val indexOf = tile.selected.indexOf(index)
                        if (indexOf != -1) {
                            setSelected(indexOf, -1)

                        }
                    }
                    break@start
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun setSelected(index: Int, value: Int) {
        controller.tileEntity!!.selected[index] = value
        Statistics.NETWORK.sendToServer(ClientEventPacket(controller.tileEntity!!.world.provider.dimension,
                controller.tileEntity!!.pos, index, value))
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
                val data = pkt.nbt!!
                tileEntity!!.type = data.getInteger("Type")

                val values = data.getCompoundTag("Data")

                val items = mutableListOf<ItemIdentifier>()
                values.getTagList("Items", Constants.NBT.TAG_COMPOUND)!!.forEach {
                    items.add(ItemIdentifier.deserializeNBT(it))
                }
                tileEntity!!.items.addAll(items)

                val selected = mutableMapOf<Int, IntArray>()
                values.getTagList("Selected", Constants.NBT.TAG_COMPOUND)!!.forEach {
                    val index = it.getByte("Index").toInt()
                    val stats = it.getIntArray("Stats")
                    selected.put(index, stats)
                }
                tileEntity!!.dataArray.clear()
                for ((key, value) in selected) {
                    tileEntity!!.dataArray.put(key, value.asList().toMutableList())
                }
            }
        }
    }

    override fun writePacket(): GuiPacket {
        val data = NBTTagCompound()
        if (tileEntity != null) {
            data.setInteger("Type", tileEntity!!.type)
            if (tileEntity!!.type == 0) {
                val short = NBTTagCompound()
                short.setTag("Items", encodeItems(tileEntity!!.shortUpdates))
                short.setTag("Selected", encodeSelected(tileEntity!!.shortUpdates))
                data.setTag("Data", short)
            }
            if (tileEntity!!.type == 1) {
                val medium = NBTTagCompound()
                medium.setTag("Items", encodeItems(tileEntity!!.mediumUpdates))
                medium.setTag("Selected", encodeSelected(tileEntity!!.mediumUpdates))
                data.setTag("Data", medium)
            }
            if (tileEntity!!.type == 2) {
                val large = NBTTagCompound()
                large.setTag("Items", encodeItems(tileEntity!!.largeUpdates))
                large.setTag("Selected", encodeSelected(tileEntity!!.largeUpdates))
                data.setTag("Data", large)
            }
        }
        return GuiPacket(data)
    }

    private fun encodeItems(map: Map<ItemIdentifier, ItemStatistics>): NBTTagList {
        //items
        val itemList = NBTTagList()
        for (i in map.keys) {
            itemList.appendTag(i.serializeNBT())
        }
        return itemList
    }

    private fun encodeSelected(map: Map<ItemIdentifier, ItemStatistics>): NBTTagList {
        val nbt = NBTTagList()
        var index = 0
        for ((key, value) in map.entries.sortedBy { it.key.stack.item.registryName.toString() }) {
            if (index in tileEntity!!.selected) {
                val tag = NBTTagCompound()
                tag.setByte("Index", index.toByte())
                tag.setIntArray("Stats", value.stackSize.toIntArray())
                nbt.appendTag(tag)
            }
            index++
        }
        return nbt
    }
}

fun NBTTagList.forEach(action: (NBTTagCompound) -> Unit) {
    for (i in 0 until tagCount()) {
        action(getCompoundTagAt(i))
    }
}