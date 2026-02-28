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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.data.Song
import com.cericatto.rockwooddial.ui.main_screen.LayoutConfig
import com.cericatto.rockwooddial.ui.main_screen.MainScreenAction
import com.cericatto.rockwooddial.ui.main_screen.MainScreenState
import com.cericatto.rockwooddial.ui.main_screen.PHONE_CONFIG
import com.cericatto.rockwooddial.ui.main_screen.TABLET_CONFIG
import com.cericatto.rockwooddial.ui.theme.DecadeBlue
import com.cericatto.rockwooddial.ui.theme.DecadeSelected
import com.cericatto.rockwooddial.ui.theme.NeonBlue
import com.cericatto.rockwooddial.ui.theme.SteelHighlight
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
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
	playerState: PlayerConstants.PlayerState
) {
	Row(
		modifier.background(Color.Black)
	) {
		// Left column: VU meters on top, YouTube player on bottom
		Column(
			Modifier
				.weight(cfg.wYoutubePlayer)
				.fillMaxHeight()
				.background(Color.Black)
		) {
			// VU Meters Row
			VuMetersRow(
				playerState = playerState,
				cfg = cfg,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.vuMeterHeightWeight),
			)
			VerticalDivider(modifier = Modifier.size(
				cfg.paddingBottomBetweenVuMetersAndYoutubePlayer.dp)
			)
			// YouTube player
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
						.align(Alignment.TopCenter)
				)
				// Transparent overlay to block all touches from reaching the WebView
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(16f / 9f)
						.align(Alignment.TopCenter)
						.pointerInput(Unit) {
							awaitPointerEventScope {
								while (true) {
									awaitPointerEvent()
								}
							}
						}
				)
			}
		}
		// Right column: decades + trails + song info
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
	Box(
		modifier.fillMaxWidth()
			.padding(
			start = 10.dp,
			end = 10.dp
		)
	) {
		Column(
			verticalArrangement = Arrangement.SpaceEvenly,
			horizontalAlignment = Alignment.Start,
			modifier = Modifier.fillMaxSize(),
		) {
			Text(
				text = song?.band ?: "",
				color = Color.White,
				fontSize = cfg.fontSongTitleSp.sp,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = song?.songTitle ?: "",
				color = SteelHighlight,
				fontSize = cfg.fontSongSp.sp,
				maxLines = 1,
				softWrap = false,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = song?.year ?: "",
				color = SteelHighlight,
				fontSize = cfg.fontSongSp.sp,
				maxLines = 1
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

//--------------------------------------------------
//  Preview helpers
//--------------------------------------------------

private fun stateWithSong(
	decade: String = "70",
	isPlaying: Boolean = false,
	playback: Int = 60,
	duration: Int = 354,
	band: String = "Led Zeppelin",
	title: String = "Kashmir",
	year: String = "1975",
) = MainScreenState(
	songs = listOf(Song(decade, year, band, title, "fJ9rUzIMcZQ")),
	isLoading = false,
	isPlaying = isPlaying,
	currentDecade = decade,
	totalDurationSeconds = duration,
	currentPlaybackTimeSeconds = playback,
)

private val emptyState = MainScreenState(
	songs = emptyList(),
	isLoading = false,
	isPlaying = false,
	currentDecade = "70",
	totalDurationSeconds = 0,
	currentPlaybackTimeSeconds = 0,
)

//--------------------------------------------------
//  MainScreenToolbar Previews
//--------------------------------------------------

@Preview(
	name = "Toolbar / Phone",
	showBackground = true,
	device = "spec:width=640dp,height=50dp,dpi=320",
)
@Composable
private fun ToolbarPhonePreview() {
	MainScreenToolbar(
		cfg = PHONE_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

@Preview(
	name = "Toolbar / Tablet",
	showBackground = true,
	device = "spec:width=1280dp,height=80dp,dpi=240",
)
@Composable
private fun ToolbarTabletPreview() {
	MainScreenToolbar(
		cfg = TABLET_CONFIG,
		modifier = Modifier.fillMaxSize(),
	)
}

//--------------------------------------------------
// MainScreenBottom Previews
//--------------------------------------------------

// Phone — paused, song loaded, slider at 25%
@Preview(
	name = "Bottom / Phone / Paused",
	showBackground = true,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun BottomPhonePausedPreview() {
	MainScreenBottom(
		state = stateWithSong(isPlaying = false, playback = 88, duration = 354),
		onAction = {},
		cfg = PHONE_CONFIG,
		youtubePlayer = null,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — playing, slider at 80%
@Preview(
	name = "Bottom / Phone / Playing",
	showBackground = true,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun BottomPhonePlayingPreview() {
	MainScreenBottom(
		state = stateWithSong(isPlaying = true, playback = 280, duration = 354),
		onAction = {},
		cfg = PHONE_CONFIG,
		youtubePlayer = null,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — no song loaded (slider and button disabled)
@Preview(
	name = "Bottom / Phone / No Song",
	showBackground = true,
	device = "spec:width=640dp,height=60dp,dpi=320",
)
@Composable
private fun BottomPhoneNoSongPreview() {
	MainScreenBottom(
		state = emptyState,
		onAction = {},
		cfg = PHONE_CONFIG,
		youtubePlayer = null,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — paused
@Preview(
	name = "Bottom / Tablet / Paused",
	showBackground = true,
	device = "spec:width=1280dp,height=100dp,dpi=240",
)
@Composable
private fun BottomTabletPausedPreview() {
	MainScreenBottom(
		state = stateWithSong(isPlaying = false, playback = 60, duration = 354),
		onAction = {},
		cfg = TABLET_CONFIG,
		youtubePlayer = null,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — playing
@Preview(
	name = "Bottom / Tablet / Playing",
	showBackground = true,
	device = "spec:width=1280dp,height=100dp,dpi=240",
)
@Composable
private fun BottomTabletPlayingPreview() {
	MainScreenBottom(
		state = stateWithSong(isPlaying = true, playback = 250, duration = 354),
		onAction = {},
		cfg = TABLET_CONFIG,
		youtubePlayer = null,
		modifier = Modifier.fillMaxSize(),
	)
}

//--------------------------------------------------
// MainScreenCenter Previews
// (YouTubePlayer can't render in preview, it shows black box)
//--------------------------------------------------

// Phone — 70s selected
@Preview(
	name = "Center / Phone / 70s selected",
	showBackground = true,
	device = "spec:width=640dp,height=280dp,dpi=320",
)
@Composable
private fun CenterPhone70sPreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = stateWithSong(decade = "70", band = "Led Zeppelin", title = "Kashmir", year = "1975"),
		onAction = {},
		cfg = PHONE_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — 80s selected, long song name tests ellipsis
@Preview(
	name = "Center / Phone / 80s selected / Long title",
	showBackground = true,
	device = "spec:width=640dp,height=280dp,dpi=320",
)
@Composable
private fun CenterPhone80sLongTitlePreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = stateWithSong(
			decade = "80",
			band = "Iron Maiden",
			title = "The Number of the Beast (Live at Hammersmith)",
			year = "1982",
		),
		onAction = {},
		cfg = PHONE_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

// Phone — no song loaded (empty state)
@Preview(
	name = "Center / Phone / No Song",
	showBackground = true,
	device = "spec:width=640dp,height=280dp,dpi=320",
)
@Composable
private fun CenterPhoneNoSongPreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = emptyState,
		onAction = {},
		cfg = PHONE_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — 90s selected
@Preview(
	name = "Center / Tablet / 90s selected",
	showBackground = true,
	device = "spec:width=1280dp,height=640dp,dpi=240",
)
@Composable
private fun CenterTablet90sPreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = stateWithSong(
			decade = "90",
			band = "Soundgarden",
			title = "Rusty Cage",
			year = "1991",
		),
		onAction = {},
		cfg = TABLET_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — 50s selected (first decade)
@Preview(
	name = "Center / Tablet / 50s selected",
	showBackground = true,
	device = "spec:width=1280dp,height=640dp,dpi=240",
)
@Composable
private fun CenterTablet50sPreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = stateWithSong(
			decade = "50",
			band = "Elvis Presley",
			title = "Hound Dog",
			year = "1956",
		),
		onAction = {},
		cfg = TABLET_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}

// Tablet — 2000s selected (last decade)
@Preview(
	name = "Center / Tablet / 2000s selected",
	showBackground = true,
	device = "spec:width=1280dp,height=640dp,dpi=240",
)
@Composable
private fun CenterTablet2000sPreview() {
	val ctx = androidx.compose.ui.platform.LocalContext.current
	MainScreenCenter(
		state = stateWithSong(
			decade = "2000",
			band = "The White Stripes",
			title = "Seven Nation Army",
			year = "2003",
		),
		onAction = {},
		cfg = TABLET_CONFIG,
		youTubePlayerView = YouTubePlayerView(ctx),
		playerState = PlayerConstants.PlayerState.PLAYING,
		modifier = Modifier.fillMaxSize(),
	)
}