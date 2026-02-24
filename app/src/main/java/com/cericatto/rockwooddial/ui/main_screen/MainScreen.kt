package com.cericatto.rockwooddial.ui.main_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.abs

// ─────────────────────────────────────────────────────────
//  Colour tokens
// ─────────────────────────────────────────────────────────
private val WoodBrown = Color(0xFF6B3A2A)
private val WoodLight = Color(0xFF8B5E3C)
private val SteelDark = Color(0xFF2A2A2E)
private val SteelMid = Color(0xFF3C3C42)
private val SteelLight = Color(0xFF5C5C64)
private val SteelHighlight = Color(0xFF8C8C96)
private val PanelBlack = Color(0xFF111114)
private val NeonBlue = Color(0xFF00CFFF)
private val NeonBlueDim = Color(0xFF004466)
private val TunerRed = Color(0xFFFF2020)
private val TunerRedLight = Color(0xFFFF6060)
private val DecadeSelected = Color(0xFFFFD700)

//--------------------------------------------------
//  Root composable — injects ViewModel
//--------------------------------------------------

@Composable
fun MainScreenRoot(viewModel: MainScreenViewModel = hiltViewModel()) {
	LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	val state by viewModel.state.collectAsStateWithLifecycle()

	val configuration = LocalConfiguration.current
	val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
	val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
	val isTablet = (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) && isLandscape
	LaunchedEffect(Unit) { viewModel.checkIfIsTabletAndLandscape(isTablet) }

	MainScreen(
		state = state,
		onAction = viewModel::onAction
	)
}

//--------------------------------------------------
//  Stateless screen — receives state + callbacks
//--------------------------------------------------

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

	// Load / cue video when current song changes
	LaunchedEffect(youtubePlayer, state.currentSong) {
		youtubePlayer?.let { player ->
			state.currentSong?.let { song ->
				if (state.isPlaying) {
					player.loadVideo(
						song.youtubeId,
						state.currentPlaybackTimeSeconds.toFloat()
					)
					player.play()
				} else {
					player.cueVideo(
						song.youtubeId,
						state.currentPlaybackTimeSeconds.toFloat()
					)
				}
			}
		}
	}

	// Sync play/pause with ViewModel state
	LaunchedEffect(youtubePlayer, state.isPlaying) {
		youtubePlayer?.let { player ->
			if (state.currentSong != null) {
				if (state.isPlaying) player.play() else player.pause()
			}
		}
	}

	// ── Main layout: wood frame + steel panel side by side ──
	Box(
		Modifier
			.fillMaxSize()
			.drawBehind { drawWoodGrain() }
	) {
		Row(
			Modifier
				.fillMaxSize()
				.padding(12.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
		) {
			// Left: YouTube player (hidden behind a retro frame)
			Box(
				Modifier
					.weight(0.40f)
					.fillMaxHeight()
					.clip(RoundedCornerShape(6.dp))
					.background(Color.Black)
					.border(
						3.dp,
						WoodBrown,
						RoundedCornerShape(6.dp)
					)
			) {
				AndroidView(
					factory = { youTubePlayerView },
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(16f / 9f)
						.align(Alignment.Center),
				)
				// Retro speaker grille overlay at bottom
				Box(
					Modifier
						.fillMaxWidth()
						.height(40.dp)
						.align(Alignment.BottomCenter)
						.background(
							Brush.verticalGradient(
								listOf(Color.Transparent, Color(0xCC111114))
							)
						)
				)
				// Song info overlay
				SongInfoOverlay(state = state)
			}

			// Right: controls panel
			Column(
				Modifier
					.weight(0.60f)
					.fillMaxHeight()
					.clip(RoundedCornerShape(6.dp))
					.drawBehind { drawSteelPanel() }
					.padding(horizontal = 14.dp, vertical = 10.dp),
				verticalArrangement = Arrangement.SpaceBetween,
			) {
				// Top bar
				TopBar()

				// Decade selector + neon indicators
				DecadeSelectorRow(state = state, onAction = onAction)

				// Tuner strip
				TunerStripSection(state = state, onAction = onAction)

				// Progress bar
				ProgressSection(state = state, youtubePlayer = youtubePlayer, onAction = onAction)

				// Transport bar
				TransportBar(state = state, onAction = onAction)
			}
		}

		// Loading / error overlay
		when {
			state.isLoading -> LoadingOverlay()
			state.error != null -> ErrorBanner(
				message = state.error,
				onDismiss = { onAction(MainScreenAction.DismissError) })
		}
	}
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
				playerState: PlayerConstants.PlayerState
			) {
				val isPlayingInVM = currentIsPlayingState.value
				when (playerState) {
					PlayerConstants.PlayerState.PLAYING -> {
						if (!isPlayingInVM) onAction(MainScreenAction.SetPlaying(true))
					}

					PlayerConstants.PlayerState.PAUSED -> {
						val isNewSong = state.currentPlaybackTimeSeconds < 2
						if (isPlayingInVM && !isNewSong) onAction(MainScreenAction.SetPlaying(false))
					}

					PlayerConstants.PlayerState.ENDED -> onAction(MainScreenAction.NextSong)
					else -> {}
				}
			}
		}

		youTubePlayerView.enableAutomaticInitialization = false
		val options = IFramePlayerOptions.Builder()
			.controls(0)
			.autoplay(0)
			.origin("https://com.cericatto.rockwooddial")  // ← this line
			.build()

		youTubePlayerView.initialize(listener, options)

		val lifecycleObserver = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> {
					if (state.isPlaying) youtubePlayerState.value?.play()
				}
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
//  UI Sub-Composables
//--------------------------------------------------

