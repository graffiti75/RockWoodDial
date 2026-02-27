package com.cericatto.rockwooddial.ui.main_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.data.Song
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

//--------------------------------------------------
//  Colors
//--------------------------------------------------

private val DecadeBlue = Color(0xFF00AAFF)
private val DecadeSelected = Color(0xFF00DDFF)
private val NeonBlue = Color(0xFF00CFFF)
private val SteelHighlight = Color(0xFFCCCCCC)

//--------------------------------------------------
//  LayoutConfig
//--------------------------------------------------

private data class LayoutConfig(
	val wToolbar: Float,
	val wCenter: Float,
	val wBottom: Float,
	val wWindow: Float,
	val wRadio: Float,
	val wDecades: Float,
	val wTrails: Float,
	val wSongInfo: Float,
	val seekbarWeight: Float,
	val spacerWeight: Float,
	val playBtnDp: Float,
	val knobDp: Float,
	val knobGapDp: Float,
	val pointerWidthDp: Float,
	val fontTitleSp: Float,
	val fontDecadeSp: Float,
	val fontSongSp: Float,
	val sliderHeightDp: Float,
	val trailsSpacerDp: Float,
	val trailsInnerWeight: Float,
)

private val PHONE_CONFIG = LayoutConfig(
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
	trailsInnerWeight = 3f,
)

private val TABLET_CONFIG = LayoutConfig(
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
	trailsInnerWeight = 3f,
)

@Composable
fun MainScreenRoot(viewModel: MainScreenViewModel = hiltViewModel()) {
	LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	val state by viewModel.state.collectAsStateWithLifecycle()
	val configuration = LocalConfiguration.current
	val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
	val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
	val isTablet = (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) && isLandscape
	LaunchedEffect(Unit) { viewModel.checkIfIsTabletAndLandscape(isTablet) }
	MainScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun MainScreen(state: MainScreenState, onAction: (MainScreenAction) -> Unit) {
	val context = LocalContext.current
	val youTubePlayerView = remember { YouTubePlayerView(context) }
	val youtubePlayerState = remember { mutableStateOf<YouTubePlayer?>(null) }
	val youtubePlayer = youtubePlayerState.value
	val lifecycleOwner = LocalLifecycleOwner.current

	YoutubeListenerDisposableEffect(
		state = state,
		onAction = onAction,
		lifecycleOwner = lifecycleOwner,
		youTubePlayerView = youTubePlayerView,
		youtubePlayerState = youtubePlayerState,
	)

	LaunchedEffect(youtubePlayer, state.currentSong) {
		youtubePlayer?.let { player ->
			state.currentSong?.let { song ->
				if (state.isPlaying)
					player.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
				else
					player.cueVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
			}
		}
	}

	LaunchedEffect(youtubePlayer, state.isPlaying) {
		youtubePlayer?.let { player ->
			if (state.currentSong != null) {
				if (state.isPlaying) player.play() else player.pause()
			}
		}
	}

	val screenWidthDp = LocalConfiguration.current.screenWidthDp
	val screenLayout =
		LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
	val isTablet = screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE && screenWidthDp > 800
	val cfg = if (isTablet) TABLET_CONFIG else PHONE_CONFIG

	Box(Modifier.fillMaxSize()) {

		Image(
			painter = painterResource(R.drawable.wood_background),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier.fillMaxSize(),
		)

		Column(
			Modifier
				.fillMaxSize()
				.padding(horizontal = 8.dp, vertical = 6.dp)
		) {

			// ── toolbar ──────────────────────────────────────────
			Box(
				Modifier
					.fillMaxWidth()
					.weight(cfg.wToolbar),
				contentAlignment = Alignment.CenterStart,
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

			// ── center ───────────────────────────────────────────
			Row(
				Modifier
					.fillMaxWidth()
					.weight(cfg.wCenter)
					.background(Color.Black),
			) {
				Column(
					Modifier
						.weight(cfg.wWindow)
						.fillMaxHeight(),
				) {
					Box(
						Modifier
							.fillMaxWidth()
							.weight(1f)
							.background(Color.Black),
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

				Column(
					Modifier
						.weight(cfg.wRadio)
						.fillMaxHeight()
						.background(Color.Black),
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

			// ── bottom ───────────────────────────────────────────
			Box(
				Modifier
					.fillMaxWidth()
					.weight(cfg.wBottom),
			) {
				Image(
					painter = painterResource(R.drawable.bottom_background),
					contentDescription = null,
					contentScale = ContentScale.FillBounds,
					modifier = Modifier.fillMaxSize(),
				)
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

				// Play button — inside bottom Box, aligned to its right edge minus knob width
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

		// Knob — outer Box, BottomEnd
		Image(
			painter = painterResource(R.drawable.button_round_big),
			contentDescription = "Skip song",
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.size(cfg.knobDp.dp)
				.align(Alignment.BottomEnd)
				.clickable { onAction(MainScreenAction.NextSong) },
		)

		when {
			state.isLoading -> LoadingOverlay()
			state.error != null -> ErrorBanner(
				message = state.error,
				onDismiss = { onAction(MainScreenAction.DismissError) },
			)
		}
	}
}

//--------------------------------------------------
//  DecadesSection
//--------------------------------------------------

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

//--------------------------------------------------
//  TrailsSection
//  The trail images are fixed dp heights. They stack from the
//  TOP of the box. The pointer spans the full box height.
//  On phone the box is short so pointer bottom ≈ white trail bottom.
//  On tablet the box is tall so pointer bottom is far below white trail.
//
//  Fix: align the trail Column to the BOTTOM of the BoxWithConstraints.
//  This way the white trail's bottom edge = box bottom = pointer bottom.
//  Both phone and tablet will show the pencil grounded on the white trail.
//--------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun TrailsSection(
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
			offsetX = centerOf(decades.indexOf(state.currentDecade).coerceAtLeast(0))
		}

		val density = LocalDensity.current

		// trail_line + dots sit at the TOP.
		// A weight(1f) spacer fills the middle gap (stretches on tablet, tiny on phone).
		// long_blue_trail + long_square_white_trail sit at the BOTTOM.
		// The pointer fillMaxHeight() spans top→bottom so its tip = white trail bottom.
		Column(Modifier.fillMaxSize()) {
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
			// Flexible spacer — fills all remaining space between dots and blue trail
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
			// Small fixed gap between blue and white trail
			Spacer(Modifier.height(cfg.trailsInnerWeight.dp))
			// 4. long_square_white_trail — bottommost, pointer tip aligns here
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

		// Pointer spans full height of the box so its TIP (bottom) aligns with
		// the bottom of the box = bottom of the white trail column.
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
							offsetX = centerOf(seg)
							onAction(MainScreenAction.ChangeDecade(decades[seg]))
						}
					) { change, drag ->
						change.consume()
						offsetX = (offsetX + drag.x).coerceIn(0f, totalWidthPx)
						onAction(MainScreenAction.OnTunerChanged(offsetX / totalWidthPx))
					}
				},
		)
	}
}

//--------------------------------------------------
//  SongInfoSection
//--------------------------------------------------

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

//--------------------------------------------------
//  ProgressSection
//--------------------------------------------------

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

	androidx.compose.material3.Slider(
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
//  YouTube DisposableEffect
//--------------------------------------------------

@Composable
fun YoutubeListenerDisposableEffect(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	lifecycleOwner: LifecycleOwner,
	youTubePlayerView: YouTubePlayerView,
	youtubePlayerState: MutableState<YouTubePlayer?>,
) {
	val currentIsPlayingState = rememberUpdatedState(state.isPlaying)

	DisposableEffect(lifecycleOwner) {
		val listener = object : AbstractYouTubePlayerListener() {
			override fun onReady(youTubePlayer: YouTubePlayer) {
				youtubePlayerState.value = youTubePlayer
			}

			override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
				onAction(MainScreenAction.UpdateTotalDuration(duration.toInt()))
			}

			override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
				onAction(MainScreenAction.UpdatePlaybackTime(second.toInt()))
			}

			override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
				onAction(MainScreenAction.OnError("Player error: ${error.name}"))
			}

			override fun onStateChange(
				youTubePlayer: YouTubePlayer,
				state: PlayerConstants.PlayerState,
			) {
				val isPlayingInVM = currentIsPlayingState.value
				when (state) {
					PlayerConstants.PlayerState.PLAYING ->
						if (!isPlayingInVM) onAction(MainScreenAction.SetPlaying(true))

					PlayerConstants.PlayerState.PAUSED ->
						if (isPlayingInVM) onAction(MainScreenAction.SetPlaying(false))

					PlayerConstants.PlayerState.ENDED -> onAction(MainScreenAction.NextSong)
					else -> {}
				}
			}
		}

		youTubePlayerView.enableAutomaticInitialization = false
		val options = IFramePlayerOptions.Builder()
			.controls(0)
			.autoplay(0)
			.origin("https://com.cericatto.rockwooddial")
			.build()
		youTubePlayerView.initialize(listener, options)

		val lifecycleObserver = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> if (state.isPlaying) youtubePlayerState.value?.play()
				Lifecycle.Event.ON_PAUSE -> youtubePlayerState.value?.pause()
				else -> {}
			}
		}
		lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

		onDispose {
			youtubePlayerState.value = null
			youTubePlayerView.release()
			lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
		}
	}
}

