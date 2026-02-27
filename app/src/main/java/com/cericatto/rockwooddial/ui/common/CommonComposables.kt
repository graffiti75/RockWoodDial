package com.cericatto.rockwooddial.ui.common

import android.app.Activity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cericatto.rockwooddial.ui.theme.NeonBlue

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

// ─────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────

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