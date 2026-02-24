package com.cericatto.rockwooddial.ui.main_screen

sealed interface MainScreenAction {
	data object PlayPause : MainScreenAction
	data object NextSong : MainScreenAction
	data object PreviousSong : MainScreenAction
	data class SeekTo(val positionSeconds: Int) : MainScreenAction
	data class UpdatePlaybackTime(val timeSeconds: Int) : MainScreenAction
	data class UpdateTotalDuration(val durationSeconds: Int) : MainScreenAction
	data class OnError(val error: String) : MainScreenAction
	data object DismissError : MainScreenAction
	data class SetPlaying(val playing: Boolean) : MainScreenAction
	data class ChangeDecade(val decade: String) : MainScreenAction
	data class OnTunerChanged(val fraction: Float) : MainScreenAction
}