//--------------------------------------------------
//  Loading / Error
//--------------------------------------------------

@Composable
private fun LoadingOverlay() {
	Box(
		Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.6f))
	) {
		CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonBlue)
	}
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
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
				Text(message, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
				TextButton(onClick = onDismiss) { Text("Next", color = NeonBlue) }
			}
		}
	}
}

//--------------------------------------------------
//  Helpers
//--------------------------------------------------

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

//--------------------------------------------------
//  Previews
//--------------------------------------------------

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
	name = "Tablet",
)
@Composable
fun MainScreenTabletPreview() {
	val state = MainScreenState(
		songs = listOf(Song("70", "1975", "Led Zeppelin", "Kashmir", "fJ9rUzIMcZQ")),
		isLoading = false, isPlaying = false, currentDecade = "70",
		totalDurationSeconds = 354, currentPlaybackTimeSeconds = 60,
	)
	Surface(color = Color.Black) { MainScreen(state = state, onAction = {}) }
}

@Preview(
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
	name = "Phone",
)
@Composable
fun MainScreenPhonePreview() {
	val state = MainScreenState(
		songs = listOf(Song("80", "1984", "AC/DC", "Hells Bells", "etAIpkdhU9Q")),
		isLoading = false, isPlaying = true, currentDecade = "80",
		totalDurationSeconds = 312, currentPlaybackTimeSeconds = 45,
	)
	Surface(color = Color.Black) { MainScreen(state = state, onAction = {}) }
}