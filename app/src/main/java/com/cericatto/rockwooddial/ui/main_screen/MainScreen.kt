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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
private val WoodBrown      = Color(0xFF6B3A2A)
private val WoodLight      = Color(0xFF8B5E3C)
private val SteelDark      = Color(0xFF2A2A2E)
private val SteelMid       = Color(0xFF3C3C42)
private val SteelLight     = Color(0xFF5C5C64)
private val SteelHighlight = Color(0xFF8C8C96)
private val PanelBlack     = Color(0xFF111114)
private val NeonBlue       = Color(0xFF00CFFF)
private val NeonBlueDim    = Color(0xFF004466)
private val TunerRed       = Color(0xFFFF2020)
private val DecadeSelected = Color(0xFFFFD700)

// ─────────────────────────────────────────────────────────
//  Root composable
// ─────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────
//  Stateless screen
// ─────────────────────────────────────────────────────────
@SuppressLint("UnusedBoxWithConstraintsScope")
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
				if (state.isPlaying) {
					player.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
					player.play()
				} else {
					player.cueVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
				}
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

	// Measure the full screen once — pass W and H down to every child
	BoxWithConstraints(
		Modifier
			.fillMaxSize()
			.drawBehind { drawWoodGrain() }
	) {
		val screenW = constraints.maxWidth.toFloat()
		val screenH = constraints.maxHeight.toFloat()

		Row(
			Modifier
				.fillMaxSize()
				.padding(
					horizontal = (screenW * 0.008f).dp,
					vertical   = (screenH * 0.015f).dp,
				),
			horizontalArrangement = Arrangement.spacedBy((screenW * 0.008f).dp),
		) {
			// Left 40%: YouTube player
			Box(
				Modifier
					.weight(0.40f)
					.fillMaxHeight()
					.clip(RoundedCornerShape(4.dp))
					.background(Color.Black)
					.border(2.dp, WoodBrown, RoundedCornerShape(4.dp))
			) {
				AndroidView(
					factory  = { youTubePlayerView },
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(16f / 9f)
						.align(Alignment.Center),
				)
				Box(
					Modifier
						.fillMaxWidth()
						.fillMaxHeight(0.10f)
						.align(Alignment.BottomCenter)
						.background(
							Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC111114)))
						)
				)
				SongInfoOverlay(state = state, screenW = screenW, screenH = screenH)
			}

			// Right 60%: controls
			Column(
				Modifier
					.weight(0.60f)
					.fillMaxHeight()
					.clip(RoundedCornerShape(4.dp))
					.drawBehind { drawSteelPanel() }
					.padding(
						horizontal = (screenW * 0.012f).dp,
						vertical   = (screenH * 0.015f).dp,
					),
				verticalArrangement = Arrangement.SpaceBetween,
			) {
				// Row 1 of 4: title bar — weight 1
				TopBar(screenW = screenW, screenH = screenH, modifier = Modifier.weight(1f))

				// Row 2 of 4: tuner (decade labels + strip + needle) — weight 2
				TunerStripSection(
					state    = state,
					onAction = onAction,
					screenW  = screenW,
					screenH  = screenH,
					modifier = Modifier.weight(2f),
				)

				// Row 3 of 4: progress — weight 2
				ProgressSection(
					state         = state,
					youtubePlayer = youtubePlayer,
					onAction      = onAction,
					screenW       = screenW,
					screenH       = screenH,
					modifier      = Modifier.weight(2f),
				)

				// Row 4 of 4: transport — weight 1
				TransportBar(
					state    = state,
					onAction = onAction,
					screenW  = screenW,
					screenH  = screenH,
					modifier = Modifier.weight(1f),
				)
			}
		}

		when {
			state.isLoading -> LoadingOverlay()
			state.error != null -> ErrorBanner(
				message   = state.error,
				onDismiss = { onAction(MainScreenAction.DismissError) },
			)
		}
	}
}

// ─────────────────────────────────────────────────────────
//  YouTube DisposableEffect
// ─────────────────────────────────────────────────────────
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
				playerState: PlayerConstants.PlayerState,
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
			.origin("https://com.cericatto.rockwooddial")
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

// ─────────────────────────────────────────────────────────
//  TopBar
// ─────────────────────────────────────────────────────────
@Composable
private fun TopBar(
	screenW: Float,
	screenH: Float,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier
			.fillMaxWidth()
			.drawBehind { drawSteelGradientVertical() }
			.padding(horizontal = (screenW * 0.008f).dp),
		verticalAlignment     = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(
			text       = "Rock Dial Dates",
			color      = NeonBlue,
			fontSize   = (screenH * 0.040f).sp,
			fontWeight = FontWeight.Bold,
			fontFamily = FontFamily.Cursive,
			fontStyle  = FontStyle.Italic,
		)
		Box(
			Modifier
				.size((screenH * 0.025f).dp)
				.clip(CircleShape)
				.background(Brush.radialGradient(listOf(NeonBlue, NeonBlueDim)))
		)
	}
}

