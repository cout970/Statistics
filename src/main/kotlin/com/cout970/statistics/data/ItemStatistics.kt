package com.cout970.statistics.data

import java.util.*

class ItemStatistics() {
    var stackSize = LinkedList<Int>()

    constructor(array: IntArray) : this(){
        stackSize.addAll(array.asSequence())
    }
}