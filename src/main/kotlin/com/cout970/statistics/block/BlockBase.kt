package com.cout970.statistics.block

import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import wiresegal.zenmodelloader.common.block.base.BlockMod

/**
 * Created by cout970 on 04/08/2016.
 */
abstract class BlockBase(mat:Material, name: String, vararg variants: String) : BlockMod(name, mat, mat.materialMapColor, *variants) {

    init{
        setCreativeTab(CreativeTabs.REDSTONE)
        setHardness(2.0f)
    }
}