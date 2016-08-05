package com.cout970.statistics.util

/**
 * Created by cout970 on 05/08/2016.
 */

fun <T> T?.orElse(other: T) = if(this == null) other else this