// ─────────────────────────────────────────────────────────
//  TunerStripSection
//
//  Key design:
//  • BoxWithConstraints gives us totalWidthPx
//  • segWidthPx = totalWidthPx / 6  (same number used everywhere)
//  • centerOf(i) = segWidthPx * i + segWidthPx / 2
//  • Decade label slots each have fillMaxWidth(1f / remaining) = equal 1/6 width
//  • Needle drawn with drawBehind at EXACTLY offsetX on the Canvas
//    → no composable offset, no density conversion, pixel-perfect on every device
// ─────────────────────────────────────────────────────────
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun TunerStripSection(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	screenW: Float,
	screenH: Float,
	modifier: Modifier = Modifier,
) {
	val decades  = listOf("50", "60", "70", "80", "90", "2000")
	val numSegs  = decades.size

	BoxWithConstraints(modifier.fillMaxWidth()) {
		val totalWidthPx = constraints.maxWidth.toFloat()
		val segWidthPx   = totalWidthPx / numSegs

		// The ONE formula used for labels AND needle
		fun centerOf(i: Int): Float = segWidthPx * i + segWidthPx / 2f

		val activeIdx = decades.indexOf(state.currentDecade).coerceAtLeast(0)
		var offsetX by remember { mutableFloatStateOf(centerOf(activeIdx)) }

		// Snap needle whenever decade changes externally
		LaunchedEffect(state.currentDecade) {
			offsetX = centerOf(decades.indexOf(state.currentDecade).coerceAtLeast(0))
		}

		Column(Modifier.fillMaxSize()) {

			// ── Top half: decade label buttons ──
			// Each Box has fillMaxWidth(1f / remaining) which produces equal 1/6 slots
			Row(
				Modifier
					.fillMaxWidth()
					.weight(1f),
				verticalAlignment = Alignment.CenterVertically,
			) {
				decades.forEachIndexed { index, decade ->
					val selected = decade == state.currentDecade
					Box(
						Modifier
							.fillMaxWidth(1f / (numSegs - index).toFloat())
							.fillMaxHeight()
							.clickable { onAction(MainScreenAction.ChangeDecade(decade)) }
							.then(
								if (selected) Modifier.drawBehind {
									drawRect(DecadeSelected.copy(alpha = 0.15f))
								} else Modifier
							),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text       = if (decade == "2000") "2000s" else "${decade}s",
							color      = if (selected) DecadeSelected else SteelHighlight,
							fontSize   = (screenH * 0.030f).sp,
							fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
							textAlign  = TextAlign.Center,
						)
					}
				}
			}

			// ── Bottom half: strip + needle + number labels ──
			// Everything drawn in ONE drawBehind so offsetX is used directly in px
			Box(
				Modifier
					.fillMaxWidth()
					.weight(1f)
					.drawBehind {

						// 1. Strip background + tick marks
						drawTunerStrip(offsetX / totalWidthPx)

						// 2. Needle drawn AT EXACTLY offsetX — same coordinate space
						//    needle width = 1.5% of strip width, scales on any screen
						val nW = size.width * 0.015f

						// Arrow tip at top of strip, stem bottom lands on the blue line (45% height)
						val tipY    = size.height * 0.02f
						val baseY   = size.height * 0.30f
						val lineEndY = size.height * 0.50f // exactly on the blue line

						val arrowPath = Path().apply {
							moveTo(offsetX, tipY)
							lineTo(offsetX - nW, baseY)
							lineTo(offsetX + nW, baseY)
							close()
						}
						drawPath(arrowPath, color = TunerRed)
						drawLine(
							color       = TunerRed,
							start       = Offset(offsetX, baseY),
							end         = Offset(offsetX, lineEndY),
							strokeWidth = nW * 0.5f,
						)
					}
					.pointerInput(Unit) {
						detectDragGestures(
							onDragEnd = {
								val seg = (offsetX / segWidthPx)
									.toInt()
									.coerceIn(0, numSegs - 1)
								offsetX = centerOf(seg)
								onAction(MainScreenAction.ChangeDecade(decades[seg]))
							}
						) { change, drag ->
							change.consume()
							offsetX = (offsetX + drag.x).coerceIn(0f, totalWidthPx)
							onAction(MainScreenAction.OnTunerChanged(offsetX / totalWidthPx))
						}
					}
			) {
				// Number labels at bottom of strip — same equal slots
				Row(
					Modifier
						.fillMaxWidth()
						.align(Alignment.BottomCenter)
						.padding(bottom = 2.dp),
				) {
					decades.forEachIndexed { index, dec ->
						Box(
							Modifier.fillMaxWidth(1f / (numSegs - index).toFloat()),
							contentAlignment = Alignment.Center,
						) {
							Text(
								text      = if (dec == "2000") "2000" else dec,
								color     = if (dec == state.currentDecade) DecadeSelected
								else SteelHighlight.copy(alpha = 0.6f),
								fontSize  = (screenH * 0.022f).sp,
								textAlign = TextAlign.Center,
							)
						}
					}
				}
			}
		}
	}
}

