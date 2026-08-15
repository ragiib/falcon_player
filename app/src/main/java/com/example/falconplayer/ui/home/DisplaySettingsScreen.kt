package com.example.falconplayer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.data.SortType
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary

@Composable
fun DisplaySettingsScreen(
    isListView: Boolean,
    showOnlyFavorites: Boolean,
    groupOption: String,
    playbackAction: String,
    sortType: SortType,
    onBackClick: () -> Unit,
    onToggleListView: () -> Unit,
    onToggleShowOnlyFavorites: () -> Unit,
    onSelectGroupOption: (String) -> Unit,
    onSelectPlaybackAction: (String) -> Unit,
    onSelectSortType: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGroupDropdown by remember { mutableStateOf(false) }
    var showPlaybackDropdown by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconBackground)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FalconTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Display settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FalconRed
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Item 1: Display in list / grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleListView() }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isListView) Icons.Default.GridView else Icons.Default.ViewList,
                    contentDescription = null,
                    tint = FalconTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = if (isListView) "Display in grid" else "Display in list",
                    fontSize = 16.sp,
                    color = FalconTextPrimary
                )
            }

            // Item 2: Show only favourites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleShowOnlyFavorites() }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = FalconTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Show only favourites",
                        fontSize = 16.sp,
                        color = FalconTextPrimary
                    )
                }
                Checkbox(
                    checked = showOnlyFavorites,
                    onCheckedChange = { onToggleShowOnlyFavorites() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FalconRed,
                        uncheckedColor = FalconTextSecondary
                    )
                )
            }

            // Item 3: Group videos
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGroupDropdown = true }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = FalconTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "Group videos",
                            fontSize = 16.sp,
                            color = FalconTextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = groupOption,
                            fontSize = 15.sp,
                            color = FalconTextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = FalconTextSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded = showGroupDropdown,
                    onDismissRequest = { showGroupDropdown = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    listOf("Group by name", "Group by folder", "No grouping").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = FalconTextPrimary) },
                            onClick = {
                                onSelectGroupOption(option)
                                showGroupDropdown = false
                            }
                        )
                    }
                }
            }

            // Item 4: Playback action
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPlaybackDropdown = true }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = FalconTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = "Playback action",
                                fontSize = 16.sp,
                                color = FalconTextPrimary
                            )
                            Text(
                                text = "Videos",
                                fontSize = 13.sp,
                                color = FalconTextSecondary
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playbackAction,
                            fontSize = 15.sp,
                            color = FalconTextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = FalconTextSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded = showPlaybackDropdown,
                    onDismissRequest = { showPlaybackDropdown = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    listOf("Play", "Append to queue", "Play next").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = FalconTextPrimary) },
                            onClick = {
                                onSelectPlaybackAction(option)
                                showPlaybackDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2 Header: Sort by...
            Text(
                text = "Sort by...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FalconRed,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Category 1: Name
            SortCategorySection(
                icon = Icons.Default.SortByAlpha,
                title = "Name",
                options = listOf(
                    "A -> Z" to SortType.NAME_ASC,
                    "Z -> A" to SortType.NAME_DESC
                ),
                currentSort = sortType,
                onSelectSort = onSelectSortType
            )

            // Category 2: Length
            SortCategorySection(
                icon = Icons.Default.AccessTime,
                title = "Length",
                options = listOf(
                    "Shortest first" to SortType.LENGTH_ASC,
                    "Longest first" to SortType.LENGTH_DESC
                ),
                currentSort = sortType,
                onSelectSort = onSelectSortType
            )

            // Category 3: Recently added
            SortCategorySection(
                icon = Icons.Default.Schedule,
                title = "Recently added",
                options = listOf(
                    "Oldest first" to SortType.ADDED_ASC,
                    "Newest first" to SortType.ADDED_DESC
                ),
                currentSort = sortType,
                onSelectSort = onSelectSortType
            )

            // Category 4: Nb tracks
            SortCategorySection(
                icon = Icons.Default.FormatListNumbered,
                title = "Nb tracks",
                options = listOf(
                    "More videos in group" to SortType.TRACKS_DESC,
                    "Less videos in group" to SortType.TRACKS_ASC
                ),
                currentSort = sortType,
                onSelectSort = onSelectSortType
            )

            // Category 5: Insertion date
            SortCategorySection(
                icon = Icons.Default.CalendarToday,
                title = "Insertion date",
                options = listOf(
                    "Oldest first" to SortType.INSERTION_ASC
                ),
                currentSort = sortType,
                onSelectSort = onSelectSortType
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SortCategorySection(
    icon: ImageVector,
    title: String,
    options: List<Pair<String, SortType>>,
    currentSort: SortType,
    onSelectSort: (SortType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FalconTextPrimary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = FalconTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            options.forEach { (label, sortType) ->
                val isSelected = currentSort == sortType
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSort(sortType) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        color = if (isSelected) FalconRed else FalconTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = FalconRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
