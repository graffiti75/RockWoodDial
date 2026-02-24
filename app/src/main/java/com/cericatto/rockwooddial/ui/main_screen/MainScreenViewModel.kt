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
			_state.update { it.copy(isLoading = true) }
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
						)
					}
				} else {
					_state.update {
						it.copy(isLoading = false, error = "No songs found for the ${decade}s.")
					}
				}
			} catch (e: Exception) {
				_state.update {
					it.copy(isLoading = false, error = "Error loading songs: ${e.message}")
				}
			}
		}
	}

	fun onAction(action: MainScreenAction) {
		when (action) {
			is MainScreenAction.PlayPause -> {
				if (_state.value.songs.isEmpty()) return
				_state.update { it.copy(isPlaying = !it.isPlaying) }
			}
			is MainScreenAction.SetPlaying -> {
				if (_state.value.songs.isEmpty()) return
				if (_state.value.isPlaying != action.playing)
					_state.update { it.copy(isPlaying = action.playing) }
			}
			is MainScreenAction.NextSong -> {
				if (_state.value.songs.isEmpty()) return
				goToNextSong()
			}
			is MainScreenAction.PreviousSong -> {
				if (_state.value.songs.isEmpty()) return
				goToPreviousSong()
			}
			is MainScreenAction.SeekTo -> {
				_state.update { it.copy(currentPlaybackTimeSeconds = action.positionSeconds) }
			}
			is MainScreenAction.UpdatePlaybackTime -> {
				_state.update { it.copy(currentPlaybackTimeSeconds = action.timeSeconds) }
			}
			is MainScreenAction.UpdateTotalDuration -> {
				_state.update { it.copy(totalDurationSeconds = action.durationSeconds) }
			}
			is MainScreenAction.OnError -> {
				_state.update {
					it.copy(
						error = action.error,
						isLoading = false,
						isPlaying = false
					)
				}
			}
			is MainScreenAction.DismissError -> goToNextSong()
			is MainScreenAction.ChangeDecade -> {
				_state.update {
					it.copy(
						currentDecade = action.decade,
						isPrevButtonEnabled = false
					)
				}
				loadSongs(action.decade)
			}
			is MainScreenAction.OnTunerChanged -> {
				_state.update { it.copy(tunerFraction = action.fraction) }
			}
		}
	}

	private fun goToNextSong() {
		_state.update { s ->
			s.copy(
				currentSongIndex = (s.currentSongIndex + 1) % s.songs.size,
				currentPlaybackTimeSeconds = 0,
				totalDurationSeconds = 0,
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
				currentPlaybackTimeSeconds = 0,
				totalDurationSeconds = 0,
				isPlaying = true,
				isPrevButtonEnabled = true,
			)
		}
	}
}
