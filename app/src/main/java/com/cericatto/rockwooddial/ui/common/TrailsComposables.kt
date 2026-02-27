package com.cericatto.rockwooddial.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.ui.main_screen.LayoutConfig
import com.cericatto.rockwooddial.ui.main_screen.MainScreenAction
import com.cericatto.rockwooddial.ui.main_screen.MainScreenState
import com.cericatto.rockwooddial.data.Song

//--------------------------------------------------
//  TrailsSection
//--------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TrailsSection(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
	modifier: Modifier = Modifier,
) {
	val decades = listOf("50", "60", "70", "80", "90", "2000")
	val numSegs = decades.size

	BoxWithConstraints(modifier) {
		val totalWidthPx = constraints.maxWidth.toFloat()
		val segWidthPx = totalWidthPx / numSegs
		fun centerOf(i: Int): Float = segWidthPx * i + segWidthPx / 2f

		val activeIdx = decades.indexOf(state.currentDecade).coerceAtLeast(0)
		var offsetX by remember { mutableFloatStateOf(centerOf(activeIdx)) }

		LaunchedEffect(state.currentDecade) {
			offsetX = centerOf(decades.indexOf(state.currentDecade)
				.coerceAtLeast(0))
		}
		TrailsColumn(cfg = cfg)
		TrailsPointer(
			offsetX = offsetX,
			cfg = cfg,
			totalWidthPx = totalWidthPx,
			segWidthPx = segWidthPx,
			numSegs = numSegs,
			decades = decades,
			onOffsetXChange = { offsetX = it },
			onAction = onAction,
		)
	}
}

//--------------------------------------------------
// TrailsColumn
// trail_line + dots at TOP, flexible spacer in the middle,
// long_blue_trail + long_square_white_trail at BOTTOM.
//--------------------------------------------------

@Composable
fun TrailsColumn(
	cfg: LayoutConfig
) {
	Column(
		Modifier.fillMaxSize()
	) {
		// 1. trail_line_background_fixed — TOP
		Image(
			painter = painterResource(R.drawable.trail_line_background_fixed),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier
				.fillMaxWidth()
				.height(15.dp)
				.padding(2.dp),
		)
		// 2. point_background__mirrored — just below trail_line
		AndroidView(
			factory = { ctx ->
				android.widget.ImageView(ctx).apply {
					setImageResource(R.drawable.point_background__mirrored)
					scaleType = android.widget.ImageView.ScaleType.FIT_XY
				}
			},
			modifier = Modifier
				.fillMaxWidth()
				.height(12.dp)
				.padding(top = 3.dp)
				.padding(2.dp),
		)
		// Flexible spacer — stretches to fill the gap between top and bottom groups
		Spacer(Modifier.weight(1f))
		// 3. long_blue_trail — BOTTOM group
		Image(
			painter = painterResource(R.drawable.long_blue_trail),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier
				.fillMaxWidth()
				.height(15.dp),
		)
		// Fixed gap between blue and white trail
		Spacer(Modifier.height(cfg.trailsInnerWeight.dp))
		// 4. long_square_white_trail — bottommost; pointer tip aligns here
		Image(
			painter = painterResource(R.drawable.long_square_white_trail),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier
				.fillMaxWidth()
				.height(20.dp)
				.padding(bottom = 5.dp),
		)
	}
}

//--------------------------------------------------
// TrailsPointer
// Draggable red pencil overlaid on the trail images.
// fillMaxHeight() spans the full BoxWithConstraints height
// so the pointer tip aligns with the bottom of TrailsColumn.
//--------------------------------------------------

@Composable
fun TrailsPointer(
	offsetX: Float,
	cfg: LayoutConfig,
	totalWidthPx: Float,
	segWidthPx: Float,
	numSegs: Int,
	decades: List<String>,
	onOffsetXChange: (Float) -> Unit,
	onAction: (MainScreenAction) -> Unit,
) {
	val density = LocalDensity.current
	val pointerWPx = with(density) { cfg.pointerWidthDp.dp.toPx() }

	Image(
		painter = painterResource(R.drawable.pointer_alpha),
		contentDescription = "Decade pointer",
		contentScale = ContentScale.FillHeight,
		modifier = Modifier
			.width(cfg.pointerWidthDp.dp)
			.fillMaxHeight()
			.offset { IntOffset((offsetX - pointerWPx / 2f).toInt(), 0) }
			.zIndex(2f)
			.pointerInput(Unit) {
				detectDragGestures(
					onDragEnd = {
						val seg = (offsetX / segWidthPx).toInt().coerceIn(0, numSegs - 1)
						onOffsetXChange(segWidthPx * seg + segWidthPx / 2f)
						onAction(MainScreenAction.ChangeDecade(decades[seg]))
					},
				) { change, drag ->
					change.consume()
					onOffsetXChange((offsetX + drag.x).coerceIn(0f, totalWidthPx))
					onAction(MainScreenAction.OnTunerChanged(offsetX / totalWidthPx))
				}
			},
	)
}

