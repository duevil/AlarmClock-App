package de.mk.alarmclock.data

data class Light(
    var duty: Int,
    var duration: Int,
) {
    companion object {
        val dutyRange = 0..7
        val durationRange = 0..255
    }
}