package miguel.brainz2.grid

import java.lang.IllegalArgumentException

data class Entry(val x: Int, val str: String) {
    init {
        if (x>8) throw IllegalArgumentException()
    }
}