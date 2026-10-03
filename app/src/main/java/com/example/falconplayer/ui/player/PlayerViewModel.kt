package com.example.falconplayer.ui.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.falconplayer.data.HistoryRepository
import com.example.falconplayer.data.PlaybackPositionRepository
import com.example.falconplayer.data.PlaylistRepository
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.data.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val positionRepository: PlaybackPositionRepository,
    private val playlistRepository: PlaylistRepository,
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private var hideControlsJob: Job? = null
    private var progressJob: Job? = null
    private var seekFeedbackJob: Job? = null
    private var resizeMessageJob: Job? = null
    private var currentUri: String? = null

    private var playlistQueue: List<VideoItem> = emptyList()
    private var currentQueueIndex: Int = 0

    init {
        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
                if (playbackState == Player.STATE_ENDED) {
                    currentUri?.let { uri ->
                        viewModelScope.launch {
                            positionRepository.savePosition(uri, 0L)
                        }
                    }
                    if (playlistQueue.isNotEmpty() && currentQueueIndex < playlistQueue.size - 1) {
                        currentQueueIndex++
                        val nextVideo = playlistQueue[currentQueueIndex]
                        loadVideo(nextVideo.contentUri, nextVideo.title)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startProgressTicker()
                    startHideControlsTimer()
                } else {
                    stopProgressTicker()
                    cancelHideControlsTimer()
                    showControls()
                    saveCurrentPosition()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                extractTracks(tracks)
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                _uiState.update {
                    it.copy(videoWidth = videoSize.width, videoHeight = videoSize.height)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = error.message.takeIf { !it.isNullOrBlank() }
                    ?: "Playback failed. The video format may be unsupported or unreadable."
                _uiState.update { it.copy(playbackState = PlaybackState.Error(errorMsg)) }
            }
        })
    }

    private fun extractTracks(tracks: Tracks) {
        val audioList = mutableListOf<TrackItem>()
        val subtitleList = mutableListOf<TrackItem>()
        var selectedAudioId: String? = null
        var selectedSubtitleId: String? = null

        var audioCount = 1
        var subtitleCount = 1

        val isTextDisabled = player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)

        for (groupIndex in 0 until tracks.groups.size) {
            val group = tracks.groups[groupIndex]
            val trackType = group.type

            if (trackType == C.TRACK_TYPE_AUDIO) {
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val isSelected = group.isTrackSelected(trackIndex)
                    val isSupported = group.isTrackSupported(trackIndex)
                    val trackId = "audio_${groupIndex}_${trackIndex}"

                    val displayName = formatAudioTrackName(format, audioCount)
                    val technicalDetails = formatAudioTechnicalInfo(format)

                    val item = TrackItem(
                        id = trackId,
                        group = group.mediaTrackGroup,
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        name = displayName,
                        language = format.language,
                        label = format.label,
                        technicalDetails = technicalDetails,
                        isSelected = isSelected,
                        isSupported = isSupported
                    )
                    audioList.add(item)
                    if (isSelected) {
                        selectedAudioId = trackId
                    }
                    audioCount++
                }
            } else if (trackType == C.TRACK_TYPE_TEXT) {
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val isSelected = !isTextDisabled && group.isTrackSelected(trackIndex)
                    val isSupported = group.isTrackSupported(trackIndex)
                    val trackId = "sub_${groupIndex}_${trackIndex}"

                    val displayName = formatSubtitleTrackName(format, subtitleCount)
                    val technicalDetails = formatSubtitleTechnicalInfo(format)

                    val item = TrackItem(
                        id = trackId,
                        group = group.mediaTrackGroup,
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        name = displayName,
                        language = format.language,
                        label = format.label,
                        technicalDetails = technicalDetails,
                        isSelected = isSelected,
                        isSupported = isSupported
                    )
                    subtitleList.add(item)
                    if (isSelected) {
                        selectedSubtitleId = trackId
                    }
                    subtitleCount++
                }
            }
        }

        _uiState.update { state ->
            state.copy(
                audioTracks = audioList,
                selectedAudioTrackId = selectedAudioId ?: state.selectedAudioTrackId,
                subtitleTracks = subtitleList,
                selectedSubtitleTrackId = if (isTextDisabled) null else (selectedSubtitleId ?: state.selectedSubtitleTrackId)
            )
        }
    }

    private fun formatLanguage(languageCode: String?): String? {
        if (languageCode.isNullOrBlank() || languageCode.equals("und", ignoreCase = true)) {
            return null
        }
        return try {
            val locale = Locale.forLanguageTag(languageCode)
            val name = locale.getDisplayLanguage(Locale.getDefault())
            if (name.isNotBlank()) name else Locale(languageCode).displayLanguage.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            languageCode.uppercase()
        }
    }

    private fun formatAudioTrackName(format: Format, index: Int): String {
        val lang = formatLanguage(format.language)
        val label = format.label?.takeIf { it.isNotBlank() }
        return when {
            lang != null && label != null -> "$lang ($label)"
            lang != null -> lang
            label != null -> label
            else -> "Audio Track $index"
        }
    }

    private fun formatAudioTechnicalInfo(format: Format): String {
        val parts = mutableListOf<String>()

        val mime = format.sampleMimeType
        val codec = when {
            mime == null -> null
            mime.contains("mp4a-latm", true) || mime.contains("aac", true) -> "AAC"
            mime.contains("eac3-joc", true) -> "Dolby Atmos"
            mime.contains("eac3", true) -> "E-AC3"
            mime.contains("ac3", true) -> "AC3"
            mime.contains("opus", true) -> "Opus"
            mime.contains("flac", true) -> "FLAC"
            mime.contains("vorbis", true) -> "Vorbis"
            mime.contains("mpeg", true) || mime.contains("mp3", true) -> "MP3"
            mime.contains("dts.hd", true) -> "DTS-HD"
            mime.contains("dts", true) -> "DTS"
            mime.contains("true-hd", true) -> "TrueHD"
            else -> mime.substringAfterLast("/")
        }
        if (codec != null) parts.add(codec)

        when (format.channelCount) {
            1 -> parts.add("Mono (1.0)")
            2 -> parts.add("Stereo (2.0)")
            6 -> parts.add("5.1 Surround")
            8 -> parts.add("7.1 Surround")
            in 3..Int.MAX_VALUE -> parts.add("${format.channelCount} Channels")
        }

        if (format.sampleRate > 0) {
            val khz = format.sampleRate / 1000f
            parts.add(if (khz % 1f == 0f) "${khz.toInt()} kHz" else "%.1f kHz".format(khz))
        }

        return parts.joinToString(" • ")
    }

    private fun formatSubtitleTrackName(format: Format, index: Int): String {
        val lang = formatLanguage(format.language)
        val label = format.label?.takeIf { it.isNotBlank() }
        return when {
            lang != null && label != null -> "$lang ($label)"
            lang != null -> lang
            label != null -> label
            else -> "Subtitle Track $index"
        }
    }

    private fun formatSubtitleTechnicalInfo(format: Format): String {
        val parts = mutableListOf<String>()
        val mime = format.sampleMimeType
        val formatName = when {
            mime == null -> null
            mime.contains("vtt", true) -> "WebVTT"
            mime.contains("subrip", true) || mime.contains("srt", true) -> "SRT"
            mime.contains("ssa", true) || mime.contains("ass", true) -> "ASS/SSA"
            mime.contains("pgs", true) -> "PGS"
            mime.contains("dvbsub", true) -> "DVB"
            mime.contains("tx3g", true) -> "TX3G"
            else -> mime.substringAfterLast("/")
        }
        if (formatName != null) parts.add(formatName)

        if (format.selectionFlags and C.SELECTION_FLAG_FORCED != 0) {
            parts.add("Forced")
        }
        if (format.roleFlags and C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND != 0 ||
            format.label?.contains("sdh", ignoreCase = true) == true) {
            parts.add("SDH")
        }
        return parts.joinToString(" • ")
    }

    fun selectAudioTrack(track: TrackItem) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .setOverrideForType(
                TrackSelectionOverride(track.group, listOf(track.trackIndex))
            )
            .build()

        _uiState.update { state ->
            val updated = state.audioTracks.map {
                it.copy(isSelected = it.id == track.id)
            }
            state.copy(
                audioTracks = updated,
                selectedAudioTrackId = track.id
            )
        }
        onUserActivity()
    }

    fun selectSubtitleTrack(track: TrackItem) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setOverrideForType(
                TrackSelectionOverride(track.group, listOf(track.trackIndex))
            )
            .build()

        _uiState.update { state ->
            val updated = state.subtitleTracks.map {
                it.copy(isSelected = it.id == track.id)
            }
            state.copy(
                subtitleTracks = updated,
                selectedSubtitleTrackId = track.id
            )
        }
        onUserActivity()
    }

    fun disableSubtitles() {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .build()

        _uiState.update { state ->
            val updated = state.subtitleTracks.map {
                it.copy(isSelected = false)
            }
            state.copy(
                subtitleTracks = updated,
                selectedSubtitleTrackId = null
            )
        }
        onUserActivity()
    }

    fun openTrackSheet(tab: TrackTab) {
        _uiState.update {
            it.copy(
                showTrackSheet = true,
                trackSheetTab = tab
            )
        }
        cancelHideControlsTimer()
    }

    fun closeTrackSheet() {
        _uiState.update { it.copy(showTrackSheet = false) }
        if (player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleResizeMode() {
        val nextMode = when (_uiState.value.resizeMode) {
            VideoResizeMode.FIT -> VideoResizeMode.ZOOM
            VideoResizeMode.ZOOM -> VideoResizeMode.STRETCH
            VideoResizeMode.STRETCH -> VideoResizeMode.FIT
        }
        _uiState.update {
            it.copy(
                resizeMode = nextMode,
                resizeModeMessage = nextMode.displayName
            )
        }
        resizeMessageJob?.cancel()
        resizeMessageJob = viewModelScope.launch {
            delay(1500L)
            _uiState.update { it.copy(resizeModeMessage = null) }
        }
        onUserActivity()
    }

    private fun updatePlaybackState() {
        val playerError = player.playerError
        if (playerError != null) {
            val errorMsg = playerError.message.takeIf { !it.isNullOrBlank() }
                ?: "Playback failed. The video format may be unsupported or unreadable."
            _uiState.update { it.copy(playbackState = PlaybackState.Error(errorMsg)) }
            return
        }
        val newState = when (player.playbackState) {
            Player.STATE_IDLE -> PlaybackState.Idle
            Player.STATE_BUFFERING -> PlaybackState.Buffering
            Player.STATE_READY -> if (player.isPlaying) PlaybackState.Playing else PlaybackState.Paused
            Player.STATE_ENDED -> PlaybackState.Ended
            else -> PlaybackState.Idle
        }
        _uiState.update { it.copy(playbackState = newState) }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                updateMediaInfo()
                delay(200L)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        _uiState.update { state ->
            state.copy(
                mediaInfo = state.mediaInfo.copy(
                    currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = player.duration.coerceAtLeast(0L),
                    bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                    isLive = player.isCurrentMediaItemLive
                )
            )
        }
    }

    fun loadPlaylistQueue(playlistId: String, startIndex: Int = 0) {
        viewModelScope.launch {
            val playlist = playlistRepository.playlists.first().find { it.id == playlistId } ?: return@launch
            val allVideos = videoRepository.getVideos()
            val videoMap = allVideos.associateBy { it.contentUri.toString() }
            val orderedVideos = playlist.videoUris.mapNotNull { uriStr ->
                videoMap[uriStr] ?: allVideos.find { it.contentUri.toString() == uriStr }
            }
            if (orderedVideos.isNotEmpty()) {
                playlistQueue = orderedVideos
                currentQueueIndex = startIndex.coerceIn(0, orderedVideos.size - 1)
                val targetVideo = orderedVideos[currentQueueIndex]
                loadVideo(targetVideo.contentUri, targetVideo.title)
            } else {
                _uiState.update { it.copy(playbackState = PlaybackState.Error("No playable videos found in playlist")) }
            }
        }
    }

    fun loadVideo(uri: Uri, title: String = uri.lastPathSegment ?: "Unknown Video") {
        val uriString = uri.toString()
        currentUri = uriString

        _uiState.update {
            it.copy(
                mediaInfo = it.mediaInfo.copy(
                    title = title,
                    currentPositionMs = 0L,
                    durationMs = 0L,
                    bufferedPositionMs = 0L
                ),
                playbackState = PlaybackState.Buffering,
                audioTracks = emptyList(),
                selectedAudioTrackId = null,
                subtitleTracks = emptyList(),
                selectedSubtitleTrackId = null
            )
        }

        viewModelScope.launch {
            historyRepository.recordVideoPlayed(uriString)
            val savedPosition = positionRepository.getSavedPosition(uriString).first()
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (savedPosition > 3000L) {
                player.seekTo(savedPosition)
            } else {
                player.seekTo(0L)
            }
            player.play()
        }
    }

    private fun saveCurrentPosition() {
        currentUri?.let { uri ->
            val pos = player.currentPosition
            val dur = player.duration
            if (dur > 0 && pos < dur - 5000L) {
                viewModelScope.launch {
                    positionRepository.savePosition(uri, pos)
                }
            }
        }
    }

    fun onPlayPauseClick() {
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.play()
        } else if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        onUserActivity()
    }

    fun onNextClick() {
        if (playlistQueue.isNotEmpty() && currentQueueIndex < playlistQueue.size - 1) {
            currentQueueIndex++
            val nextVideo = playlistQueue[currentQueueIndex]
            loadVideo(nextVideo.contentUri, nextVideo.title)
        } else {
            onForwardClick()
        }
    }

    fun onPreviousClick() {
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (playlistQueue.isNotEmpty() && currentQueueIndex > 0) {
            currentQueueIndex--
            val prevVideo = playlistQueue[currentQueueIndex]
            loadVideo(prevVideo.contentUri, prevVideo.title)
        } else {
            player.seekTo(0L)
        }
    }

    fun onSeek(positionMs: Long) {
        player.seekTo(positionMs)
        updateMediaInfo()
        onUserActivity()
    }

    fun onRewindClick() {
        val target = (player.currentPosition - 10000L).coerceAtLeast(0L)
        player.seekTo(target)
        updateMediaInfo()
        onUserActivity()
    }

    fun onForwardClick() {
        val target = (player.currentPosition + 10000L).coerceAtMost(player.duration.coerceAtLeast(0L))
        player.seekTo(target)
        updateMediaInfo()
        onUserActivity()
    }

    fun triggerDoubleTapSeek(isForward: Boolean) {
        if (isForward) {
            onForwardClick()
        } else {
            onRewindClick()
        }
        _uiState.update {
            it.copy(
                seekFeedback = SeekFeedback(
                    isForward = isForward,
                    amountSeconds = 10,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        seekFeedbackJob?.cancel()
        seekFeedbackJob = viewModelScope.launch {
            delay(650L)
            _uiState.update { it.copy(seekFeedback = null) }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
        onUserActivity()
    }

    fun stopPlayback() {
        saveCurrentPosition()
        if (player.isPlaying) {
            player.pause()
        }
        player.stop()
        player.clearMediaItems()
        stopProgressTicker()
        cancelHideControlsTimer()
        _uiState.update { it.copy(playbackState = PlaybackState.Idle) }
    }

    fun onControlsScreenTap() {
        when (_uiState.value.controlsState) {
            ControlsState.Visible -> hideControls()
            ControlsState.Hidden -> {
                showControls()
                if (player.isPlaying) {
                    startHideControlsTimer()
                }
            }
            ControlsState.Locked -> {
                showControls()
                startHideControlsTimer()
            }
        }
    }

    fun onLockToggle() {
        _uiState.update {
            val newControlsState = if (it.controlsState == ControlsState.Locked) {
                ControlsState.Visible
            } else {
                ControlsState.Locked
            }
            it.copy(controlsState = newControlsState)
        }
        startHideControlsTimer()
    }

    fun onUserActivity() {
        if (_uiState.value.controlsState == ControlsState.Visible && player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleSettingsSheet() {
        val willShow = !_uiState.value.showSettingsSheet
        _uiState.update { it.copy(showSettingsSheet = willShow) }
        if (willShow) {
            cancelHideControlsTimer()
        } else if (player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleInfoDialog() {
        val willShow = !_uiState.value.showInfoDialog
        _uiState.update { it.copy(showInfoDialog = willShow) }
        if (willShow) {
            cancelHideControlsTimer()
        } else if (player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    private fun showControls() {
        if (_uiState.value.controlsState != ControlsState.Locked) {
            _uiState.update { it.copy(controlsState = ControlsState.Visible) }
        }
    }

    private fun hideControls() {
        if (!_uiState.value.showSettingsSheet && !_uiState.value.showTrackSheet) {
            _uiState.update {
                it.copy(
                    controlsState = if (it.controlsState == ControlsState.Locked) ControlsState.Locked else ControlsState.Hidden
                )
            }
        }
    }

    private fun startHideControlsTimer() {
        cancelHideControlsTimer()
        hideControlsJob = viewModelScope.launch {
            delay(3000L)
            hideControls()
        }
    }

    private fun cancelHideControlsTimer() {
        hideControlsJob?.cancel()
        hideControlsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentPosition()
        player.release()
    }
}
