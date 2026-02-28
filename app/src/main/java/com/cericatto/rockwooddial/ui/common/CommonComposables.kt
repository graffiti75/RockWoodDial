package com.cericatto.rockwooddial.ui.common

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.ui.main_screen.LayoutConfig
import com.cericatto.rockwooddial.ui.main_screen.PHONE_CONFIG
import com.cericatto.rockwooddial.ui.main_screen.TABLET_CONFIG
import com.cericatto.rockwooddial.ui.theme.NeonBlue
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun LockScreenOrientation(orientation: Int) {
	val context = LocalContext.current
	DisposableEffect(Unit) {
		val activity = context as? Activity ?: return@DisposableEffect onDispose {}
		val original = activity.requestedOrientation
		activity.requestedOrientation = orientation
		onDispose { activity.requestedOrientation = original }
	}
}

@Composable
fun ErrorBanner(
	message: String,
	onDismiss: () -> Unit
) {
	val offsetY by animateDpAsState(targetValue = 0.dp, label = "error_slide")
	Box(
		Modifier
			.fillMaxSize()
			.padding(16.dp)
			.offset(y = offsetY),
		contentAlignment = Alignment.BottomCenter,
	) {
		Surface(
			color = Color(0xFF8B0000),
			shape = RoundedCornerShape(8.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			Row(
				Modifier.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(
					message,
					color = Color.White,
					fontSize = 12.sp,
					modifier = Modifier.weight(1f)
				)
				TextButton(onClick = onDismiss) {
					Text("Next", color = NeonBlue)
				}
			}
		}
	}
}

@Composable
fun LoadingOverlay() {
	Box(
		Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.6f)),
	) {
		CircularProgressIndicator(
			modifier = Modifier.align(Alignment.Center),
			color = NeonBlue
		)
	}
}

@Composable
fun KnobVolumeControl(
	cfg: LayoutConfig,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val audioManager = context.getSystemService(AudioManager::class.java)

	val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
		.takeIf { it > 0f } ?: 1f
	val initialVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
		.coerceAtLeast(0f)
	val initialAngle = 30f + (initialVol / maxVol) * 120f

	var knobAngle by remember { mutableFloatStateOf(initialAngle) }
	val animatedAngle by animateFloatAsState(
		targetValue = knobAngle,
		animationSpec = tween(durationMillis = 300),
		label = "knob_rotation"
	)

	// Listen for external volume changes (hardware buttons, system UI, etc.)
	DisposableEffect(Unit) {
		val receiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context, intent: Intent) {
				val streamType = intent.getIntExtra(
					"android.media.EXTRA_VOLUME_STREAM_TYPE", -1
				)
				if (streamType == AudioManager.STREAM_MUSIC) {
					val newVol = audioManager.getStreamVolume(
						AudioManager.STREAM_MUSIC
					).toFloat()
					knobAngle = 30f + (newVol / maxVol) * 120f
				}
			}
		}
		val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
		context.registerReceiver(receiver, filter)
		onDispose { context.unregisterReceiver(receiver) }
	}

	Image(
		painter = painterResource(R.drawable.button_round_big),
		contentDescription = "Volume knob",
		contentScale = ContentScale.Fit,
		modifier = modifier
			.graphicsLayer {
				rotationZ = animatedAngle
			}
			.pointerInput(Unit) {
				val centerX = size.width / 2f
				val centerY = size.height / 2f
				var lastAngle = knobAngle

				detectDragGestures(
					onDragStart = { offset ->
						lastAngle = Math.toDegrees(
							atan2(
								(offset.y - centerY).toDouble(),
								(offset.x - centerX).toDouble()
							)
						).toFloat()
					}
				) { change, _ ->
					change.consume()

					val currentTouchAngle = Math.toDegrees(
						atan2(
							(change.position.y - centerY).toDouble(),
							(change.position.x - centerX).toDouble()
						)
					).toFloat()

					var delta = currentTouchAngle - lastAngle
					if (delta > 180f) delta -= 360f
					if (delta < -180f) delta += 360f

					lastAngle = currentTouchAngle
					knobAngle = (knobAngle + delta).coerceIn(30f, 150f)

					val volumeFraction = (knobAngle - 30f) / 120f
					val newVol = (volumeFraction * maxVol)
						.roundToInt()
						.coerceIn(0, maxVol.toInt())
					audioManager.setStreamVolume(
						AudioManager.STREAM_MUSIC,
						newVol,
						0
					)
				}
			},
	)
}

//--------------------------------------------------
//  Previews
//--------------------------------------------------

