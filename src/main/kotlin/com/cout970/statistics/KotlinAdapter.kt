package com.cout970.statistics

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by cout970 on 04/08/2016.
 */
class KotlinAdapter : ILanguageAdapter {

    override fun supportsStatics(): Boolean = false

    override fun setProxy(target: Field?, proxyTarget: Class<*>?, proxy: Any?) {
        target?.set(null, proxy)
    }

    override fun getNewInstance(container: FMLModContainer?, objectClass: Class<*>?, classLoader: ClassLoader?, factoryMarkedAnnotation: Method?): Any? {
        return objectClass?.getField("INSTANCE")?.get(null)
    }

    override fun setInternalProxies(mod: ModContainer?, side: Side?, loader: ClassLoader?) {
    }
}