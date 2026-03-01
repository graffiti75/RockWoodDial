package com.cericatto.rockwooddial.ui.main_screen

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.rockwooddial.R
import com.cericatto.rockwooddial.data.Song
import com.cericatto.rockwooddial.ui.common.ErrorBanner
import com.cericatto.rockwooddial.ui.common.KnobVolumeControl
import com.cericatto.rockwooddial.ui.common.LoadingOverlay
import com.cericatto.rockwooddial.ui.common.LockScreenOrientation
import com.cericatto.rockwooddial.ui.common.MainScreenBottom
import com.cericatto.rockwooddial.ui.common.MainScreenCenter
import com.cericatto.rockwooddial.ui.common.MainScreenToolbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

//--------------------------------------------------
//  LayoutConfig
//--------------------------------------------------

data class LayoutConfig(
	val wToolbar: Float,
	val wCenter: Float,
	val wBottom: Float,
	val wYoutubePlayer: Float,
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
	val fontSongTitleSp: Float,
	val sliderHeightDp: Float,
	val trailsSpacerDp: Float,
	val trailsInnerWeight: Float,
	val paddingBottomBetweenVuMetersAndYoutubePlayer: Float,
	val vuMeterHeightWeight: Float,
	val vuMeterNeedleLengthPercent: Float
)

val PHONE_CONFIG = LayoutConfig(
	wToolbar = 2f,
	wCenter = 8f,
	wBottom = 2f,
	wYoutubePlayer = 4f,
	wRadio = 6f,
	wDecades = 1f,
	wTrails = 1.2f,
	wSongInfo = 1.8f,
	seekbarWeight = 7f,
	spacerWeight = 0f,
	playBtnDp = 36f,
	knobDp = 104f,
	knobGapDp = 2f,
	pointerWidthDp = 19f,
	fontTitleSp = 49f,
	fontDecadeSp = 28f,
	fontSongSp = 21f,
	fontSongTitleSp = 23f,
	sliderHeightDp = 12f,
	trailsSpacerDp = 4f,
	trailsInnerWeight = 3f,
	paddingBottomBetweenVuMetersAndYoutubePlayer = 10f,
	vuMeterHeightWeight = 0.35f,
	vuMeterNeedleLengthPercent = 0.9f
)

val TABLET_CONFIG = LayoutConfig(
	wToolbar = 2f,
	wCenter = 8f,
	wBottom = 2f,
	wYoutubePlayer = 5f,
	wRadio = 6f,
	wDecades = 1f,
	wTrails = 1.2f,
	wSongInfo = 1.8f,
	seekbarWeight = 7f,
	spacerWeight = 0f,
	playBtnDp = 120f,
	knobDp = 175f,
	knobGapDp = 5f,
	pointerWidthDp = 22f,
	fontTitleSp = 77f,
	fontDecadeSp = 32f,
	fontSongSp = 40f,
	fontSongTitleSp = 44f,
	sliderHeightDp = 14f,
	trailsSpacerDp = 4f,
	trailsInnerWeight = 3f,
	paddingBottomBetweenVuMetersAndYoutubePlayer = 50f,
	vuMeterHeightWeight = 0.25f,
	vuMeterNeedleLengthPercent = 0.8f
)

//--------------------------------------------------
//  MainScreenRoot
//--------------------------------------------------

@Composable
fun MainScreenRoot(viewModel: MainScreenViewModel = hiltViewModel()) {
	LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	val state by viewModel.state.collectAsStateWithLifecycle()
	val configuration = LocalConfiguration.current
	val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
	val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
	val isTablet = (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) && isLandscape
	LaunchedEffect(Unit) {
		viewModel.checkIfIsTabletAndLandscape(isTablet)
	}
	MainScreen(
		state = state,
		onAction = viewModel::onAction
	)
}

//--------------------------------------------------
//  MainScreen
//--------------------------------------------------

