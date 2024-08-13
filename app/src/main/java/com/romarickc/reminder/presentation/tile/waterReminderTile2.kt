package com.romarickc.reminder.presentation.tile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.romarickc.reminder.R
import com.romarickc.reminder.domain.repository.WaterIntakeRepository
import com.romarickc.reminder.presentation.MainActivity
import com.romarickc.reminder.presentation.utils.Constants
import com.romarickc.reminder.presentation.utils.WaterReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.wear.protolayout.material.Typography
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.romarickc.reminder.presentation.theme.SeafoamGreen
import com.romarickc.reminder.presentation.theme.wearColorPalette
import java.util.Locale

private const val RESOURCES_VERSION = "0"
private const val IMAGE_GLASS = "glass"
private const val ADD_GLASS = "add_glass"

@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
class WaterReminderTile2 : SuspendingTileService() {
    @Inject
    lateinit var repository: WaterIntakeRepository

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return Resources.Builder()
            .addIdToImageMapping(
                IMAGE_GLASS, drawableResToImageResource(R.drawable.water_full)
            )
            .setVersion(RESOURCES_VERSION).build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        if (requestParams?.currentState?.lastClickableId == ADD_GLASS) {
            Log.i("addintake", "add intake")
            repository.insertIntake()

            // update the worker to not send any notification that is like a minute away
            val notifPref = repository.getNotifPref(1).firstOrNull() ?: 0
            //Toast.makeText(application, "Registered", Toast.LENGTH_SHORT).show()
            when (notifPref) {
                1 -> {
                    // the 3 hours interval
                    updatePeriodicWorkInterval(3)
                }

                2 -> {
                    // notification disabled, so I don't care
                }

                0 -> {
                    // the 1 hour interval
                    updatePeriodicWorkInterval(1)
                }
            }
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(
                    tileLayout(
                        requestParams.deviceConfiguration!!,
                        baseContext,
                        repository
                    )
                ).build()
            ).build()
        ).build()

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(Constants.REFRESH_INTERVAL_TILE).build()
    }

    private fun updatePeriodicWorkInterval(interval: Long) {
        Log.i("register intake notif", "called")
        var workInfos =
            WorkManager.getInstance(application).getWorkInfosByTag("water_reminder_periodic_work")
                .get()

        Log.i("worker settings", "current cancelling and recreating $workInfos")
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            interval,
            TimeUnit.HOURS
        ).setInitialDelay(interval, TimeUnit.HOURS).setConstraints(
            Constraints.Builder().build()
        ).addTag("water_reminder_periodic_work") // set the tag here
            .build()

        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "water_reminder_periodic_work",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
        workInfos =
            WorkManager.getInstance(application).getWorkInfosByTag("water_reminder_periodic_work")
                .get()

        Log.i("worker register intake", "new one -> $workInfos")
    }
}

val buttonColors = ButtonColors.primaryButtonColors(
    Colors(
        wearColorPalette.primary.toArgb(),
        wearColorPalette.onPrimary.toArgb(),
        wearColorPalette.surface.toArgb(),
        wearColorPalette.onSurface.toArgb()
    )
)

private suspend fun tileLayout(
    deviceParameters: DeviceParameters,
    baseContext: Context,
    repository: WaterIntakeRepository
): LayoutElementBuilders.LayoutElement {

    val now: ZonedDateTime = ZonedDateTime.now()
    val startOfDay: ZonedDateTime = now.toLocalDate().atStartOfDay(now.zone)
    val startOfDayTimestamp = startOfDay.toInstant().toEpochMilli()

    val currentIntake: Int = withContext(Dispatchers.IO) {
        repository.getCountTgtThis(startOfDayTimestamp).first()
    }

    val targetVal: Int = withContext(Dispatchers.IO) {
        repository.getTarget(1).first()
    }

    // Log.i("tile out", "currentIntake $currentIntake targetVal $targetVal")

    return LayoutElementBuilders.Column.Builder().setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setClickable(
                    ModifiersBuilders.Clickable.Builder()
                        .setOnClick(
                            ActionBuilders.LaunchAction.Builder().setAndroidActivity(
                                ActionBuilders.AndroidActivity.Builder()
                                    .setClassName(MainActivity::class.qualifiedName ?: "")
                                    .setPackageName(baseContext.packageName).build()
                            ).build()
                        )
                        .build()
                )
                .build()
        )
        .addContent(
            layout3(
                currentIntake = currentIntake,
                targetVal = targetVal,
                deviceParameters,
                baseContext
            ).build()
        ).build()
}

private fun layout3(
    currentIntake: Int,
    targetVal: Int,
    deviceParameters: DeviceParameters,
    baseContext: Context
) = PrimaryLayout.Builder(deviceParameters).setResponsiveContentInsetEnabled(true)
    .setPrimaryLabelTextContent(
//        Text.Builder(baseContext, "Intake").setTypography(Typography.TYPOGRAPHY_CAPTION1)
//            .setColor(argb(SeafoamGreen.toArgb())).build()
        Image.Builder()
            .setResourceId(IMAGE_GLASS)
            .setWidth(dp(25f))
            .setHeight(dp(25f))
            .setColorFilter(
                ColorFilter.Builder()
                    .setTint(argb(SeafoamGreen.toArgb()))
                    .build()
            )
            .build()
    ).setContent(
        Column.Builder()
            .addContent(
                Text.Builder(
                    baseContext,
                    if(currentIntake == 0)
                        "No data"
                    else String.format(Locale.getDefault(), "%,d", currentIntake)
                ).setTypography(Typography.TYPOGRAPHY_DISPLAY2)
                    .setColor(argb(SeafoamGreen.toArgb())).build()
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(
                        dp(5f)
                    )
                    .build()
            )
            .addContent(
                Text.Builder(
                    baseContext,
                    String.format(Locale.getDefault(), "/ %,d", targetVal)
                ).setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .setColor(argb(Color(0xFFA6ADB7).toArgb())).build()
            )
            .build()
    ).setPrimaryChipContent(
        CompactChip.Builder(
            baseContext, "Add glass",             ModifiersBuilders.Clickable.Builder()
                .setOnClick(
                    ActionBuilders.LoadAction.Builder().build()
                )
                .setId(
                    ADD_GLASS
                )
                .build(), deviceParameters
        ).setChipColors(
            ChipColors(
                argb(SeafoamGreen.toArgb()),
                argb(wearColorPalette.onPrimary.toArgb())
            )
        ).build()
    )

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(
    onTileResourceRequest = { request ->
        Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                IMAGE_GLASS, drawableResToImageResource(R.drawable.water_full)
            )
            .build()
    },
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            LayoutElementBuilders.Column.Builder().setWidth(DimensionBuilders.expand())
                .setHeight(DimensionBuilders.expand())

                .addContent(
                    layout3(
                        currentIntake = 4000,
                        targetVal = 10000,
                        request.deviceConfiguration,
                        context
                    ).build()
                ).build()
        ).build()
    }
)