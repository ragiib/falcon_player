package com.example.falconplayer.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.falconplayer.MainActivity
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.ui.player.components.PlayerControls
import com.example.falconplayer.ui.player.components.PlayerSettingsSheet
import com.example.falconplayer.ui.player.components.TrackSelectionSheet
import com.example.falconplayer.ui.player.components.VideoInfoDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    initialVideoUri: android.net.Uri? = null,
    initialTitle: String? = null,
    initialPlaylistId: String? = null,
    initialPlaylistIndex: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val trackSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var brightnessLevel by remember { mutableFloatStateOf(0.5f) }
    var volumeLevel by remember { mutableFloatStateOf(0.5f) }
    var showBrightnessHud by remember { mutableStateOf(false) }
    var showVolumeHud by remember { mutableStateOf(false) }
    var hudJob by remember { mutableStateOf<Job?>(null) }

    val handleBack = {
        viewModel.stopPlayback()
        onBackClick()
    }

    // Intercept back button when sheets or dialogs are visible
    BackHandler {
        if (uiState.showInfoDialog) {
            viewModel.toggleInfoDialog()
        } else if (uiState.showTrackSheet) {
            viewModel.closeTrackSheet()
        } else if (uiState.showSettingsSheet) {
            viewModel.toggleSettingsSheet()
        } else {
            handleBack()
        }
    }

    LaunchedEffect(initialVideoUri, initialPlaylistId, initialPlaylistIndex) {
        if (initialPlaylistId != null) {
            viewModel.loadPlaylistQueue(initialPlaylistId, initialPlaylistIndex)
        } else if (initialVideoUri != null) {
            viewModel.loadVideo(initialVideoUri, initialTitle ?: "Video")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: Media3 Player Surface with dynamic aspect-ratio resize
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    useController = false
                    resizeMode = uiState.resizeMode.mode
                    subtitleView?.setUserDefaultStyle()
                    subtitleView?.setFractionalTextSize(0.053f)
                }
            },
            update = { playerView ->
                playerView.player = viewModel.player
                if (playerView.resizeMode != uiState.resizeMode.mode) {
                    playerView.resizeMode = uiState.resizeMode.mode
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator Layer
        if (uiState.playbackState == PlaybackState.Buffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = FalconRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Error Indicator Layer
        (uiState.playbackState as? PlaybackState.Error)?.let { errorState ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, FalconRed.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = FalconRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Playback Error",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorState.message,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Layer 2: Gesture Interceptor (Vertical Swipe: Brightness/Volume, Double Tap: Seek, Tap: Controls)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val screenWidth = size.width
                            val activity = context as? Activity
                            if (offset.x < screenWidth * 0.45f) {
                                showBrightnessHud = true
                                showVolumeHud = false
                                val curB = activity?.window?.attributes?.screenBrightness ?: 0.5f
                                brightnessLevel = if (curB < 0f) 0.5f else curB
                            } else if (offset.x > screenWidth * 0.55f) {
                                showVolumeHud = true
                                showBrightnessHud = false
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                volumeLevel = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount / (size.height * 0.65f)
                            val activity = context as? Activity
                            if (change.position.x < size.width * 0.45f && activity != null) {
                                brightnessLevel = (brightnessLevel + delta).coerceIn(0.01f, 1.0f)
                                activity.window.attributes = activity.window.attributes.apply {
                                    screenBrightness = brightnessLevel
                                }
                            } else if (change.position.x > size.width * 0.55f) {
                                volumeLevel = (volumeLevel + delta).coerceIn(0f, 1f)
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val targetVol = (volumeLevel * maxVol).toInt()
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                            }
                        },
                        onDragEnd = {
                            hudJob?.cancel()
                            hudJob = coroutineScope.launch {
                                delay(800L)
                                showBrightnessHud = false
                                showVolumeHud = false
                            }
                        },
                        onDragCancel = {
                            showBrightnessHud = false
                            showVolumeHud = false
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.45f) {
                                viewModel.triggerDoubleTapSeek(isForward = false)
                            } else if (offset.x > screenWidth * 0.55f) {
                                viewModel.triggerDoubleTapSeek(isForward = true)
                            } else {
                                viewModel.onPlayPauseClick()
                            }
                        },
                        onTap = {
                            viewModel.onControlsScreenTap()
                        }
                    )
                }
        )

        // Brightness Gesture Floating HUD (Left Center)
        AnimatedVisibility(
            visible = showBrightnessHud,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(brightnessLevel * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { brightnessLevel },
                        color = FalconRed,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                    )
                }
            }
        }

        // Volume Gesture Floating HUD (Right Center)
        AnimatedVisibility(
            visible = showVolumeHud,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (volumeLevel <= 0.01f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(volumeLevel * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { volumeLevel },
                        color = FalconRed,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                    )
                }
            }
        }

        // Double-Tap Seek Visual Feedback
        uiState.seekFeedback?.let { feedback ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                contentAlignment = if (feedback.isForward) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (feedback.isForward) Icons.Filled.FastForward else Icons.Filled.FastRewind,
                            contentDescription = null,
                            tint = FalconRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (feedback.isForward) "+" else "-"}${feedback.amountSeconds}s",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Floating Aspect Ratio Mode Toast Notification
        AnimatedVisibility(
            visible = uiState.resizeModeMessage != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AspectRatio,
                        contentDescription = null,
                        tint = FalconRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.resizeModeMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Layer 3: Controls Overlay
        // Single playback speed control is prominently housed in Bottom Controls (no duplicate speed button).
        PlayerControls(
            uiState = uiState,
            onBackClick = handleBack,
            onAudioClick = { viewModel.openTrackSheet(TrackTab.AUDIO) },
            onSubtitlesClick = { viewModel.openTrackSheet(TrackTab.SUBTITLE) },
            onAspectRatioClick = viewModel::toggleResizeMode,
            onPipClick = {
                val activity = context as? Activity
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                    val params = PictureInPictureParams.Builder().build()
                    activity.enterPictureInPictureMode(params)
                }
            },
            onSpeedClick = viewModel::toggleSettingsSheet,
            onPlayPauseClick = viewModel::onPlayPauseClick,
            onPreviousClick = viewModel::onPreviousClick,
            onNextClick = viewModel::onNextClick,
            onRewindClick = viewModel::onRewindClick,
            onForwardClick = viewModel::onForwardClick,
            onSeek = viewModel::onSeek,
            onLockToggle = viewModel::onLockToggle,
            onFullscreenToggle = {
                val activity = context as? MainActivity
                if (activity != null) {
                    val isLandscape = activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    activity.requestedOrientation = if (isLandscape) {
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                }
            }
        )

        // Layer 4: Modern Audio & Subtitle Track Selection Sheet
        if (uiState.showTrackSheet) {
            TrackSelectionSheet(
                sheetState = trackSheetState,
                initialTab = uiState.trackSheetTab,
                audioTracks = uiState.audioTracks,
                selectedAudioTrackId = uiState.selectedAudioTrackId,
                onAudioTrackSelect = { track ->
                    viewModel.selectAudioTrack(track)
                    viewModel.closeTrackSheet()
                },
                subtitleTracks = uiState.subtitleTracks,
                selectedSubtitleTrackId = uiState.selectedSubtitleTrackId,
                onSubtitleTrackSelect = { track ->
                    viewModel.selectSubtitleTrack(track)
                    viewModel.closeTrackSheet()
                },
                onDisableSubtitles = {
                    viewModel.disableSubtitles()
                    viewModel.closeTrackSheet()
                },
                onDismissRequest = viewModel::closeTrackSheet
            )
        }

        // Layer 5: Playback Settings (Speed & Media Details) Sheet
        if (uiState.showSettingsSheet) {
            PlayerSettingsSheet(
                sheetState = settingsSheetState,
                onDismissRequest = viewModel::toggleSettingsSheet,
                currentSpeed = uiState.playbackSpeed,
                onSpeedSelect = { speed ->
                    viewModel.setPlaybackSpeed(speed)
                    viewModel.toggleSettingsSheet()
                },
                onOpenMediaInfo = {
                    viewModel.toggleSettingsSheet()
                    viewModel.toggleInfoDialog()
                }
            )
        }

        // Layer 6: Media Information Dialog
        if (uiState.showInfoDialog) {
            VideoInfoDialog(
                uiState = uiState,
                onDismissRequest = viewModel::toggleInfoDialog
            )
        }
    }
}