// ─────────────────────────────────────────────────────────
//  ProgressSection
// ─────────────────────────────────────────────────────────
@Composable
private fun ProgressSection(
	state: MainScreenState,
	youtubePlayer: YouTubePlayer?,
	onAction: (MainScreenAction) -> Unit,
	screenW: Float,
	screenH: Float,
	modifier: Modifier = Modifier,
) {
	var sliderPos by remember(state.currentPlaybackTimeSeconds) {
		mutableFloatStateOf(state.currentPlaybackTimeSeconds.toFloat())
	}
	val totalFloat = state.totalDurationSeconds.toFloat().takeIf { it > 0f } ?: 100f

	Column(
		modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.Center,
	) {
		Box(
			Modifier
				.fillMaxWidth()
				.weight(1f)
				.clip(RoundedCornerShape(4.dp))
				.border(1.dp, SteelHighlight, RoundedCornerShape(4.dp))
				.background(PanelBlack)
		) {
			val pct = (sliderPos / totalFloat).coerceIn(0f, 1f)
			Box(
				Modifier
					.fillMaxHeight()
					.fillMaxWidth(pct)
					.background(Brush.horizontalGradient(listOf(NeonBlueDim, NeonBlue)))
			)
		}

		Slider(
			value    = sliderPos,
			onValueChange = { sliderPos = it },
			onValueChangeFinished = {
				youtubePlayer?.seekTo(sliderPos)
				onAction(MainScreenAction.SeekTo(sliderPos.toInt()))
			},
			valueRange = 0f..totalFloat,
			modifier   = Modifier
				.fillMaxWidth()
				.weight(1f),
			enabled = state.currentSong != null && state.totalDurationSeconds > 0,
			colors  = SliderDefaults.colors(
				thumbColor         = NeonBlue,
				activeTrackColor   = Color.Transparent,
				inactiveTrackColor = Color.Transparent,
			),
		)

		Row(
			Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(formatTime(sliderPos.toInt()), color = SteelHighlight, fontSize = (screenH * 0.022f).sp)
			Text(formatTime(state.totalDurationSeconds), color = SteelHighlight, fontSize = (screenH * 0.022f).sp)
		}
	}
}

// ─────────────────────────────────────────────────────────
//  TransportBar
// ─────────────────────────────────────────────────────────
@Composable
private fun TransportBar(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	screenW: Float,
	screenH: Float,
	modifier: Modifier = Modifier,
) {
	val buttonSize = (screenH * 0.10f).dp
	val iconSize   = (screenH * 0.06f).dp

	Row(
		modifier
			.fillMaxWidth()
			.drawBehind { drawSteelGradientVertical() },
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment     = Alignment.CenterVertically,
	) {
		IconButton(
			onClick  = { onAction(MainScreenAction.PreviousSong) },
			enabled  = state.isPrevButtonEnabled,
			modifier = Modifier.size(buttonSize),
		) {
			Icon(Icons.Default.SkipPrevious, contentDescription = "Previous",
				tint = SteelHighlight, modifier = Modifier.size(iconSize))
		}

		Box(
			Modifier
				.size(buttonSize)
				.clip(CircleShape)
				.background(
					Brush.radialGradient(
						listOf(
							if (state.isPlaying) NeonBlue.copy(alpha = 0.3f) else NeonBlueDim,
							PanelBlack,
						)
					)
				)
				.border(2.dp, if (state.isPlaying) NeonBlue else SteelMid, CircleShape)
				.clickable(enabled = state.currentSong != null) {
					onAction(MainScreenAction.PlayPause)
				},
			contentAlignment = Alignment.Center,
		) {
			Icon(
				imageVector        = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
				contentDescription = if (state.isPlaying) "Pause" else "Play",
				tint               = if (state.isPlaying) NeonBlue else SteelHighlight,
				modifier           = Modifier.size(iconSize),
			)
		}

		IconButton(
			onClick  = { onAction(MainScreenAction.NextSong) },
			enabled  = state.songs.size > 1,
			modifier = Modifier.size(buttonSize),
		) {
			Icon(Icons.Default.SkipNext, contentDescription = "Next",
				tint = SteelHighlight, modifier = Modifier.size(iconSize))
		}
	}
}

