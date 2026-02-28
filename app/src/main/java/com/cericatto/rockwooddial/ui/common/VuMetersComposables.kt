package com.cericatto.rockwooddial.ui.common

import android.media.AudioManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Needle sweep range in degrees.
// -60f = far left, 0f = center, +60f = far right
private const val MIN_ANGLE = 135f
private const val MAX_ANGLE = 45f

@Composable
fun VuMetersRow(
	playerState: PlayerConstants.PlayerState,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		PowerMeter(
			modifier = Modifier
				.weight(1f)
				.fillMaxHeight(),
		)
		SignalMeter(
			playerState = playerState,
			modifier = Modifier
				.weight(1f)
				.fillMaxHeight(),
		)
	}
}

@Composable
private fun PowerMeter(
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	var volumeFraction by remember { mutableFloatStateOf(0f) }

	LaunchedEffect(Unit) {
		val audioManager = context.getSystemService(AudioManager::class.java)
		while (true) {
			val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
			val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
			volumeFraction = if (max > 0f) current / max else 0f
			delay(300)
		}
	}

	val needleAngle by animateFloatAsState(
		targetValue = MIN_ANGLE + (volumeFraction * (MAX_ANGLE - MIN_ANGLE)),
		animationSpec = tween(durationMillis = 300),
		label = "power_needle"
	)

	VuMeterCanvas(
		label = "POWER",
		needleAngle = needleAngle,
		modifier = modifier,
	)
}

@Composable
private fun SignalMeter(
	playerState: PlayerConstants.PlayerState,
	modifier: Modifier = Modifier,
) {
	val targetAngle = when (playerState) {
		PlayerConstants.PlayerState.PLAYING -> MAX_ANGLE
		PlayerConstants.PlayerState.BUFFERING -> 90f
		else -> MIN_ANGLE
	}

	val needleAngle by animateFloatAsState(
		targetValue = targetAngle,
		animationSpec = tween(durationMillis = 600),
		label = "signal_needle"
	)

	VuMeterCanvas(
		label = "SIGNAL",
		needleAngle = needleAngle,
		modifier = modifier,
	)
}

@Composable
private fun VuMeterCanvas(
	label: String,
	needleAngle: Float,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.background(Color.Black)
			.border(1.dp, Color(0xFF888888))
			.padding(4.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceBetween,
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
		) {
			Canvas(modifier = Modifier.fillMaxSize()) {
				val w = size.width
				val h = size.height

				// Pivot point: center-bottom of the canvas
				val pivotX = w / 2f
				val pivotY = h * 0.85f

				// Arc radius
				val arcRadius = w * 0.42f

				// Draw the blue arc
				val arcColor = Color(0xFF1E6FD9)
				val arcStroke = h * 0.07f
				val arcSteps = 60
				for (i in 0 until arcSteps) {
					val t1 = i.toFloat() / arcSteps
					val t2 = (i + 1).toFloat() / arcSteps
					// Sweep from MIN_ANGLE to MAX_ANGLE (135 down to 45)
					val a1 = Math.toRadians((MIN_ANGLE - t1 * (MIN_ANGLE - MAX_ANGLE)).toDouble())
					val a2 = Math.toRadians((MIN_ANGLE - t2 * (MIN_ANGLE - MAX_ANGLE)).toDouble())
					val x1 = pivotX + arcRadius * cos(a1).toFloat()
					val y1 = pivotY - arcRadius * sin(a1).toFloat()
					val x2 = pivotX + arcRadius * cos(a2).toFloat()
					val y2 = pivotY - arcRadius * sin(a2).toFloat()
					drawLine(
						color = arcColor,
						start = Offset(x1, y1),
						end = Offset(x2, y2),
						strokeWidth = arcStroke,
						cap = StrokeCap.Round,
					)
				}

				// Draw tick marks
				val tickColor = Color(0xFF888888)
				val numTicks = 7
				for (i in 0..numTicks) {
					val fraction = i.toFloat() / numTicks
					val angle =
						Math.toRadians((MIN_ANGLE - fraction * (MIN_ANGLE - MAX_ANGLE)).toDouble())
					val innerR = arcRadius - arcStroke
					val outerR = arcRadius + arcStroke * 0.5f
					val sx = pivotX + innerR * cos(angle).toFloat()
					val sy = pivotY - innerR * sin(angle).toFloat()
					val ex = pivotX + outerR * cos(angle).toFloat()
					val ey = pivotY - outerR * sin(angle).toFloat()
					drawLine(
						color = tickColor,
						start = Offset(sx, sy),
						end = Offset(ex, ey),
						strokeWidth = 2.dp.toPx(),
					)
				}

				// Draw the needle — 50% shorter
				val needleRad = Math.toRadians(needleAngle.toDouble())
				val needleLength = (arcRadius + arcStroke) * 0.5f
				val needleEndX = pivotX + needleLength * cos(needleRad).toFloat()
				val needleEndY = pivotY - needleLength * sin(needleRad).toFloat()
				drawLine(
					color = Color(0xFFCCCCCC),
					start = Offset(pivotX, pivotY),
					end = Offset(needleEndX, needleEndY),
					strokeWidth = 2.dp.toPx(),
					cap = StrokeCap.Round,
				)

				// Draw pivot dot
				drawCircle(
					color = Color(0xFFAAAAAA),
					radius = 4.dp.toPx(),
					center = Offset(pivotX, pivotY),
				)
			}
		}

		Text(
			text = label,
			color = Color(0xFF1E6FD9),
			fontSize = 10.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 2.dp),
		)
	}
}

//--------------------------------------------------
//  Previews
//--------------------------------------------------

@Preview(
	name = "VuMeters / Power min + Signal idle",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 300,
	heightDp = 100,
)
@Composable
private fun VuMetersIdlePreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.UNSTARTED,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Power mid + Signal buffering",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 300,
	heightDp = 100,
)
@Composable
private fun VuMetersBufferingPreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.BUFFERING,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Power max + Signal playing",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 300,
	heightDp = 100,
)
@Composable
private fun VuMetersPlayingPreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Signal paused",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 300,
	heightDp = 100,
)
@Composable
private fun VuMetersPausedPreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.PAUSED,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Signal ended",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 300,
	heightDp = 100,
)
@Composable
private fun VuMetersEndedPreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.ENDED,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Power needle left (volume 0)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasPowerMinPreview() {
	VuMeterCanvas(
		label = "POWER",
		needleAngle = MIN_ANGLE,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Power needle center (volume 5)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasPowerMidPreview() {
	VuMeterCanvas(
		label = "POWER",
		needleAngle = 90f,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Power needle right (volume 10)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasPowerMaxPreview() {
	VuMeterCanvas(
		label = "POWER",
		needleAngle = MAX_ANGLE,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Signal needle left (idle)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasSignalMinPreview() {
	VuMeterCanvas(
		label = "SIGNAL",
		needleAngle = MIN_ANGLE,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Signal needle center (buffering)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasSignalMidPreview() {
	VuMeterCanvas(
		label = "SIGNAL",
		needleAngle = 90f,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeterCanvas / Signal needle right (playing)",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 150,
	heightDp = 100,
)
@Composable
private fun VuMeterCanvasSignalMaxPreview() {
	VuMeterCanvas(
		label = "SIGNAL",
		needleAngle = MAX_ANGLE,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Tablet size",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 500,
	heightDp = 150,
)
@Composable
private fun VuMetersTabletPreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "VuMeters / Phone size",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 200,
	heightDp = 70,
)
@Composable
private fun VuMetersPhonePreview() {
	VuMetersRow(
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}