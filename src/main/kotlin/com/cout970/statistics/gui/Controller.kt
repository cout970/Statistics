package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.tileentity.TileController
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ResourceLocation
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

            val snapshots = controller.tileEntity!!.shortSnapshots
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
                    val stack = key.stack.copy().apply { stackSize = 1 }

                    if (index in selected) {
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
            selected.fill(-1)
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        start@for (x in 0 until 15) {
            for (y in 0 until 3) {
                if (inside(guiLeft + x * 18 + 6, guiTop + y * 18 + 125, 18, 18, mouseX, mouseY)) {
                    val index = x + (y + offset) * 15
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
                val nbt = pkt.nbt!!

                if (nbt.hasKey("short")) {
                    tileEntity!!.shortSnapshots.clear()
                    val short = nbt.getTagList("short", Constants.NBT.TAG_LIST)
                    for (i in 0 until short.tagCount()) {
                        val shot = TileController.DataSnapshot()
                        (short[i] as NBTTagList).forEach {
                            val item = it.getShort("I").toInt()
                            val meta = it.getShort("M").toInt()
                            val tag = it.getCompoundTag("N")
                            val size = it.getShort("S").toInt()
                            val id = TileController.ItemIdentifier(ItemStack(Item.getItemById(item), 1, meta, tag))
                            shot.data.put(id, TileController.ItemStatistics(id).apply { this.amount = size })
                        }
                        tileEntity!!.shortSnapshots.add(shot)
                    }
                }

                if (nbt.hasKey("medium")) {
                    tileEntity!!.mediumSnapshots.clear()
                    val medium = nbt.getTagList("medium", Constants.NBT.TAG_LIST)
                    for (i in 0 until medium.tagCount()) {
                        val shot = TileController.DataSnapshot()
                        val data = mutableMapOf<TileController.ItemIdentifier, TileController.ItemStatistics>()
                        (medium[i] as NBTTagList).forEach {
                            val item = it.getInteger("I")
                            val meta = it.getInteger("M")
                            val tag = it.getCompoundTag("N")
                            val size = it.getInteger("S")
                            val id = TileController.ItemIdentifier(ItemStack(Item.getItemById(item), 1, meta, tag))
                            shot.data.put(id, TileController.ItemStatistics(id).apply { this.amount = size })
                        }
                        tileEntity!!.mediumSnapshots.add(shot)
                    }
                }

                if (nbt.hasKey("large")) {
                    tileEntity!!.largeSnapshots.clear()
                    val large = nbt.getTagList("large", Constants.NBT.TAG_LIST)
                    for (i in 0 until large.tagCount()) {
                        val shot = TileController.DataSnapshot()
                        val data = mutableMapOf<TileController.ItemIdentifier, TileController.ItemStatistics>()
                        (large[i] as NBTTagList).forEach {
                            val item = ItemStack.loadItemStackFromNBT(it.getCompoundTag("Item"))
                            val id = TileController.ItemIdentifier(item)
                            shot.data.put(id, TileController.ItemStatistics(id).apply { amount = nbt.getInteger("stacksize") })
                        }
                        tileEntity!!.largeSnapshots.add(shot)
                    }
                }
            }
        }
    }

    override fun writePacket(): GuiPacket {
        val data = NBTTagCompound()
        if (tileEntity != null) {
            val short = NBTTagList()
            val medium = NBTTagList()
            val large = NBTTagList()

            for (shot in tileEntity!!.shortSnapshots) {
                val list = NBTTagList()
                for ((key, value) in shot.data) {
                    val nbt = NBTTagCompound()
                    nbt.setShort("I", Item.getIdFromItem(key.stack.item).toShort())
                    nbt.setShort("M", key.stack.metadata.toShort())
                    key.stack.tagCompound?.let { nbt.setTag("N", it) }
                    nbt.setShort("S", value.amount.toShort())
                    list.appendTag(nbt)
                }
                short.appendTag(list)
            }

            for (shot in tileEntity!!.mediumSnapshots) {
                val list = NBTTagList()
                for ((key, value) in shot.data) {
                    val nbt = NBTTagCompound()
                    nbt.setTag("Item", key.stack.serializeNBT())
                    nbt.setInteger("stacksize", value.amount)
                    list.appendTag(nbt)
                }
                medium.appendTag(list)
            }

            for (shot in tileEntity!!.largeSnapshots) {
                val list = NBTTagList()
                for ((key, value) in shot.data) {
                    val nbt = NBTTagCompound()
                    nbt.setTag("Item", key.stack.serializeNBT())
                    nbt.setInteger("stacksize", value.amount)
                    list.appendTag(nbt)
                }
                large.appendTag(list)
            }

            data.setTag("short", short)
//            data.setTag("medium", medium)
//            data.setTag("large", large)
        }
        return GuiPacket(data)
    }

}

fun NBTTagList.forEach(action: (NBTTagCompound) -> Unit) {
    for (i in 0 until tagCount()) {
        action(getCompoundTagAt(i))
    }
}