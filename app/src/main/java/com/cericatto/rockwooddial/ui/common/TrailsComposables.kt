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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.ui.main_screen.LayoutConfig
import com.cericatto.rockwooddial.ui.main_screen.MainScreenAction
import com.cericatto.rockwooddial.ui.main_screen.MainScreenState

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