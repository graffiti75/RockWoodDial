package com.cericatto.rockwooddial.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.ui.main_screen.LayoutConfig
import com.cericatto.rockwooddial.ui.main_screen.MainScreenAction
import com.cericatto.rockwooddial.ui.main_screen.MainScreenState
import com.cericatto.rockwooddial.ui.theme.DecadeBlue
import com.cericatto.rockwooddial.ui.theme.DecadeSelected
import com.cericatto.rockwooddial.ui.theme.NeonBlue
import com.cericatto.rockwooddial.ui.theme.SteelHighlight
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun MainScreenToolbar(
	cfg: LayoutConfig,
	modifier: Modifier = Modifier
) {
	Box(
		modifier, contentAlignment = Alignment.CenterStart
	) {
		Image(
			painter = painterResource(R.drawable.up_background),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier.fillMaxSize(),
		)
		Text(
			text = "Rock Dial Dates",
			color = Color(0xFF111111),
			fontSize = cfg.fontTitleSp.sp,
			fontWeight = FontWeight.Bold,
			fontFamily = FontFamily.Cursive,
			fontStyle = FontStyle.Italic,
			maxLines = 1,
			softWrap = false,
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 8.dp, end = 8.dp),
		)
	}
}

@Composable
fun MainScreenCenter(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
	youTubePlayerView: YouTubePlayerView,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier.background(Color.Black)
	) {
		// YouTube window column
		Column(
			Modifier
				.weight(cfg.wWindow)
				.fillMaxHeight()
		) {
			Box(
				Modifier
					.fillMaxWidth()
					.weight(1f)
					.background(Color.Black)
			) {
				AndroidView(
					factory = { youTubePlayerView },
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(16f / 9f)
						.align(Alignment.TopCenter),
				)
			}
		}
		// Radio column: decades + trails + song info
		Column(
			Modifier
				.weight(cfg.wRadio)
				.fillMaxHeight()
				.background(Color.Black)
		) {
			DecadesSection(
				state = state,
				onAction = onAction,
				cfg = cfg,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wDecades),
			)
			TrailsSection(
				state = state,
				onAction = onAction,
				cfg = cfg,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wTrails),
			)
			SongInfoSection(
				state = state,
				cfg = cfg,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wSongInfo),
			)
		}
	}
}

@Composable
fun MainScreenBottom(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
	youtubePlayer: YouTubePlayer?,
	modifier: Modifier = Modifier,
) {
	Box(modifier) {
		Image(
			painter = painterResource(R.drawable.bottom_background),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier.fillMaxSize(),
		)
		// Slider — fills width leaving room for play button + knob
		Row(
			Modifier
				.fillMaxSize()
				.padding(start = 8.dp, end = (cfg.playBtnDp + cfg.knobDp).dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(Modifier.weight(1f)) {
				ProgressSection(
					state = state,
					youtubePlayer = youtubePlayer,
					onAction = onAction,
					cfg = cfg,
				)
			}
		}
		// Play button — CenterEnd, offset left by knobDp to sit flush with the knob
		Box(
			Modifier
				.size(cfg.playBtnDp.dp)
				.align(Alignment.CenterEnd)
				.offset(x = (-cfg.knobDp).dp)
				.clickable(enabled = state.currentSong != null) {
					onAction(MainScreenAction.PlayPause)
				},
			contentAlignment = Alignment.Center,
		) {
			Icon(
				imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
				contentDescription = if (state.isPlaying) "Pause" else "Play",
				tint = Color.White,
				modifier = Modifier.size((cfg.playBtnDp * 0.8f).dp),
			)
		}
	}
}

@Composable
private fun DecadesSection(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
	modifier: Modifier = Modifier,
) {
	val decades = listOf("50", "60", "70", "80", "90", "2000")
	Row(
		modifier.padding(top = 5.dp, start = 5.dp, end = 5.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		decades.forEach { decade ->
			val selected = decade == state.currentDecade
			Box(
				Modifier
					.weight(1f)
					.fillMaxHeight()
					.clickable { onAction(MainScreenAction.ChangeDecade(decade)) },
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = if (decade == "2000") "2000s" else "${decade}s",
					color = if (selected) DecadeSelected else DecadeBlue,
					fontSize = cfg.fontDecadeSp.sp,
					fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
					textAlign = TextAlign.Center,
					maxLines = 1,
					softWrap = false,
				)
			}
		}
	}
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun SongInfoSection(
	state: MainScreenState,
	cfg: LayoutConfig,
	modifier: Modifier = Modifier,
) {
	val song = state.currentSong
	Box(modifier.padding(start = 10.dp, end = 10.dp)) {
		Column(
			Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.SpaceEvenly,
		) {
			Text(
				text = song?.band ?: "",
				color = Color.White,
				fontSize = cfg.fontSongSp.sp,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			Text(
				text = song?.songTitle ?: "",
				color = SteelHighlight,
				fontSize = cfg.fontSongSp.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(end = 172.dp),
			)
			Text(
				text = song?.year ?: "",
				color = SteelHighlight,
				fontSize = cfg.fontSongSp.sp,
				maxLines = 1,
			)
		}
	}
}

@Composable
private fun ProgressSection(
	state: MainScreenState,
	youtubePlayer: YouTubePlayer?,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
) {
	var sliderPos by remember(state.currentPlaybackTimeSeconds) {
		mutableFloatStateOf(state.currentPlaybackTimeSeconds.toFloat())
	}
	val totalFloat = state.totalDurationSeconds.toFloat().takeIf { it > 0f } ?: 100f

	Slider(
		value = sliderPos,
		onValueChange = { sliderPos = it },
		onValueChangeFinished = {
			youtubePlayer?.seekTo(sliderPos)
			onAction(MainScreenAction.SeekTo(sliderPos.toInt()))
		},
		valueRange = 0f..totalFloat,
		modifier = Modifier
			.fillMaxWidth()
			.height(cfg.sliderHeightDp.dp),
		enabled = state.currentSong != null && state.totalDurationSeconds > 0,
		colors = androidx.compose.material3.SliderDefaults.colors(
			thumbColor = Color.White,
			activeTrackColor = NeonBlue,
			inactiveTrackColor = Color(0xFF444444),
		),
	)
}