package com.example.falconplayer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.falconplayer.ui.player.components.PlayerControls
import com.example.falconplayer.ui.player.components.PlayerSettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: Video Surface (Placeholder for now)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder icon for video
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_play),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxSize(0.3f)
            )
        }

        // Layer 2: Gesture Interceptors
        // This is a placeholder for actual gesture handling (brightness, volume, double tap)
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
            onOverflowClick = { viewModel.toggleSettingsSheet() },
            onPlayPauseClick = viewModel::onPlayPauseClick,
            onPreviousClick = { /* TODO */ viewModel.onUserActivity() },
            onNextClick = { /* TODO */ viewModel.onUserActivity() },
            onRewindClick = { /* TODO */ viewModel.onUserActivity() },
            onForwardClick = { /* TODO */ viewModel.onUserActivity() },
            onSeek = { /* TODO */ viewModel.onUserActivity() },
            onLockToggle = viewModel::onLockToggle,
            onSpeedClick = { viewModel.toggleSettingsSheet() },
            onSubtitleClick = { viewModel.toggleSettingsSheet() },
            onAudioTrackClick = { viewModel.toggleSettingsSheet() },
            onFullscreenToggle = viewModel::toggleFullscreen
        )

        // Layer 4: Bottom Sheet for Settings
        if (uiState.showSettingsSheet) {
            PlayerSettingsSheet(
                sheetState = sheetState,
                onDismissRequest = viewModel::toggleSettingsSheet
            )
        }
    }
}
