package de.mk.alarmclock.data

data class Sound(
    var id: Int,
    var name: String,
    var allowRandom: Boolean,
) {
    companion object {
        const val RANDOM = 0
    }
}