// ─────────────────────────────────────────────────────────
//  SongInfoOverlay
// ─────────────────────────────────────────────────────────
@Composable
private fun BoxScope.SongInfoOverlay(
	state: MainScreenState,
	screenW: Float,
	screenH: Float,
) {
	val song = state.currentSong ?: return
	Column(
		Modifier
			.align(Alignment.BottomStart)
			.fillMaxWidth()
			.background(Color(0xCC000000))
			.padding(
				horizontal = (screenW * 0.008f).dp,
				vertical   = (screenH * 0.008f).dp,
			),
	) {
		Text(
			text       = song.band,
			color      = NeonBlue,
			fontSize   = (screenH * 0.032f).sp,
			fontWeight = FontWeight.Bold,
			maxLines   = 1,
			overflow   = TextOverflow.Ellipsis,
		)
		Text(
			text     = "${song.songTitle}  •  ${song.year}",
			color    = SteelHighlight,
			fontSize = (screenH * 0.024f).sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

// ─────────────────────────────────────────────────────────
//  Loading / Error
// ─────────────────────────────────────────────────────────
@Composable
private fun LoadingOverlay() {
	Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) {
		CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NeonBlue)
	}
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
	val offsetY by animateDpAsState(targetValue = 0.dp, label = "error_slide")
	Box(
		Modifier.fillMaxSize().padding(16.dp).offset(y = offsetY),
		contentAlignment = Alignment.BottomCenter,
	) {
		Surface(
			color    = Color(0xFF8B0000),
			shape    = RoundedCornerShape(8.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			Row(
				Modifier.padding(12.dp),
				verticalAlignment     = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(message, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
				TextButton(onClick = onDismiss) { Text("Next", color = NeonBlue) }
			}
		}
	}
}

// ─────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────
//  Canvas helpers
// ─────────────────────────────────────────────────────────
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
	for (i in 0..6) {
		val y = i * (size.height / 7f)
		drawLine(SteelHighlight.copy(alpha = 0.05f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
	}
}

private fun DrawScope.drawSteelGradientVertical() {
	drawRect(Brush.verticalGradient(listOf(SteelLight, SteelDark)))
}

private fun DrawScope.drawTunerStrip(fraction: Float) {
	drawRect(PanelBlack)
	drawRect(
		brush   = Brush.horizontalGradient(listOf(NeonBlueDim, NeonBlue.copy(alpha = 0.4f), NeonBlueDim)),
		topLeft = Offset(0f, size.height * 0.45f),
		size    = Size(size.width, size.height * 0.10f),
	)
	val tickCount = 60
	for (i in 0..tickCount) {
		val x         = size.width * i / tickCount.toFloat()
		val isMajor   = i % 10 == 0
		val proximity = 1f - (abs(x / size.width - fraction) * 5f).coerceIn(0f, 1f)
		drawLine(
			color       = NeonBlue.copy(alpha = 0.3f + proximity * 0.7f),
			start       = Offset(x, if (isMajor) size.height * 0.20f else size.height * 0.35f),
			end         = Offset(x, size.height * 0.50f),
			strokeWidth = if (isMajor) 2f else 1f,
		)
	}
}

// ─────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────
@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape", name = "Tablet")
@Composable
fun MainScreenTabletPreview() {
	val state = MainScreenState(
		songs = listOf(Song("70", "1975", "Queen", "Bohemian Rhapsody", "fJ9rUzIMcZQ")),
		isLoading = false, isPlaying = false, currentDecade = "70",
		totalDurationSeconds = 354, currentPlaybackTimeSeconds = 60,
	)
	Surface(color = Color.Black) { MainScreen(state = state, onAction = {}) }
}

@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape", name = "Phone")
@Composable
fun MainScreenPhonePreview() {
	val state = MainScreenState(
		songs = listOf(Song("80", "1984", "AC/DC", "Hells Bells", "etAIpkdhU9Q")),
		isLoading = false, isPlaying = true, currentDecade = "80",
		totalDurationSeconds = 312, currentPlaybackTimeSeconds = 45,
	)
	Surface(color = Color.Black) { MainScreen(state = state, onAction = {}) }
}
