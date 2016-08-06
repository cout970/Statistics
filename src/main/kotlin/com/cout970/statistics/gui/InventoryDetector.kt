package com.cout970.statistics.gui

import com.cout970.statistics.Statistics
import com.cout970.statistics.data.ItemIdentifier
import com.cout970.statistics.network.ClientEventPacket
import com.cout970.statistics.network.GuiPacket
import com.cout970.statistics.tileentity.TileInventoryDetector
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiTextField
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
import java.awt.Color

/**
 * Created by cout970 on 06/08/2016.
 */
class GuiInventoryDetector(val cont: ContainerInventoryDetector) : GuiBase(cont) {

    lateinit var text: GuiTextField
    var offset = 0
    var count = 0
    var newLimit = -1

    override fun initGui() {
        super.initGui()
        text = object : GuiTextField(0, fontRendererObj, guiLeft + 80 - 2, guiTop + 67 + 8, 90, 18) {
            override fun setFocused(isFocusedIn: Boolean) {
                super.setFocused(isFocusedIn)
                if (!isFocusedIn) {
                    var amount = cont.tile!!.limit
                    try {
                        val i = Integer.parseInt(text)
                        if (i >= 0) {
                            amount = i
                        }
                    } catch (e: Exception) {
                    }
                    text = amount.toString()
                    cont.tile!!.limit = amount
                    newLimit = amount
                    Statistics.NETWORK.sendToServer(ClientEventPacket(cont.tile!!.world.provider.dimension, cont.tile!!.pos, 0, amount))
                }
            }
        }
        text.text = cont.tile!!.limit.toString()
    }