// ErrorBanner — short message (phone landscape)
@Preview(
	name = "ErrorBanner / Short message",
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
)
@Composable
private fun ErrorBannerShortPreview() {
	Surface(color = Color(0xFF1A1A1A)) {
		ErrorBanner(
			message = "Player error: UNKNOWN",
			onDismiss = {},
		)
	}
}

// ErrorBanner — long message that should truncate/wrap
@Preview(
	name = "ErrorBanner / Long message",
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
)
@Composable
private fun ErrorBannerLongPreview() {
	Surface(color = Color(0xFF1A1A1A)) {
		ErrorBanner(
			message = "Player error: VIDEO_NOT_FOUND — " +
				"the requested YouTube video could not be loaded. " +
				"Please check your connection and try again.",
			onDismiss = {},
		)
	}
}

// ErrorBanner — on tablet
@Preview(
	name = "ErrorBanner / Tablet",
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
)
@Composable
private fun ErrorBannerTabletPreview() {
	Surface(color = Color(0xFF1A1A1A)) {
		ErrorBanner(
			message = "Player error: UNKNOWN",
			onDismiss = {},
		)
	}
}

// LoadingOverlay — on phone landscape
@Preview(
	name = "LoadingOverlay / Phone",
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
)
@Composable
private fun LoadingOverlayPhonePreview() {
	// Dark wood-like background to mimic real app context
	Box(Modifier.fillMaxSize().background(Color(0xFF3B2A1A))) {
		LoadingOverlay()
	}
}

// LoadingOverlay — on tablet
@Preview(
	name = "LoadingOverlay / Tablet",
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
)
@Composable
private fun LoadingOverlayTabletPreview() {
	Box(Modifier.fillMaxSize().background(Color(0xFF3B2A1A))) {
		LoadingOverlay()
	}
}

// Both overlays layered — shows how ErrorBanner sits above a semi-transparent overlay
@Preview(
	name = "ErrorBanner over dark background / Phone",
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
)
@Composable
private fun ErrorBannerOverDarkPreview() {
	Box(Modifier.fillMaxSize().background(Color(0xFF3B2A1A))) {
		LoadingOverlay()
		ErrorBanner(message = "Player error: UNKNOWN", onDismiss = {})
	}
}

//--------------------------------------------------
//  KnobVolumeControl Previews
//--------------------------------------------------

// Phone — knob at minimum volume (30°)
@Preview(
	name = "KnobVolumeControl / Phone / Volume min",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 124,
	heightDp = 124,
)
@Composable
private fun KnobVolumeMinPhonePreview() {
	KnobVolumeControl(
		cfg = PHONE_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — knob at mid volume (90°)
@Preview(
	name = "KnobVolumeControl / Phone / Volume mid",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 124,
	heightDp = 124,
)
@Composable
private fun KnobVolumeMidPhonePreview() {
	KnobVolumeControl(
		cfg = PHONE_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — knob at maximum volume (150°)
@Preview(
	name = "KnobVolumeControl / Phone / Volume max",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 124,
	heightDp = 124,
)
@Composable
private fun KnobVolumeMaxPhonePreview() {
	KnobVolumeControl(
		cfg = PHONE_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — knob size
@Preview(
	name = "KnobVolumeControl / Tablet",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 171,
	heightDp = 171,
)
@Composable
private fun KnobVolumeTabletPreview() {
	KnobVolumeControl(
		cfg = TABLET_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

// KnobVolumeControl inside realistic bottom bar context — phone
@Preview(
	name = "KnobVolumeControl / Phone / In context",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=640dp,height=80dp,dpi=320",
)
@Composable
private fun KnobVolumeInContextPhonePreview() {
	Box(Modifier.fillMaxSize()) {
		KnobVolumeControl(
			cfg = PHONE_CONFIG,
			modifier = Modifier
				.size(PHONE_CONFIG.knobDp.dp)
				.align(Alignment.BottomEnd)
				.padding(bottom = 8.dp, end = 8.dp),
		)
	}
}

// KnobVolumeControl inside realistic bottom bar context — tablet
@Preview(
	name = "KnobVolumeControl / Tablet / In context",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=1280dp,height=120dp,dpi=240",
)
@Composable
private fun KnobVolumeInContextTabletPreview() {
	Box(Modifier.fillMaxSize()) {
		KnobVolumeControl(
			cfg = TABLET_CONFIG,
			modifier = Modifier
				.size(TABLET_CONFIG.knobDp.dp)
				.align(Alignment.BottomEnd)
				.padding(bottom = 8.dp, end = 8.dp),
		)
	}
}