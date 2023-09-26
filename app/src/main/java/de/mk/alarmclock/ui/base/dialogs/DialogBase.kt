package de.mk.alarmclock.ui.base.dialogs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class DialogBase {
    protected var show by mutableStateOf(false)
    fun show() = show.also { show = true }
    fun dismiss() = show.also { show = false }
    fun isShowing() = show
}