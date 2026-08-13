package com.example.falconplayer.ui.player

import android.content.pm.ActivityInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.example.falconplayer.MainActivity
import com.example.falconplayer.ui.player.components.PlayerControls
import com.example.falconplayer.ui.player.components.PlayerSettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.loadVideo(it)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    useController = false // We use our own Compose UI
                }
            },
            update = { playerView ->
                playerView.player = viewModel.player
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator Layer
        if (uiState.playbackState == PlaybackState.Buffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
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
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFE53935),
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

        // Layer 2: Gesture Interceptors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = viewModel::onControlsScreenTap
                )
        )

        // Layer 3: Controls Overlay
        PlayerControls(
            uiState = uiState,
            onBackClick = onBackClick,
            onOverflowClick = { videoPickerLauncher.launch(arrayOf("video/*")) }, // Reusing overflow as open file
            onPlayPauseClick = viewModel::onPlayPauseClick,
            onPreviousClick = { /* No-op or skip backward */ },
            onNextClick = { /* No-op or skip forward */ },
            onRewindClick = viewModel::onRewindClick,
            onForwardClick = viewModel::onForwardClick,
            onSeek = viewModel::onSeek,
            onLockToggle = viewModel::onLockToggle,
            onSpeedClick = viewModel::toggleSettingsSheet,
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

        // Layer 4: Bottom Sheet for Settings
        if (uiState.showSettingsSheet) {
            PlayerSettingsSheet(
                sheetState = sheetState,
                onDismissRequest = viewModel::toggleSettingsSheet,
                currentSpeed = viewModel.player.playbackParameters.speed,
                onSpeedSelect = { speed ->
                    viewModel.setPlaybackSpeed(speed)
                    viewModel.toggleSettingsSheet()
                }
            )
        }
    }
}
