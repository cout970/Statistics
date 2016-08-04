package com.cout970.statistics.gui

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

/**
 * Created by cout970 on 04/08/2016.
 */
abstract class GuiBase(val container: ContainerBase) : GuiContainer(container) {

    fun bindTexture(res: ResourceLocation) {
        mc.textureManager.bindTexture(res)
    }

    fun drawStack(stack: ItemStack, x: Int, y: Int) {
        mc.renderItem.renderItemIntoGUI(stack, x, y)
    }

    fun drawLine(points: List<Pair<Float, Float>>) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        for (i in points) {
            buffer.pos(i.first.toDouble(), i.second.toDouble(), 0.0).endVertex()
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }
}