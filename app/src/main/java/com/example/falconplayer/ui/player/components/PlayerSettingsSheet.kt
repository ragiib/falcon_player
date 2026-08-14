package com.example.falconplayer.ui.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.RadioButtonDefaults
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    currentSpeed: Float,
    onSpeedSelect: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = FalconSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Playback Speed",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
            speeds.forEach { speed ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSpeedSelect(speed) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSpeed == speed,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = FalconRed)
                    )
                    Text(
                        text = if (speed == 1.0f) "Normal" else "${speed}x",
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp)) // Padding for bottom nav
        }
    }
}

