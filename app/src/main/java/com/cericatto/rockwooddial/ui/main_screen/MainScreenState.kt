package com.cericatto.rockwooddial.ui.main_screen

import com.cericatto.rockwooddial.data.Song

data class MainScreenState(
	val songs: List<Song> = listOf(Song()),
	val currentSongIndex: Int = 0,
	val isPlaying: Boolean = false,
	val currentPlaybackTimeSeconds: Int = 0,
	val totalDurationSeconds: Int = 0,
	val isLoading: Boolean = true,
	val error: String? = null,
	val isPrevButtonEnabled: Boolean = false,
	val currentDecade: String = "70",
	val tunerFraction: Float = 0.5f,      // 0f = leftmost, 1f = rightmost
	val isTabletAndLandscape: Boolean = false,
) {
	val currentSong: Song? get() = songs.getOrNull(currentSongIndex)
}
