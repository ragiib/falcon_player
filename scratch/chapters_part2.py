from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
from scratch.pdf_builder import build_callout, build_code_box

def get_chapters_part2(styles):
    story = []

    # =========================================================================
    # CHAPTER 6: COMPLETE UI AND DESIGN SYSTEM
    # =========================================================================
    story.append(Paragraph("Chapter 6  -  Complete UI and Design System", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("6.1 Color Palette & Visual Tokens", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player enforces a high-contrast cinematic dark theme. The color system is centralized in <code>theme/Color.kt</code> and bound to Material 3 in <code>theme/Theme.kt</code>:",
        styles['Body']
    ))

    color_table_data = [
        [Paragraph("Token Name", styles['TableHeader']), Paragraph("Hex Value", styles['TableHeader']), Paragraph("Role & Usage Across UI", styles['TableHeader'])],
        [Paragraph("<code>FalconRed</code>", styles['TableCellBold']), Paragraph("#E50914", styles['TableCellCode']), Paragraph("Primary brand accent; active playback indicators, scrubbers, FABs, selected tabs.", styles['TableCell'])],
        [Paragraph("<code>FalconRedDark</code>", styles['TableCellBold']), Paragraph("#B81D24", styles['TableCellCode']), Paragraph("Container highlights, gradient end-stops in logo feathers.", styles['TableCell'])],
        [Paragraph("<code>FalconRedLight</code>", styles['TableCellBold']), Paragraph("#FF3B30", styles['TableCellCode']), Paragraph("Secondary badges, gradient start-stops for vivid highlights.", styles['TableCell'])],
        [Paragraph("<code>FalconBackground</code>", styles['TableCellBold']), Paragraph("#101010", styles['TableCellCode']), Paragraph("Deep near-black application scaffold background, minimizing battery drain on OLED screens.", styles['TableCell'])],
        [Paragraph("<code>FalconSurface</code>", styles['TableCellBold']), Paragraph("#1E1E1E", styles['TableCellCode']), Paragraph("Elevated cards, bottom sheets, navigation bar containers, dialog backdrops.", styles['TableCell'])],
        [Paragraph("<code>FalconSurfaceVariant</code>", styles['TableCellBold']), Paragraph("#282828", styles['TableCellCode']), Paragraph("Indicator pills, secondary buttons, subtle dividers.", styles['TableCell'])],
        [Paragraph("<code>FalconTextPrimary</code>", styles['TableCellBold']), Paragraph("#FFFFFF", styles['TableCellCode']), Paragraph("High-emphasis video titles, active tab labels, timestamp digits.", styles['TableCell'])],
        [Paragraph("<code>FalconTextSecondary</code>", styles['TableCellBold']), Paragraph("#A0A0A0", styles['TableCellCode']), Paragraph("Medium-emphasis file sizes, folder counts, artist names, unselected tabs.", styles['TableCell'])],
    ]
    t_color = Table(color_table_data, colWidths=[120, 80, 274])
    t_color.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#E50914")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_color)
    story.append(Spacer(1, 10))

    story.append(Paragraph("6.2 Vector Branding: The Falcon Logo Canvas", styles['SectionHeading']))
    story.append(Paragraph(
        "Rather than loading a static raster image, Falcon Player's logo in <code>ui/components/FalconLogo.kt</code> is procedurally rendered using Compose <code>Canvas</code>. It constructs 6 mathematically proportioned vector paths:",
        styles['Body']
    ))
    story.append(Paragraph(
        "1. <b>Upper Wing Feather:</b> <code>Path</code> in dark crimson (<code>#B3121C</code>).<br/>"
        "2. <b>Middle Wing Feather:</b> <code>Path</code> in mid crimson (<code>#D81722</code>).<br/>"
        "3. <b>Lower Wing Feather:</b> <code>Path</code> in deep shadow red (<code>#960E16</code>).<br/>"
        "4. <b>Aerodynamic Beak (Play Triangle):</b> Dynamic forward play arrow rendered with a linear gradient spanning <code>#FF3843</code> to <code>#B80710</code>.<br/>"
        "5. <b>Crest Highlight:</b> Translucent white overlay (<code>Color.White.copy(alpha = 0.22f)</code>) simulating specular reflection.<br/>"
        "6. <b>Piercing White Eye:</b> Centered polygon highlighting the playhead core.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 7: MEDIA DISCOVERY AND LIBRARY MANAGEMENT
    # =========================================================================
    story.append(Paragraph("Chapter 7  -  Media Discovery and Library Management", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("7.1 Local Video Discovery via MediaStore", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player queries the Android <code>MediaStore.Video.Media.EXTERNAL_CONTENT_URI</code> database through a robust projection defined in <code>VideoRepository.kt</code>:",
        styles['Body']
    ))
    code_proj = """// Location: app/src/main/java/com/example/falconplayer/data/VideoRepository.kt
val projection = arrayOf(
    MediaStore.Video.Media._ID,
    MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.TITLE,
    MediaStore.Video.Media.DURATION,
    MediaStore.Video.Media.WIDTH,
    MediaStore.Video.Media.HEIGHT,
    MediaStore.Video.Media.SIZE,
    MediaStore.Video.Media.BUCKET_ID,
    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    MediaStore.Video.Media.DATA,
    MediaStore.Video.Media.DATE_ADDED
)"""
    story.extend(build_code_box(code_proj, styles))
    story.append(Paragraph(
        "<b>Handling Missing or Null Metadata:</b> Android devices vary widely in how MediaStore indexes media. Falcon Player implements defensive fallbacks:<br/>"
        "• <b>Title Fallback:</b> <code>val name = displayName ?: title ?: dataPath?.let { File(it).name } ?: \"Video_$id\"</code><br/>"
        "• <b>Folder Bucket Fallback:</b> If <code>BUCKET_DISPLAY_NAME</code> is null, the repository parses <code>File(dataPath).parentFile.name</code>. If <code>BUCKET_ID</code> is null, it hashes the bucket name: <code>bucketName.hashCode().toString()</code>.<br/>"
        "• <b>Resolution Badge Calculation:</b> Dynamically inspects <code>width</code> and <code>height</code> to append <code>4K</code> (>= 2160p), <code>1080p</code>, <code>720p</code>, or <code>480p</code> badges.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("7.2 Scoped Storage Deletion & Android OS Dialogs", styles['SectionHeading']))
    story.append(Paragraph(
        "Android Scoped Storage rules (Android 10+) strictly prohibit an application from directly calling <code>File.delete()</code> on media files owned by other apps. Falcon Player implements a three-tier deletion architecture in <code>VideoRepository.deleteVideo()</code>:",
        styles['Body']
    ))
    code_del = """// Location: app/src/main/java/com/example/falconplayer/data/VideoRepository.kt
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    // Android 11+ (API 30+)  -  Atomically requests OS permission dialog
    val intentSender = MediaStore.createDeleteRequest(
        context.contentResolver,
        listOf(videoUri)
    ).intentSender
    DeleteResult.NeedsPermission(intentSender)
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    // Android 10 (API 29)  -  Catch RecoverableSecurityException
    try {
        val deleted = context.contentResolver.delete(videoUri, null, null)
        if (deleted > 0) DeleteResult.Success else DeleteResult.Failure(\"Not found\")
    } catch (e: android.app.RecoverableSecurityException) {
        DeleteResult.NeedsPermission(e.userAction.actionIntent.intentSender)
    }
} else {
    // Android 9 and below  -  Direct delete
    val deleted = context.contentResolver.delete(videoUri, null, null)
    if (deleted > 0) DeleteResult.Success else DeleteResult.Failure(\"Not found\")
}"""
    story.extend(build_code_box(code_del, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("7.3 Multi-Selection and Bulk Deletion", styles['SectionHeading']))
    story.append(Paragraph(
        "In <code>HomeScreen.kt</code>, users can long-press any video card to enter <b>Selection Mode</b> (<code>isSelectionMode = true</code>). Multiple video IDs are collected in a <code>Set&lt;Long&gt;</code>.<br/>"
        "When the user taps <b>Delete Selected</b>, <code>HomeViewModel.deleteSelectedVideos()</code> passes all selected URIs to <code>VideoRepository.deleteVideos(uris)</code>. On Android 11+, <code>MediaStore.createDeleteRequest(contentResolver, uris)</code> displays <b>a single system dialog</b> confirming the deletion of all selected items simultaneously.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 8: VIDEO PLAYBACK ENGINE: DEEP TECHNICAL EXPLANATION
    # =========================================================================
    story.append(Paragraph("Chapter 8  -  Video Playback Engine: Deep Technical Explanation", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("8.1 Player Initialization & Surface Interoperability", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player instantiates its video engine via <code>ExoPlayer.Builder(context).build()</code> inside <code>PlayerViewModel.kt</code>. To embed the video surface inside a declarative Compose hierarchy, <code>PlayerScreen.kt</code> uses <code>AndroidView</code> wrapping Media3's <code>PlayerView</code>:",
        styles['Body']
    ))
    code_surface = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerScreen.kt
AndroidView(
    factory = { ctx ->
        PlayerView(ctx).apply {
            player = viewModel.player
            useController = false // Custom Compose controls used instead
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
)"""
    story.extend(build_code_box(code_surface, styles))
    story.append(Paragraph(
        "Setting <code>useController = false</code> instructs Media3 to hide its default XML playback controls, allowing Falcon Player's custom Compose overlays to manage the entire user interface.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("8.2 Double-Tap Seeking with Cumulative Time Aggregation", styles['SectionHeading']))
    story.append(Paragraph(
        "A critical feature in Falcon Player is <b>accumulating double-tap seek feedback</b>. Rapid successive taps continue adding +10s (+10s, +20s, +30s) rather than resetting or flashing:",
        styles['Body']
    ))
    code_seek = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerViewModel.kt
fun triggerDoubleTapSeek(isForward: Boolean) {
    if (doubleTapLastIsForward != isForward) {
        doubleTapSeekAccumulatedMs = 0L // Reset if direction changed
    }
    doubleTapLastIsForward = isForward
    doubleTapSeekAccumulatedMs += 10_000L

    val seekDeltaMs = if (isForward) 10_000L else -10_000L
    val target = (player.currentPosition + seekDeltaMs)
        .coerceIn(0L, player.duration.coerceAtLeast(0L))
    player.seekTo(target)
    updateMediaInfo()
    onUserActivity()

    val totalSeconds = (doubleTapSeekAccumulatedMs / 1000L).toInt()
    _uiState.update {
        it.copy(
            seekFeedback = SeekFeedback(isForward, totalSeconds, System.currentTimeMillis())
        )
    }
    seekFeedbackJob?.cancel()
    seekFeedbackJob = viewModelScope.launch {
        delay(900L) // Interaction window
        _uiState.update { it.copy(seekFeedback = null) }
        doubleTapSeekAccumulatedMs = 0L
        doubleTapLastIsForward = null
    }
}"""
    story.extend(build_code_box(code_seek, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("8.3 Playback State Machine & Lifecycle Release", styles['SectionHeading']))
    story.append(Paragraph(
        "ExoPlayer reports its internal state via <code>Player.Listener</code>. Falcon Player maps these states to its domain <code>PlaybackState</code> interface:<br/>"
        "• <code>STATE_IDLE</code> -> <code>PlaybackState.Idle</code><br/>"
        "• <code>STATE_BUFFERING</code> -> <code>PlaybackState.Buffering</code> (triggers custom pulsing indicator)<br/>"
        "• <code>STATE_READY</code> -> <code>PlaybackState.Playing</code> if <code>isPlaying</code>, else <code>Paused</code><br/>"
        "• <code>STATE_ENDED</code> -> <code>PlaybackState.Ended</code> (saves resume position to 0L, advances playlist if present)<br/>"
        "When the user leaves the player, <code>ViewModel.onCleared()</code> persists the playback position and invokes <code>player.release()</code> to free hardware decoders immediately.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 9: AUDIO PLAYBACK AND AUDIO LIBRARY
    # =========================================================================
    story.append(Paragraph("Chapter 9  -  Audio Playback and Audio Library", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("9.1 The AudioPlaybackManager Architecture", styles['SectionHeading']))
    story.append(Paragraph(
        "Audio playback requires an independent, application-scoped lifecycle so that playback can continue when users navigate between tabs. In <code>data/AudioPlaybackManager.kt</code>, an <code>@Singleton</code> ExoPlayer instance is initialized on the Application Context:",
        styles['Body']
    ))
    code_audio = """// Location: app/src/main/java/com/example/falconplayer/data/AudioPlaybackManager.kt
val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
    val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()
    setAudioAttributes(audioAttributes, true) // Automatically manages Audio Focus!
    setHandleAudioBecomingNoisy(true)        // Pauses automatically if headphones disconnect!
}"""
    story.extend(build_code_box(code_audio, styles))
    story.append(Paragraph(
        "<b>Audio Focus & Becoming Noisy:</b> Passing <code>true</code> to <code>setAudioAttributes</code> ensures Falcon Player automatically ducks or pauses when phone calls or navigation directions occur. Setting <code>setHandleAudioBecomingNoisy(true)</code> registers an internal <code>BroadcastReceiver</code> that pauses playback immediately if Bluetooth or wired headphones are disconnected, avoiding embarrassing loud playback in public environments.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("9.2 Queue Management, Shuffle, and Repeat Modes", styles['SectionHeading']))
    story.append(Paragraph(
        "<code>AudioPlaybackManager</code> maintains an <code>originalQueue</code> and an active <code>queue</code>. When the user enables shuffle (<code>toggleShuffle()</code>), the current track is retained as index 0, and all remaining tracks are shuffled around it, preventing the currently playing song from restarting. Three repeat modes are supported: <code>OFF</code> (stops at queue end), <code>ALL</code> (loops back to track 0), and <code>ONE</code> (seeks to 0 and replays current track).",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 10: AUDIO TRACKS AND SUBTITLE SYSTEM
    # =========================================================================
    story.append(Paragraph("Chapter 10  -  Audio Tracks and Subtitle System", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("10.1 Track Discovery & Metadata Extraction", styles['SectionHeading']))
    story.append(Paragraph(
        "When ExoPlayer finishes parsing media containers (such as MKV or MP4), it triggers <code>onTracksChanged(tracks: Tracks)</code>. In <code>PlayerViewModel.extractTracks()</code>, Falcon Player scans all track groups:",
        styles['Body']
    ))
    code_tracks = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerViewModel.kt
for (groupIndex in 0 until tracks.groups.size) {
    val group = tracks.groups[groupIndex]
    if (group.type == C.TRACK_TYPE_AUDIO) {
        for (trackIndex in 0 until group.length) {
            val format = group.getTrackFormat(trackIndex)
            val displayName = formatAudioTrackName(format, audioCount)
            val technicalDetails = formatAudioTechnicalInfo(format) // Codec, 5.1/Stereo, kHz
            // Create TrackItem...
        }
    } else if (group.type == C.TRACK_TYPE_TEXT) {
        for (trackIndex in 0 until group.length) {
            val format = group.getTrackFormat(trackIndex)
            val displayName = formatSubtitleTrackName(format, subtitleCount)
            val technicalDetails = formatSubtitleTechnicalInfo(format) // SRT, ASS, Forced, SDH
            // Create TrackItem...
        }
    }
}"""
    story.extend(build_code_box(code_tracks, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("10.2 Track Switching via TrackSelectionOverride", styles['SectionHeading']))
    story.append(Paragraph(
        "To switch audio or subtitle tracks without reloading the media file or resetting playback position, Falcon Player builds a <code>TrackSelectionOverride</code>:",
        styles['Body']
    ))
    code_override = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerViewModel.kt
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon()
    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
    .setOverrideForType(
        TrackSelectionOverride(track.group, listOf(track.trackIndex))
    )
    .build()"""
    story.extend(build_code_box(code_override, styles))
    story.append(Paragraph(
        "To disable subtitles completely, Falcon Player calls <code>setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)</code> and <code>clearOverridesOfType(C.TRACK_TYPE_TEXT)</code>, instantly clearing on-screen text overlays.",
        styles['Body']
    ))
    story.append(PageBreak())

    return story
