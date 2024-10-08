package com.romarickc.reminder.presentation.screen.home

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.romarickc.reminder.R
import com.romarickc.reminder.presentation.screen.intakeTarget.IntakeTargetViewModel
import com.romarickc.reminder.presentation.theme.MyBlue
import com.romarickc.reminder.presentation.theme.ReminderTheme
import com.romarickc.reminder.presentation.utils.Constants
import com.romarickc.reminder.presentation.utils.Page
import com.romarickc.reminder.presentation.utils.UiEvent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    viewModel2: IntakeTargetViewModel = hiltViewModel(),
    onNavigate: (UiEvent.Navigate) -> Unit,
) {
    val intakesCnttoday = viewModel.intakesToday.collectAsState(initial = 0).value
    val intakeTarget =
        viewModel2.currentTarget.collectAsState(initial = Constants.MIN_INTAKE).value
    // Log.i("home_screen", "intake today cnt $intakesCnttoday")

    LaunchedEffect(key1 = true, block = {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    onNavigate(event)
                }

                else -> Unit
            }
        }
    })

    HomeContent(
        intakecnt = intakesCnttoday,
        intakeTarget = intakeTarget,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeContent(
    intakecnt: Int,
    intakeTarget: Int,
    onEvent: (HomeScreenEvents) -> Unit
) {
    Page {
        // title
        item {
            ListHeader {
                Text(text = "Water Reminder")
            }
        }

        val intakepercentage = (intakecnt * 100) / intakeTarget

        // intake overview and mod
        item {
            AppCard(
                appImage = {
                    Icon(
                        imageVector = Icons.Rounded.LocalDrink,
                        contentDescription = "none",
                        modifier = Modifier.requiredSize(15.dp)
                    )
                },
                appName = { Text("Target", color = MaterialTheme.colors.primary) },
                time = { Text("$intakepercentage%", color = MaterialTheme.colors.secondary) },
                title = { Text("Daily intake", color = MaterialTheme.colors.onSurface) },
                modifier = Modifier
                    .padding(2.dp),
                onClick = { onEvent(HomeScreenEvents.OnAddNewIntakeClick) },
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            R.string.water_intake,
                            intakecnt,
                            intakeTarget,
                            String.format("%.2f", intakecnt * 0.25),
                        ),
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }

        // intake history
        item {
            Chip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                    //.padding(top = 5.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "triggers water intake history action",
                        modifier = Modifier
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colors.onPrimary,
                        text = "Intake History"
                    )
                },
                onClick = {
                    onEvent(HomeScreenEvents.OnIntakeHistory)
                }
            )
        }

        // header
        item {
            ListHeader {
                Text(text = "Tips")
            }
        }

        // others hydration & intake history
        items(1) { index ->
            Chip(
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    when (index) {
                        0 -> Icon(
                            imageVector = Icons.Rounded.SelfImprovement,
                            contentDescription = "triggers hydration tips action",
                            modifier = Modifier
                        )

                        1 -> Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "triggers water intake history action",
                            modifier = Modifier
                        )
                    }
                },
//                    colors = ChipDefaults.chipColors(
//                        backgroundColor = MyBlue
//                    ),
                label = {
                    when (index) {
                        0 -> {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onPrimary,
                                text = "Hydration Tips"
                            )
                        }
                    }
                },
                onClick = {
                    when (index) {
                        0 -> {
                            onEvent(HomeScreenEvents.OnHydraTips)
                        }
                    }
                }
            )
        }

        // title
        item {
            ListHeader {
                Text(text = "Settings")
            }
        }

        // daily target & notifications, & import/export
        items(2) { index ->
            Chip(
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    when (index) {
                        0 -> Icon(
                            imageVector = Icons.Rounded.CrisisAlert,
                            contentDescription = "triggers set target action",
                            modifier = Modifier
                        )

                        1 -> Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "triggers notifications settings action",
                            modifier = Modifier
                        )

                        2 -> Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "triggers Import/Export action",
                            modifier = Modifier
                        )

                    }
                },
                label = {
                    when (index) {
                        0 -> {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onSecondary,
                                text = "Set Target"
                            )
                        }

                        1 -> {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onPrimary,
                                text = "Notifications Settings"
                            )
                        }

                        2 -> {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onPrimary,
                                text = "Import/Export data"
                            )
                        }
                    }
                },
                onClick = {
                    when (index) {
                        0 -> {
                            onEvent(HomeScreenEvents.OnSetTarget)
                        }

                        1 -> {
                            onEvent(HomeScreenEvents.OnNotifSettings)
                        }

                        2 -> {
                            onEvent(HomeScreenEvents.OnImportExportData)
                        }
                    }
                }
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun Preview() {
    ReminderTheme {
        HomeContent(
            intakecnt = 7, Constants.RECOMMENDED_INTAKE,
            onEvent = {})
    }
}