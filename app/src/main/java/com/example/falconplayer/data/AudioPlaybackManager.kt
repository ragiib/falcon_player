package com.example.falconplayer.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class AudioRepeatMode {
    OFF,
    ALL,
    ONE
}

data class AudioPlaybackState(
    val currentTrack: AudioItem? = null,
    val queue: List<AudioItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: AudioRepeatMode = AudioRepeatMode.OFF,
    val speed: Float = 1.0f
)

@OptIn(UnstableApi::class)
@Singleton
class AudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        setAudioAttributes(audioAttributes, true) // Automatically handle audio focus
        setHandleAudioBecomingNoisy(true) // Pause automatically if headphones disconnect
    }

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var originalQueue: List<AudioItem> = emptyList()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }
        })
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _playbackState.update {
                    it.copy(
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
        _playbackState.update {
            it.copy(
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L)
            )
        }
    }

    private fun handleTrackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            AudioRepeatMode.ONE -> {
                player.seekTo(0)
                player.play()
            }
            AudioRepeatMode.ALL -> {
                playNext()
            }
            AudioRepeatMode.OFF -> {
                if (state.currentIndex < state.queue.size - 1) {
                    playNext()
                } else {
                    player.seekTo(0)
                    player.pause()
                }
            }
        }
    }

    fun playTrack(track: AudioItem, queue: List<AudioItem> = listOf(track)) {
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playQueue(queue, index)
    }

    fun playQueue(tracks: List<AudioItem>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        originalQueue = tracks
        val safeIndex = startIndex.coerceIn(0, tracks.size - 1)

        val activeQueue = if (_playbackState.value.isShuffle) {
            val selected = tracks[safeIndex]
            listOf(selected) + tracks.filter { it.id != selected.id }.shuffled()
        } else {
            tracks
        }
        val targetIndex = if (_playbackState.value.isShuffle) 0 else safeIndex
        val targetTrack = activeQueue[targetIndex]

        _playbackState.update {
            it.copy(
                currentTrack = targetTrack,
                queue = activeQueue,
                currentIndex = targetIndex,
                currentPositionMs = 0L,
                durationMs = targetTrack.durationMs
            )
        }

        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(targetTrack.contentUri))
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun playNext() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return
        val nextIndex = if (state.currentIndex + 1 < state.queue.size) {
            state.currentIndex + 1
        } else if (state.repeatMode == AudioRepeatMode.ALL) {
            0
        } else {
            return
        }

        val nextTrack = state.queue[nextIndex]
        _playbackState.update {
            it.copy(
                currentTrack = nextTrack,
                currentIndex = nextIndex,
                currentPositionMs = 0L,
                durationMs = nextTrack.durationMs
            )
        }

        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(nextTrack.contentUri))
        player.prepare()
        player.play()
    }

    fun playPrevious() {
        val state = _playbackState.value
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
            return
        }
        if (state.queue.isEmpty()) return
        val prevIndex = if (state.currentIndex > 0) {
            state.currentIndex - 1
        } else if (state.repeatMode == AudioRepeatMode.ALL) {
            state.queue.size - 1
        } else {
            0
        }

        val prevTrack = state.queue[prevIndex]
        _playbackState.update {
            it.copy(
                currentTrack = prevTrack,
                currentIndex = prevIndex,
                currentPositionMs = 0L,
                durationMs = prevTrack.durationMs
            )
        }

        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(prevTrack.contentUri))
        player.prepare()
        player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        val state = _playbackState.value
        val currentTrack = state.currentTrack

        val newQueue = if (newShuffle) {
            if (currentTrack != null) {
                listOf(currentTrack) + originalQueue.filter { it.id != currentTrack.id }.shuffled()
            } else {
                originalQueue.shuffled()
            }
        } else {
            originalQueue
        }
        val newIndex = if (currentTrack != null) {
            newQueue.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
        } else 0

        _playbackState.update {
            it.copy(
                isShuffle = newShuffle,
                queue = newQueue,
                currentIndex = newIndex
            )
        }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            AudioRepeatMode.OFF -> AudioRepeatMode.ALL
            AudioRepeatMode.ALL -> AudioRepeatMode.ONE
            AudioRepeatMode.ONE -> AudioRepeatMode.OFF
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    fun toggleRepeat() = toggleRepeatMode()

    fun next() = playNext()

    fun previous() = playPrevious()

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _playbackState.update { it.copy(speed = speed) }
    }

    fun setPlaybackSpeed(speed: Float) = setSpeed(speed)

    fun playQueueIndex(index: Int) {
        val currentQueue = _playbackState.value.queue
        if (index in currentQueue.indices) {
            val targetTrack = currentQueue[index]
            _playbackState.update {
                it.copy(
                    currentTrack = targetTrack,
                    currentIndex = index,
                    currentPositionMs = 0L,
                    durationMs = targetTrack.durationMs
                )
            }
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(targetTrack.contentUri))
            player.prepare()
            player.play()
        }
    }

    fun stop() {
        player.stop()
        stopProgressTracker()
        _playbackState.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                currentPositionMs = 0L
            )
        }
    }
}
