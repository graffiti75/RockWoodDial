package com.cericatto.rockwooddial.ui.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cericatto.rockwooddial.data.SongParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
	private val songParser: SongParser,
	private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

	private val _state = MutableStateFlow(MainScreenState())
	val state: StateFlow<MainScreenState> = _state.asStateFlow()

	init {
		loadSongs("70")
	}

	fun checkIfIsTabletAndLandscape(value: Boolean) {
		_state.update { it.copy(isTabletAndLandscape = value) }
	}

	fun loadSongs(decade: String) {
		viewModelScope.launch(dispatcher) {
			try {
				val songs = songParser.parseSongs(decade).shuffled()
				if (songs.isNotEmpty()) {
					_state.update {
						it.copy(
							songs = songs,
							isLoading = false,
							currentSongIndex = 0,
							currentPlaybackTimeSeconds = 0,
							totalDurationSeconds = 0,
							error = null,
							isPlaying = true,  // auto-play on decade change
						)
					}
				} else {
					_state.update {
						it.copy(
							isLoading = false,
							error = "No songs found for the ${decade}s."
						)
					}
				}
			} catch (e: Exception) {
				_state.update {
					it.copy(
						isLoading = false,
						error = "Error loading songs: ${e.message}"
					)
				}
			}
		}
	}

	fun onAction(action: MainScreenAction) {
		when (action) {
			is MainScreenAction.PlayPause -> togglePlayPause()
			is MainScreenAction.SetPlaying -> setPlaying(action.playing)
			is MainScreenAction.NextSong -> onNextSong()
			is MainScreenAction.PreviousSong -> onPreviousSong()
			is MainScreenAction.SeekTo -> seekTo(action.positionSeconds)
			is MainScreenAction.UpdatePlaybackTime -> updatePlaybackTime(action.timeSeconds)
			is MainScreenAction.UpdateTotalDuration -> updateTotalDuration(action.durationSeconds)
			is MainScreenAction.OnError -> onError(action.error)
			is MainScreenAction.DismissError -> goToNextSong()
			is MainScreenAction.ChangeDecade -> changeDecade(action.decade)
			is MainScreenAction.OnTunerChanged -> onTunerChanged(action.fraction)
		}
	}

	private fun togglePlayPause() {
		if (_state.value.songs.isEmpty()) return
		_state.update { it.copy(isPlaying = !it.isPlaying) }
	}

	private fun setPlaying(playing: Boolean) {
		if (_state.value.songs.isEmpty()) return
		if (_state.value.isPlaying != playing)
			_state.update { it.copy(isPlaying = playing) }
	}

	private fun onNextSong() {
		if (_state.value.songs.isEmpty()) return
		goToNextSong()
	}

	private fun onPreviousSong() {
		if (_state.value.songs.isEmpty()) return
		goToPreviousSong()
	}

	private fun seekTo(positionSeconds: Int) {
		_state.update { it.copy(currentPlaybackTimeSeconds = positionSeconds) }
	}

	private fun updatePlaybackTime(timeSeconds: Int) {
		_state.update { it.copy(currentPlaybackTimeSeconds = timeSeconds) }
	}

	private fun updateTotalDuration(durationSeconds: Int) {
		_state.update { it.copy(totalDurationSeconds = durationSeconds) }
	}

	private fun onError(error: String) {
//		_state.update {
//			it.copy(
//				error = error,
//				isLoading = false,
//				isPlaying = false
//			)
//		}
		goToNextSong()
	}

	private fun changeDecade(decade: String) {
		_state.update { it.copy(currentDecade = decade, isPrevButtonEnabled = false) }
		loadSongs(decade)
	}

	private fun onTunerChanged(fraction: Float) {
		val songs = _state.value.songs
		if (songs.isEmpty()) return
		val newIndex = (fraction * songs.size).toInt().coerceIn(0, songs.size - 1)
		if (newIndex != _state.value.currentSongIndex) {
			_state.update {
				it.copy(
					tunerFraction = fraction,
					currentSongIndex = newIndex,
					currentPlaybackTimeSeconds = 0,
					totalDurationSeconds = 0,
					isPlaying = true,
				)
			}
		} else {
			_state.update { it.copy(tunerFraction = fraction) }
		}
	}

	private fun goToNextSong() {
		_state.update { s ->
			s.copy(
				currentSongIndex = (s.currentSongIndex + 1) % s.songs.size,
//				currentPlaybackTimeSeconds = 0,
//				totalDurationSeconds = 0,
				isPlaying = true,
				error = null,
				isPrevButtonEnabled = true,
			)
		}
	}

	private fun goToPreviousSong() {
		_state.update { s ->
			val prev = (s.currentSongIndex - 1 + s.songs.size) % s.songs.size
			s.copy(
				currentSongIndex = prev,
//				currentPlaybackTimeSeconds = 0,
//				totalDurationSeconds = 0,
				isPlaying = true,
				isPrevButtonEnabled = true,
			)
		}
	}
}