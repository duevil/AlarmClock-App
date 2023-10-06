package de.mk.alarmclock.data

data class Player(
    var volume: Int,
) {
    companion object {
        val volumeRange = 0..30
    }
}