@Composable
fun MainScreen(
	state: MainScreenState,
	onAction: (
		MainScreenAction
	) -> Unit
) {
	val context = LocalContext.current
	val youTubePlayerView = remember { YouTubePlayerView(context) }
	val youtubePlayerState = remember { mutableStateOf<YouTubePlayer?>(null) }
	val youtubePlayer = youtubePlayerState.value
	val lifecycleOwner = LocalLifecycleOwner.current
	val playerState = remember { mutableStateOf(PlayerConstants.PlayerState.UNSTARTED) }

	YoutubeListenerDisposableEffect(
		state = state,
		onAction = onAction,
		lifecycleOwner = lifecycleOwner,
		youTubePlayerView = youTubePlayerView,
		youtubePlayerState = youtubePlayerState,
		playerState = playerState
	)

	LaunchedEffect(youtubePlayer, state.currentSong) {
		youtubePlayer?.let { player ->
			state.currentSong?.let { song ->
				if (state.isPlaying) {
					player.loadVideo(song.youtubeId, 0f)
				} else {
					player.cueVideo(song.youtubeId,0f)
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

	val screenWidthDp = LocalConfiguration.current.screenWidthDp
	val screenLayout =
		LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
	val isTablet = screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE && screenWidthDp > 800
	val cfg = if (isTablet) TABLET_CONFIG else PHONE_CONFIG

	MainScreenContent(
		state = state,
		onAction = onAction,
		cfg = cfg,
		youTubePlayerView = youTubePlayerView,
		youtubePlayer = youtubePlayer,
		playerState = playerState.value
	)
}

//--------------------------------------------------
// MainScreenContent
//--------------------------------------------------

@Composable
private fun MainScreenContent(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	cfg: LayoutConfig,
	youTubePlayerView: YouTubePlayerView,
	youtubePlayer: YouTubePlayer?,
	playerState: PlayerConstants.PlayerState
) {
	Box(
		Modifier.fillMaxSize()
	) {
		Image(
			painter = painterResource(R.drawable.wood_background),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = Modifier.fillMaxSize(),
		)
		Column(
			Modifier
				.fillMaxSize()
				.padding(horizontal = 8.dp, vertical = 6.dp),
		) {
			MainScreenToolbar(
				cfg = cfg,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wToolbar),
			)
			MainScreenCenter(
				state = state,
				onAction = onAction,
				cfg = cfg,
				youTubePlayerView = youTubePlayerView,
				playerState = playerState,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wCenter),
			)
			MainScreenBottom(
				state = state,
				onAction = onAction,
				cfg = cfg,
				youtubePlayer = youtubePlayer,
				modifier = Modifier
					.fillMaxWidth()
					.weight(cfg.wBottom),
			)
		}
		// Knob — lives in the outer Box to share coordinate space with MainScreenBottom
		KnobVolumeControl(
			cfg = cfg,
			modifier = Modifier
				.size(cfg.knobDp.dp)
				.align(Alignment.BottomEnd)
				.padding(bottom = 8.dp, end = 8.dp),
		)
		/*
		Image(
			painter = painterResource(R.drawable.button_round_big),
			contentDescription = "Skip song",
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.size(cfg.knobDp.dp)
				.align(Alignment.BottomEnd)
				.padding(bottom = 10.dp, end = 10.dp)
				.clickable { onAction(MainScreenAction.NextSong) },
		)
		 */
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
//  YouTube DisposableEffect
//--------------------------------------------------

@Composable
fun YoutubeListenerDisposableEffect(
	state: MainScreenState,
	onAction: (MainScreenAction) -> Unit,
	lifecycleOwner: LifecycleOwner,
	youTubePlayerView: YouTubePlayerView,
	youtubePlayerState: MutableState<YouTubePlayer?>,
	playerState: MutableState<PlayerConstants.PlayerState>
) {
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
				playerState.value = state
				when (state) {
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
		currentDecade = "70",
		totalDurationSeconds = 354,
		currentPlaybackTimeSeconds = 60,
	)
	Surface(color = Color.Black) {
		MainScreen(
			state = state,
			onAction = {}
		)
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape",
	name = "Phone",
)
@Composable
fun MainScreenPhonePreview() {
	val state = MainScreenState(
		songs = listOf(
			Song(
				"70",
				"1972",
				"James Brown",
				"I Got Ants In My Pants (And I Want To Dance)",
				"fB-hpewwEiY"
			)
		),
		isLoading = false,
		isPlaying = true,
		currentDecade = "80",
		totalDurationSeconds = 312,
		currentPlaybackTimeSeconds = 45,
	)
	Surface(color = Color.Black) {
		MainScreen(
			state = state,
			onAction = {}
		)
	}
}