@Composable
private fun TopBar() {
	Row(
		Modifier
			.fillMaxWidth()
			.drawBehind { drawSteelGradientVertical() }
			.padding(horizontal = 8.dp, vertical = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(
			text = "Rock Dial Dates",
			color = NeonBlue,
			fontSize = 20.sp,
			fontWeight = FontWeight.Bold,
			fontFamily = FontFamily.Cursive,
			fontStyle = FontStyle.Italic,
		)
		// Neon power dot
		Box(
			Modifier
				.size(12.dp)
				.clip(CircleShape)
				.background(
					Brush.radialGradient(listOf(NeonBlue, NeonBlueDim))
				)
		)
	}
}

@Composable
private fun DecadeSelectorRow(state: MainScreenState, onAction: (MainScreenAction) -> Unit) {
	val decades = listOf("50", "60", "70", "80", "90", "2000")
	Row(
		Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically,
	) {
		decades.forEach { decade ->
			val selected = decade == state.currentDecade
			Text(
				text = if (decade == "2000") "2000s" else "${decade}s",
				color = if (selected) DecadeSelected else SteelHighlight,
				fontSize = if (selected) 14.sp else 12.sp,
				fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
				modifier = Modifier
					.clickable { onAction(MainScreenAction.ChangeDecade(decade)) }
					.padding(horizontal = 6.dp, vertical = 4.dp)
					.then(
						if (selected) Modifier.drawBehind {
							drawRect(DecadeSelected.copy(alpha = 0.15f))
						} else Modifier
					),
			)
		}
	}
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun TunerStripSection(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit
) {
	val decades = listOf("50", "60", "70", "80", "90", "2000")
	val numSegs = decades.size
	val density = androidx.compose.ui.platform.LocalDensity.current
	val halfNeedlePx = with(density) { 7.dp.toPx() }

	BoxWithConstraints(
		Modifier
			.fillMaxWidth()
			.height(80.dp)
	) {
		val totalWidthPx = constraints.maxWidth.toFloat()
		val segWidthPx = totalWidthPx / numSegs

		fun centerOf(i: Int) = segWidthPx * i + segWidthPx / 2f

		var offsetX by remember {
			mutableFloatStateOf(
				centerOf(
					decades.indexOf(state.currentDecade).coerceAtLeast(0)
				)
			)
		}

		LaunchedEffect(state.currentDecade) {
			offsetX = centerOf(decades.indexOf(state.currentDecade).coerceAtLeast(0))
		}

		// Canvas tuner strip
		Box(
			Modifier
				.fillMaxSize()
				.drawBehind { drawTunerStrip(offsetX / totalWidthPx) }
		)

		// Draggable needle icon
		Icon(
			painter = painterResource(id = R.drawable.ic_red_pencil_two),
			contentDescription = "Tuner needle",
			tint = TunerRed,
			modifier = Modifier
				.padding(vertical = 4.dp)
				.size(width = 14.dp, height = 60.dp)
				.offset { IntOffset((offsetX - halfNeedlePx).toInt(), 0) }
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
						offsetX =
							(offsetX + drag.x).coerceIn(halfNeedlePx, totalWidthPx - halfNeedlePx)
						onAction(MainScreenAction.OnTunerChanged(offsetX / totalWidthPx))
					}
				}
		)

		// Decade labels along strip
		Row(
			Modifier.fillMaxSize(),
			horizontalArrangement = Arrangement.Start
		) {
			decades.forEach { dec ->
				Text(
					text = if (dec == "2000") "2000" else dec,
					color = if (dec == state.currentDecade) DecadeSelected else SteelHighlight.copy(
						alpha = 0.6f
					),
					fontSize = 9.sp,
					modifier = Modifier
						.weight(1f)
						.align(Alignment.Bottom)
						.padding(bottom = 2.dp),
				)
			}
		}
	}
}

