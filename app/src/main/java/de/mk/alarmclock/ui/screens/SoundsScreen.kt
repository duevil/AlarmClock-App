package de.mk.alarmclock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.mk.alarmclock.MainViewModel
import de.mk.alarmclock.R
import de.mk.alarmclock.api.toMutable
import de.mk.alarmclock.data.Data
import de.mk.alarmclock.data.Sound
import de.mk.alarmclock.ui.base.Icon
import de.mk.alarmclock.ui.base.Icons
import de.mk.alarmclock.ui.base.composeName
import de.mk.alarmclock.ui.base.dialogs.ConfirmDialog
import de.mk.alarmclock.ui.base.dialogs.TextInputDialog
import de.mk.alarmclock.ui.base.items.SmallTextItem
import de.mk.alarmclock.ui.base.items.TextItem

@Composable
fun MainViewModel.SoundsScreen() {
    val sounds = Data.SOUNDS.get<Set<Sound>>().toMutable()
    var selected: Sound? by remember { mutableStateOf(null) }
    var forDeletion: Sound? by remember { mutableStateOf(null) }
    val isAdded by remember(selected, sounds.value) {
        derivedStateOf { !sounds.value.contains(selected) }
    }
    var showEditMenu by remember { mutableStateOf(false) }
    val nameDialog = TextInputDialog()
    val confirmDeleteDialog = ConfirmDialog()

    LaunchedEffect(selected) { nameDialog.takeIf { (selected != null) and !showEditMenu }?.show() }
    LaunchedEffect(forDeletion) { confirmDeleteDialog.takeIf { forDeletion != null }?.show() }

    nameDialog(
        title = if (isAdded) R.string.add_sound else R.string.rename_sound,
        initial = selected?.name ?: "",
        onConfirm = { newName ->
            selected?.name = newName
            selected?.takeIf { isAdded }?.let(::addSound) ?: selected?.let(::setSound)
            selected = null
        },
        onDismiss = { selected = null },
    )
    forDeletion?.let {
        confirmDeleteDialog(
            title = R.string.confirm_delete,
            message = stringResource(R.string.delete_sound_confirmation, it.name, it.id),
            onDismiss = { forDeletion = null },
            onConfirm = { deleteSound(it) },
        )
    }
    Column {
        if (sounds.value.isEmpty()) SmallTextItem(
            text = stringResource(R.string.error_loading_sound),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        )
        sounds.value.sortedBy(Sound::id).forEach {
            TextItem(
                headlineText = it.composeName(),
                supportingText = "#${it.id}",
                leadingContent = {
                    DropdownMenu(
                        expanded = showEditMenu and (selected == it),
                        onDismissRequest = { showEditMenu = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play") },
                            onClick = { playSound(it) },
                            trailingIcon = { Icons.Play() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                forDeletion = it
                                showEditMenu = false
                            },
                            trailingIcon = { Icons.Delete() },
                            enabled = it.editable
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_up)) },
                            onClick = {
                                with(sounds) {
                                    value.firstOrNull { s -> s.id == it.id - 1 }?.let { prev ->
                                        val index = value.indexOf(prev)
                                        value = value.toMutableList().apply {
                                            set(index, it.copy(id = it.id - 1))
                                            set(index + 1, prev.copy(id = prev.id + 1))
                                        }.toSet()
                                    }
                                    updateData()
                                }
                                showEditMenu = false
                            },
                            trailingIcon = { Icons.ArrowUp() },
                            enabled = it.editable && it.id > (sounds.value
                                .filter { s -> s.editable }
                                .map { s -> s.id }
                                .minByOrNull { id -> id }
                                ?: -1)
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_down)) },
                            onClick = {
                                with(sounds) {
                                    value.firstOrNull { s -> s.id == it.id + 1 }?.let { next ->
                                        val index = value.indexOf(next)
                                        value = value.toMutableList().apply {
                                            set(index, it.copy(id = it.id + 1))
                                            set(index - 1, next.copy(id = next.id - 1))
                                        }.toSet()
                                    }
                                    updateData()
                                }
                                showEditMenu = false
                            },
                            trailingIcon = { Icons.ArrowDown() },
                            enabled = it.editable && it.id < (sounds.value
                                .map { s -> s.id }
                                .maxByOrNull { id -> id }
                                ?: -1)
                        )
                    }
                    it.Icon()
                },
                trailingContent = {
                    if (it.editable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { it.allowRandom = !it.allowRandom }
                        ) {
                            FilledIconToggleButton(
                                checked = it.allowRandom,
                                onCheckedChange = { b -> setSound(it.copy(allowRandom = b)) }
                            ) { Icons.Random() }
                        }
                    }
                },
                onClick = { if (it.editable) selected = it },
                onLongClick = {
                    selected = it
                    showEditMenu = true
                },
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalIconButton(
                onClick = {
                    val id = sounds.value.maxOfOrNull { it.id }?.let { it + 1 } ?: 0
                    val newSound = Sound(id, "", false)
                    selected = newSound
                },
                content = { Icons.Add() },
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .size(52.dp)
            )
        }
    }
}

private inline val Sound.editable get() = id != Sound.RANDOM