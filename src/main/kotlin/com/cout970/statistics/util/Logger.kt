package com.cout970.statistics.util

import com.cout970.statistics.Statistics

/**
 * Created by cout970 on 04/08/2016.
 */

fun info(obj:Any?){
    Statistics.LOGGER.info(obj.toString())
}

fun error(obj:Any?){
    Statistics.LOGGER.error(obj.toString())
}

fun debug(obj:Any?){
    Statistics.LOGGER.info("[DEBUG] "+ obj.toString())
}