@Composable
private fun ProgressSection(
	state: MainScreenState,
	youtubePlayer: YouTubePlayer?,
	onAction: (MainScreenAction) -> Unit,
) {
	var sliderPos by remember(state.currentPlaybackTimeSeconds) {
		mutableFloatStateOf(state.currentPlaybackTimeSeconds.toFloat())
	}
	val totalFloat = state.totalDurationSeconds.toFloat().takeIf { it > 0f } ?: 100f

	Column(
		Modifier.fillMaxWidth()
	) {
		// Thin progress bar styled retro
		Box(
			Modifier
				.fillMaxWidth()
				.height(18.dp)
				.clip(RoundedCornerShape(4.dp))
				.border(
					1.dp,
					SteelHighlight,
					RoundedCornerShape(4.dp)
				)
				.background(PanelBlack)
		) {
			// Fill
			val pct = (sliderPos / totalFloat).coerceIn(0f, 1f)
			Box(
				Modifier
					.fillMaxHeight()
					.fillMaxWidth(pct)
					.background(
						Brush.horizontalGradient(
							listOf(NeonBlueDim, NeonBlue)
						)
					)
			)
		}

		Spacer(Modifier.height(2.dp))

		// Standard Slider for interactivity (transparent, overlaid)
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
				.height(20.dp)
				.offset(y = (-20).dp),
			enabled = state.currentSong != null && state.totalDurationSeconds > 0,
			colors = SliderDefaults.colors(
				thumbColor = NeonBlue,
				activeTrackColor = Color.Transparent,
				inactiveTrackColor = Color.Transparent,
			),
		)

		Row(
			Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				formatTime(sliderPos.toInt()),
				color = SteelHighlight,
				fontSize = 9.sp
			)
			Text(
				formatTime(state.totalDurationSeconds),
				color = SteelHighlight,
				fontSize = 9.sp
			)
		}
	}
}

