package com.romarickc.reminder.presentation.screen.notifSettings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults.snapBehavior
import androidx.wear.compose.material.*
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rememberRotaryHapticHandler
import com.romarickc.reminder.presentation.theme.ReminderTheme
import com.romarickc.reminder.presentation.utils.UiEvent

@Composable
fun NotifSettingsScreen(
    viewModel: NotifSettingsViewModel = hiltViewModel(),
    onPopBackStack: (UiEvent.PopBackStack) -> Unit,
) {
    val currentNotifPref = viewModel.currentPref.collectAsState(initial = 0).value

    // handle navigations there
    LaunchedEffect(key1 = true, block = {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.PopBackStack -> {
                    onPopBackStack(event)
                }
                else -> Unit
            }
        }
    })

    NotifSettingsContent(onEvent = viewModel::onEvent, notifPref = currentNotifPref)
    Log.i("notifPref", "$currentNotifPref")
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalHorologistApi::class)
@Composable
fun NotifSettingsContent(
    onEvent: (NotifSettingsEvents) -> Unit,
    notifPref: Int,
) {
    val items = listOf("Every hour", "Every 3 hours", "Deactivated")

    val pickerState = rememberPickerState(
        initialNumberOfOptions = items.size,
        initiallySelectedOption = notifPref,
        repeatItems = false
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Picker(
            state = pickerState,
            contentDescription = "Notification Setting",
            modifier = Modifier.height(140.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = (items[it]),
                    style = with(LocalDensity.current) {
                        MaterialTheme.typography.display3.copy(
                            fontWeight = FontWeight.Medium,
                            // Ignore text scaling
                            fontSize = MaterialTheme.typography.display3.fontSize.value.dp.toSp()
                        )
                    },
                    color = MaterialTheme.colors.primary,
                    // In case of overflow, minimize weird layout behavior
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
        Button(
            onClick = {
                onEvent(NotifSettingsEvents.OnValueChange(pickerState.selectedOption))
            },
            colors = ButtonDefaults.secondaryButtonColors(),
            modifier = Modifier
                .size(ButtonDefaults.DefaultButtonSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                tint = MaterialTheme.colors.primary,
                contentDescription = "Settings",
                modifier = Modifier
                    .padding(2.dp)
            )
        }
    }
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun Preview() {
    ReminderTheme {
        NotifSettingsContent(onEvent = {}, notifPref = 0)
    }
}