//--------------------------------------------------
//  Preview Helpers
//--------------------------------------------------

private val PHONE_CFG = LayoutConfig(
	wToolbar = 2f,
	wCenter = 8f,
	wBottom = 2f,
	wWindow = 2f,
	wRadio = 5f,
	wDecades = 1f,
	wTrails = 1.2f,
	wSongInfo = 1.8f,
	seekbarWeight = 7f,
	spacerWeight = 0f,
	playBtnDp = 36f,
	knobDp = 123.75f,
	knobGapDp = 2f,
	pointerWidthDp = 19f,
	fontTitleSp = 49f,
	fontDecadeSp = 27f,
	fontSongSp = 21f,
	sliderHeightDp = 12f,
	trailsSpacerDp = 4f,
	trailsInnerWeight = 3f
)

private val TABLET_CFG = LayoutConfig(
	wToolbar = 2f,
	wCenter = 8f,
	wBottom = 2f,
	wWindow = 2f,
	wRadio = 5f,
	wDecades = 1f,
	wTrails = 1.2f,
	wSongInfo = 1.8f,
	seekbarWeight = 7f,
	spacerWeight = 0f,
	playBtnDp = 150f,
	knobDp = 264f,
	knobGapDp = 5f,
	pointerWidthDp = 22f,
	fontTitleSp = 77f,
	fontDecadeSp = 53f,
	fontSongSp = 40f,
	sliderHeightDp = 14f,
	trailsSpacerDp = 4f,
	trailsInnerWeight = 3f
)

private fun previewState(decade: String) = MainScreenState(
	songs = listOf(
		Song(
			"70",
			"1975",
			"Led Zeppelin",
			"Kashmir",
			"fJ9rUzIMcZQ"
		)
	),
	isLoading = false,
	isPlaying = false,
	currentDecade = decade,
	totalDurationSeconds = 354,
	currentPlaybackTimeSeconds = 60,
)

//--------------------------------------------------
// TrailsSection Previews
//--------------------------------------------------

// Phone — pointer on 70s (center)
@Preview(
	name = "TrailsSection / Phone / Pointer on 70s",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun TrailsSectionPhone70sPreview() {
	TrailsSection(
		state = previewState("70"),
		onAction = {},
		cfg = PHONE_CFG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — pointer on 50s (far left)
@Preview(
	name = "TrailsSection / Phone / Pointer on 50s",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun TrailsSectionPhone50sPreview() {
	TrailsSection(
		state = previewState("50"),
		onAction = {},
		cfg = PHONE_CFG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — pointer on 2000s (far right)
@Preview(
	name = "TrailsSection / Phone / Pointer on 2000s",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun TrailsSectionPhone2000sPreview() {
	TrailsSection(
		state = previewState("2000"),
		onAction = {},
		cfg = PHONE_CFG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — pointer on 70s
@Preview(
	name = "TrailsSection / Tablet / Pointer on 70s",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=1280dp,height=96dp,dpi=240",
)
@Composable
private fun TrailsSectionTablet70sPreview() {
	TrailsSection(
		state = previewState("70"),
		onAction = {},
		cfg = TABLET_CFG,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — pointer on 90s
@Preview(
	name = "TrailsSection / Tablet / Pointer on 90s",
	showBackground = true,
	backgroundColor = 0xFF000000,
	device = "spec:width=1280dp,height=96dp,dpi=240",
)
@Composable
private fun TrailsSectionTablet90sPreview() {
	TrailsSection(
		state = previewState("90"),
		onAction = {},
		cfg = TABLET_CFG,
		modifier = Modifier.fillMaxSize(),
	)
}

//--------------------------------------------------
// TrailsColumn Previews
//--------------------------------------------------

// TrailsColumn — phone height
@Preview(
	name = "TrailsColumn / Phone height",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 400,
	heightDp = 60,
)
@Composable
private fun TrailsColumnPhonePreview() {
	TrailsColumn(cfg = PHONE_CFG)
}

// TrailsColumn — tablet height (taller row, spacer stretches)
@Preview(
	name = "TrailsColumn / Tablet height",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 800,
	heightDp = 96,
)
@Composable
private fun TrailsColumnTabletPreview() {
	TrailsColumn(cfg = TABLET_CFG)
}

// TrailsColumn — extra tall (stress test: spacer must absorb height)
@Preview(
	name = "TrailsColumn / Extra tall",
	showBackground = true,
	backgroundColor = 0xFF000000,
	widthDp = 400,
	heightDp = 160,
)
@Composable
private fun TrailsColumnExtraTallPreview() {
	TrailsColumn(cfg = TABLET_CFG)
}