package com.romarickc.reminder.presentation.screen.intakeTarget

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import com.romarickc.reminder.presentation.theme.MyBlue
import com.romarickc.reminder.presentation.theme.ReminderTheme
import com.romarickc.reminder.presentation.utils.Constants
import com.romarickc.reminder.presentation.utils.UiEvent
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun IntakeTargetScreen(
    viewModel: IntakeTargetViewModel = hiltViewModel(),
    onPopBackStack: (UiEvent.PopBackStack) -> Unit,
) {
    val intakeTarget =
        viewModel.currentTarget.collectAsState(initial = Constants.MIN_INTAKE).value

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
    // Log.i("intaketargetscreen", "${intakeTarget.value}")
    IntakeTargetContent(onEvent = viewModel::onEvent, intakeTarget = intakeTarget)
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IntakeTargetContent(
    onEvent: (IntakeTargetEvents) -> Unit,
    intakeTarget: Int,
) {
    val items = (Constants.MIN_INTAKE..Constants.MAX_INTAKE).toList()

    val pickerState = rememberPickerState(
        initialNumberOfOptions = items.size,
        initiallySelectedOption = intakeTarget - Constants.MIN_INTAKE,
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
                    text = String.format(Locale.getDefault(), "%,d", items[it]),
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
                onEvent(IntakeTargetEvents.OnValueChange(pickerState.selectedOption + Constants.MIN_INTAKE))
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
        IntakeTargetContent(onEvent = {}, Constants.RECOMMENDED_INTAKE)
    }
}