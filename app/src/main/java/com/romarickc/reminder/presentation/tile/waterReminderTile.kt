package com.romarickc.reminder.presentation.tile

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.romarickc.reminder.domain.repository.WaterIntakeRepository
import com.romarickc.reminder.presentation.MainActivity
import com.romarickc.reminder.presentation.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject
import androidx.wear.protolayout.material.Typography
import com.romarickc.reminder.presentation.theme.SeafoamGreen

private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
@AndroidEntryPoint
class WaterReminderTile : SuspendingTileService() {
    @Inject
    lateinit var repository: WaterIntakeRepository

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                tileLayout(
                                    requestParams.deviceConfiguration
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(Constants.REFRESH_INTERVAL_TILE)
            .build()
    }

    private suspend fun tileLayout(
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
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

        return LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        Clickable.Builder()
                            .setOnClick(
                                ActionBuilders.LaunchAction.Builder()
                                    .setAndroidActivity(
                                        ActionBuilders.AndroidActivity.Builder()
                                            .setClassName(
                                                MainActivity::class.qualifiedName ?: ""
                                            )
                                            .setPackageName(this.packageName)
                                            .build()
                                    )
                                    .build()
                            ).build(),
                    )
                    .build()
            )
            .addContent(
                layout2(
                    currentIntake = currentIntake,
                    targetVal = targetVal,
                    deviceParameters,
                ).build(),
            ).build()
    }

    private fun layout2(
        currentIntake: Int,
        targetVal: Int,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ) = EdgeContentLayout.Builder(deviceParameters).setResponsiveContentInsetEnabled(true)
        .setEdgeContent(
            CircularProgressIndicator.Builder()
                .setProgress(currentIntake.toFloat() / targetVal.toFloat())
                .setCircularProgressIndicatorColors(colorThing())
                .build()
        )
        .setPrimaryLabelTextContent(
            Text.Builder(baseContext, "Intake")
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(argb(SeafoamGreen.toArgb()))
                .build()
        )
        .setSecondaryLabelTextContent(
            Text.Builder(baseContext, "/$targetVal")
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(argb(Color.White.toArgb()))
                .build()
        )
        .setContent(
            Text.Builder(baseContext, "$currentIntake")
                .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                .setColor(argb(Color.White.toArgb()))
                .build()
        )
}

private fun colorThing() = ProgressIndicatorColors(
    argb(SeafoamGreen.toArgb()),
    argb(Color(1f, 1f, 1f, 0.1f).toArgb())
)