@Composable
private fun TransportBar(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit
) {
	Row(
		Modifier
			.fillMaxWidth()
			.drawBehind { drawSteelGradientVertical() }
			.padding(horizontal = 8.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Prev
		IconButton(
			onClick = { onAction(MainScreenAction.PreviousSong) },
			enabled = state.isPrevButtonEnabled,
		) {
			Icon(
				Icons.Default.SkipPrevious,
				contentDescription = "Previous",
				tint = SteelHighlight
			)
		}

		// Play / Pause — big neon-glowing button
		Box(
			Modifier
				.size(48.dp)
				.clip(CircleShape)
				.background(
					Brush.radialGradient(
						listOf(
							if (state.isPlaying) NeonBlue.copy(alpha = 0.3f) else NeonBlueDim,
							PanelBlack
						)
					)
				)
				.border(
					2.dp,
					if (state.isPlaying) NeonBlue else SteelMid, CircleShape
				)
				.clickable(enabled = state.currentSong != null) {
					onAction(MainScreenAction.PlayPause)
				},
			contentAlignment = Alignment.Center,
		) {
			Icon(
				imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
				contentDescription = if (state.isPlaying) "Pause" else "Play",
				tint = if (state.isPlaying) NeonBlue else SteelHighlight,
				modifier = Modifier.size(28.dp),
			)
		}

		// Next
		IconButton(
			onClick = { onAction(MainScreenAction.NextSong) },
			enabled = state.songs.size > 1
		) {
			Icon(
				Icons.Default.SkipNext,
				contentDescription = "Next",
				tint = SteelHighlight
			)
		}
	}
}

@Composable
private fun BoxScope.SongInfoOverlay(
	state: MainScreenState
) {
	val song = state.currentSong ?: return
	Column(
		Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.background(Color(0xCC000000))
			.padding(horizontal = 8.dp, vertical = 6.dp),
	) {
		Text(
			text = song.band,
			color = NeonBlue,
			fontSize = 13.sp,
			fontWeight = FontWeight.Bold,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
		Text(
			text = "${song.songTitle}  •  ${song.year}",
			color = SteelHighlight,
			fontSize = 10.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

@Composable
private fun LoadingOverlay() {
	Box(
		Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.6f))
	) {
		CircularProgressIndicator(
			modifier = Modifier.align(Alignment.Center),
			color = NeonBlue,
		)
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

//--------------------------------------------------
//  Helpers
//--------------------------------------------------

fun formatTime(totalSeconds: Int): String {
	val m = totalSeconds / 60
	val s = totalSeconds % 60
	return "%d:%02d".format(m, s)
}

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
//  Canvas drawing helpers
//--------------------------------------------------

private fun DrawScope.drawWoodGrain() {
	drawRect(Brush.linearGradient(listOf(WoodBrown, WoodLight, WoodBrown, WoodLight, WoodBrown)))
	val grainPaint = Paint().apply { color = Color(0x22000000) }
	val canvas = drawContext.canvas
	for (i in 0..40) {
		val y = i * (size.height / 40f)
		canvas.drawLine(Offset(0f, y), Offset(size.width, y + 8f), grainPaint)
	}
}

private fun DrawScope.drawSteelPanel() {
	drawRect(Brush.verticalGradient(listOf(SteelMid, SteelDark, SteelMid, PanelBlack)))
	// subtle horizontal highlights
	for (i in 0..6) {
		val y = i * (size.height / 7f)
		drawLine(
			SteelHighlight.copy(alpha = 0.05f),
			Offset(0f, y),
			Offset(size.width, y),
			strokeWidth = 1f
		)
	}
}

private fun DrawScope.drawSteelGradientVertical() {
	drawRect(Brush.verticalGradient(listOf(SteelLight, SteelDark)))
}

private fun DrawScope.drawTunerStrip(fraction: Float) {
	// Background track
	drawRect(PanelBlack)
	drawRect(
		Brush.horizontalGradient(listOf(NeonBlueDim, NeonBlue.copy(alpha = 0.4f), NeonBlueDim)),
		topLeft = Offset(0f, size.height * 0.45f),
		size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.1f)
	)

	// Tick marks
	val tickCount = 60
	for (i in 0..tickCount) {
		val x = size.width * i / tickCount.toFloat()
		val isMajor = i % 10 == 0
		val proximity = 1f - (abs(x / size.width - fraction) * 5f).coerceIn(0f, 1f)
		drawLine(
			color = NeonBlue.copy(alpha = 0.3f + proximity * 0.7f),
			start = Offset(x, if (isMajor) size.height * 0.2f else size.height * 0.35f),
			end = Offset(x, size.height * 0.5f),
			strokeWidth = if (isMajor) 2f else 1f,
		)
	}
}

//--------------------------------------------------
//  Previews
//--------------------------------------------------

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape"
)
@Composable
fun MainScreenPreview() {
	val state = MainScreenState(
		songs = listOf(
			Song(
				"70",
				"1975",
				"Queen",
				"Bohemian Rhapsody",
				"fJ9rUzIMcZQ"
			)
		),
		isLoading = false,
		isPlaying = false,
		currentDecade = "70",
		totalDurationSeconds = 354,
		currentPlaybackTimeSeconds = 60,
	)
	Surface(color = Color.Black) {
		MainScreen(state = state, onAction = {})
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
	name = "Loading"
)
@Composable
fun MainScreenLoadingPreview() {
	Surface(color = Color.Black) {
		MainScreen(state = MainScreenState(isLoading = true), onAction = {})
	}
}