    override fun updateScreen() {
        super.updateScreen()
        text.updateCursorCounter()
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {

        if (cont.tile!!.limit != newLimit) {
            newLimit = cont.tile!!.limit
            text.text = cont.tile!!.limit.toString()
        }

        val tile = cont.tile!!
        val start = guiTop + 67
        //background
        Gui.drawRect(guiLeft, guiTop + 67, guiLeft + xSize, guiTop + 100, 0x7F4C4C4C.toInt())

        //active/disable
        val item = ItemStack(Blocks.STAINED_GLASS_PANE, 1, if (tile.active) 5 else 14)
        drawStack(item, guiLeft + 9, start + 9)

        //item
        Gui.drawRect(guiLeft + 35, start + 8, guiLeft + 53, start + 26, 0x7FFFFFFF.toInt())
        Gui.drawRect(guiLeft + 35 - 1, start + 8 - 1, guiLeft + 53 + 1, start + 26 + 1, 0x7F000000.toInt())
        synchronized(tile) {
            val selected = tile.itemCache
            if (selected != null) {
                drawStack(selected.stack.copy().apply { stackSize = 1 }, guiLeft + 9 + 18 + 9, start + 9)
            }
        }

        //comparator
        drawBox(guiLeft + 59, start + 11, 6 + 8, 8 + 4, 0x3FFFFFFF.toInt())
        drawCenteredString(fontRendererObj, tile.operation.toString(), guiLeft + 65, start + 13, Color.WHITE.rgb)

        //text
        text.drawTextBox()

        //items
        val move = -20
        Gui.drawRect(guiLeft, guiTop + 123 + move, guiLeft + xSize, guiTop + 125 + 54 + move, 0x4F4C4C4C.toInt())
        synchronized(tile) {
            var index = 0
            val allItems = tile.items.keys.sortedBy { it.stack.item.registryName.toString() }
            val dye = EnumDyeColor.byMetadata(0)
            for (i in allItems) {
                if (index / 9 < 3 + offset && index / 9 - offset >= 0) {
                    val x = index % 9 * 18 + 7
                    val y = (index / 9 - offset) * 18 + 125 + move
                    val stack = i.stack.copy().apply { stackSize = 1 }

                    if (tile.getItem(index) == tile.itemCache) {
                        if (stack.item != Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE)) {
                            GlStateManager.pushMatrix()
                            GlStateManager.translate(0.0, 0.0, -20.0)
                            drawStack(ItemStack(Blocks.STAINED_GLASS_PANE, 1, dye.metadata), guiLeft + x, guiTop + y)
                            GlStateManager.popMatrix()
                        } else {
                            Gui.drawRect(guiLeft + x - 1, guiTop + y - 1, guiLeft + x + 17, guiTop + y + 17, dye.mapColor.colorValue or 0x5F000000.toInt())
                        }
                    }
                    drawStack(stack, guiLeft + x, guiTop + y)
                }
                index++
            }
            count = index
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        if (inside(guiLeft + 9 + 18 + 9 - 1, guiTop + 67 + 9 - 1, 18, 18, mouseX, mouseY)) {
            if (cont.tile!!.amount != -1) {
                drawHoveringText(listOf("Amount: " + cont.tile!!.amount.toString()), mouseX - guiLeft, mouseY - guiTop)
            }
        }
        if (inside(guiLeft + 8, guiTop + 67 + 8, 18, 18, mouseX, mouseY)) {
            drawHoveringText(listOf(cont.tile!!.active.toString()), mouseX - guiLeft, mouseY - guiTop)
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
    }

    fun drawBox(x: Int, y: Int, sizeX: Int, sizeY: Int, color: Int = 0x7F4C4C4C.toInt()) {
        Gui.drawRect(x, y, x + sizeX, y + sizeY, color)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val dwheel = Mouse.getDWheel()
        if (dwheel < 0) {
            if (count / 9 > 3 + offset) {
                offset++
            }
        } else if (dwheel > 0) {
            if (offset > 0) {
                offset--
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        text.mouseClicked(mouseX, mouseY, mouseButton)
        if (mouseButton == 0) {
            if (inside(guiLeft + 59, guiTop + 67 + 11, 6 + 8, 8 + 4, mouseX, mouseY)) {
                Statistics.NETWORK.sendToServer(ClientEventPacket(cont.tile!!.world.provider.dimension, cont.tile!!.pos, 1, cont.tile!!.operation.ordinal + 1))
            }
        }
        val move = -20
        val tile = cont.tile!!
        start@for (x in 0 until 9) {
            for (y in 0 until 3) {
                if (inside(guiLeft + x * 18 + 6, guiTop + y * 18 + 125 + move, 18, 18, mouseX, mouseY)) {
                    val index = x + (y + offset) * 9
                    if (mouseButton == 0) {
                        if (index < count && tile.getItem(index) != tile.itemCache) {
                            Statistics.NETWORK.sendToServer(ClientEventPacket(cont.tile!!.world.provider.dimension, cont.tile!!.pos, 2, index))
                        }
                    } else {
                        if (tile.getItem(index) == tile.itemCache) {
                            Statistics.NETWORK.sendToServer(ClientEventPacket(cont.tile!!.world.provider.dimension, cont.tile!!.pos, 2, -1))
                        }
                    }
                    break@start
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        text.textboxKeyTyped(typedChar, keyCode)
        if (keyCode == Keyboard.KEY_DOWN) {
            if (count / 9 > 3 + offset) {
                offset++
            }
        } else if (keyCode == Keyboard.KEY_UP) {
            if (offset > 0) {
                offset--
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

}

class ContainerInventoryDetector(world: World, pos: BlockPos, player: EntityPlayer) : ContainerBase(world, pos, player) {

    val tile by lazy {
        val t = world.getTileEntity(pos)
        if (t is TileInventoryDetector) {
            t
        } else {
            null
        }
    }

    override fun readPacket(pkt: GuiPacket) {
        if (tile != null) {
            val nbt = pkt.nbt!!

            val map = mutableMapOf<ItemIdentifier, Int>()
            nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND).forEach {
                val item = ItemIdentifier.deserializeNBT(it)
                val size = it.getInteger("Extra")
                map.put(item, size)
            }
            synchronized(tile!!) {
                tile!!.operation = TileInventoryDetector.Operation.values()[nbt.getInteger("Operation")]
                tile!!.active = nbt.getBoolean("Active")
                if (!nbt.getCompoundTag("Cache").hasNoTags()) {
                    tile!!.itemCache = ItemIdentifier.deserializeNBT(nbt.getCompoundTag("Cache"))
                } else {
                    tile!!.itemCache = null
                }
                tile!!.limit = nbt.getInteger("Limit")
                tile!!.amount = nbt.getInteger("Amount")
                tile!!.items.clear()
                tile!!.items.putAll(map)
            }
        }
    }

    override fun writePacket(): GuiPacket? = GuiPacket(NBTTagCompound().apply {
        setTag("Items", encodeItems(tile!!.items))
        setTag("Cache", tile!!.itemCache?.serializeNBT() ?: NBTTagCompound())
        setInteger("Limit", tile!!.limit)
        setInteger("Amount", tile!!.amount)
        setInteger("Operation", tile!!.operation.ordinal)
        setBoolean("Active", tile!!.getRedstoneLevel() > 0)
    })

    private fun encodeItems(map: Map<ItemIdentifier, Int>): NBTTagList {
        //items
        val itemList = NBTTagList()
        for ((key, value) in map) {
            itemList.appendTag(key.serializeNBT().apply { setInteger("Extra", value) })
        }
        return itemList
    }

}