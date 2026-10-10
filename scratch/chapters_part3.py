from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
from scratch.pdf_builder import build_callout, build_code_box

def get_chapters_part3(styles):
    story = []

    # =========================================================================
    # CHAPTER 11: TIMELINE, CONTROLS, GESTURES, AND FULLSCREEN
    # =========================================================================
    story.append(Paragraph("Chapter 11  -  Timeline, Controls, Gestures, and Fullscreen", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("11.1 Custom Interactive Timeline Scrubbing Canvas", styles['SectionHeading']))
    story.append(Paragraph(
        "Standard Android sliders or Compose <code>Slider</code> components can feel clunky and visually unrefined for video scrubbing. Falcon Player creates a custom, high-precision timeline in <code>ui/player/components/VideoTimeline.kt</code> using Compose <code>Canvas</code> and pointer input detection:",
        styles['Body']
    ))
    code_timeline = """// Location: app/src/main/java/com/example/falconplayer/ui/player/components/VideoTimeline.kt
val trackHeight by animateDpAsState(if (isDragging) 5.5.dp else 3.5.dp)
val thumbRadius by animateDpAsState(if (isDragging) 8.dp else 5.dp)

Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
    // 1. Inactive full duration track (semi-transparent white)
    drawRoundRect(color = Color.White.copy(alpha = 0.22f), size = Size(size.width, size.height))
    // 2. Buffered progress track (brighter white)
    if (bufferedFraction > 0f) {
        drawRoundRect(color = Color.White.copy(alpha = 0.45f), size = Size(size.width * bufferedFraction, size.height))
    }
    // 3. Played progress track (Falcon Red)
    if (playedFraction > 0f) {
        drawRoundRect(color = FalconRed, size = Size(size.width * playedFraction, size.height))
    }
    // 4. Dual-layer Scrubber Thumb (outer Falcon Red ring, inner crisp white core)
    drawCircle(color = FalconRed, radius = thumbRadius.toPx(), center = thumbCenter)
    drawCircle(color = Color.White, radius = thumbRadius.toPx() * 0.4f, center = thumbCenter)
}"""
    story.extend(build_code_box(code_timeline, styles))
    story.append(Paragraph(
        "<b>Touch Ergonomics:</b> The <code>VideoTimeline</code> wraps the Canvas in a 36dp touch target box with both <code>detectTapGestures</code> (for immediate jumps) and <code>detectDragGestures</code> (for smooth scrub previewing). The track height expands dynamically from 3.5dp to 5.5dp when the user touches the bar, providing tactile visual feedback.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("11.2 Vertical Drag Gestures: Brightness and Volume HUDs", styles['SectionHeading']))
    story.append(Paragraph(
        "Inside <code>PlayerScreen.kt</code>, vertical swipe gestures on the screen surface dynamically adjust device brightness and media volume:",
        styles['Body']
    ))
    code_hud = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerScreen.kt
detectVerticalDragGestures(
    onDragStart = { offset ->
        if (offset.x < size.width * 0.45f) { // LEFT HALF -> Brightness
            showBrightnessHud = true
        } else if (offset.x > size.width * 0.55f) { // RIGHT HALF -> Volume
            showVolumeHud = true
        }
    },
    onVerticalDrag = { change, dragAmount ->
        change.consume()
        val delta = -dragAmount / (size.height * 0.65f)
        if (change.position.x < size.width * 0.45f) {
            brightnessLevel = (brightnessLevel + delta).coerceIn(0.01f, 1.0f)
            activity?.window?.attributes = activity.window.attributes.apply { screenBrightness = brightnessLevel }
        } else if (change.position.x > size.width * 0.55f) {
            volumeLevel = (volumeLevel + delta).coerceIn(0f, 1f)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volumeLevel * maxVol).toInt(), 0)
        }
    }
)"""
    story.extend(build_code_box(code_hud, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("11.3 Troubleshooting: The Fullscreen Orientation Reset Fix", styles['SectionHeading']))
    story.append(Paragraph(
        "A common bug in mobile video players is that requesting landscape orientation via <code>activity.requestedOrientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE</code> locks the entire application in landscape permanently even after returning to the library.<br/>"
        "<b>Root Cause & Falcon Player Solution:</b> In <code>PlayerScreen.kt</code>, Falcon Player binds a <code>DisposableEffect(Unit)</code> directly to the screen's lifecycle. When the screen exits the Compose backstack, <code>onDispose</code> runs unconditionally:<br/>"
        "<code>activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED</code>.<br/>"
        "This ensures the device orientation sensor immediately regains control as the user navigates back to <code>HomeScreen</code>.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 12: ANDROID PERMISSIONS, SECURITY, AND STORAGE
    # =========================================================================
    story.append(Paragraph("Chapter 12  -  Android Permissions, Security, and Storage", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("12.1 Granular Runtime Permission Matrix", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player adapts its permission requests depending on the active Android OS API level:",
        styles['Body']
    ))

    perm_data = [
        [Paragraph("Permission String", styles['TableHeader']), Paragraph("API Level", styles['TableHeader']), Paragraph("Architectural Purpose & Fallback Behavior", styles['TableHeader'])],
        [
            Paragraph("<code>READ_MEDIA_VIDEO</code>", styles['TableCellCode']),
            Paragraph("API 33+ (Android 13+)", styles['TableCell']),
            Paragraph("Allows discovering local video files through MediaStore without requesting photo or document permissions.", styles['TableCell'])
        ],
        [
            Paragraph("<code>READ_MEDIA_AUDIO</code>", styles['TableCellCode']),
            Paragraph("API 33+ (Android 13+)", styles['TableCell']),
            Paragraph("Allows discovering music tracks, album metadata, and artist collections.", styles['TableCell'])
        ],
        [
            Paragraph("<code>READ_EXTERNAL_STORAGE</code>", styles['TableCellCode']),
            Paragraph("API 24 - 32 (maxSdk 32)", styles['TableCell']),
            Paragraph("Legacy media read access on Android 7.0 through Android 12L.", styles['TableCell'])
        ],
        [
            Paragraph("<code>WRITE_EXTERNAL_STORAGE</code>", styles['TableCellCode']),
            Paragraph("API 24 - 29 (maxSdk 29)", styles['TableCell']),
            Paragraph("Legacy file deletion permission on Android 9 (Pie) and below.", styles['TableCell'])
        ],
        [
            Paragraph("<code>FOREGROUND_SERVICE_MEDIA_PLAYBACK</code>", styles['TableCellCode']),
            Paragraph("API 34+ (Android 14+)", styles['TableCell']),
            Paragraph("Declares foreground service permission for uninterrupted media playback.", styles['TableCell'])
        ],
        [
            Paragraph("<code>POST_NOTIFICATIONS</code>", styles['TableCellCode']),
            Paragraph("API 33+ (Android 13+)", styles['TableCell']),
            Paragraph("Permits displaying playback notifications with lock-screen media controls.", styles['TableCell'])
        ]
    ]
    t_perm = Table(perm_data, colWidths=[160, 90, 244])
    t_perm.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#2D3748")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_perm)
    story.append(Spacer(1, 10))

    story.append(Paragraph("12.2 Privacy & Incognito Mode Architecture", styles['SectionHeading']))
    story.append(Paragraph(
        "In <code>HistoryRepository.kt</code>, Falcon Player checks <code>DisplaySettingsRepository.settings.value.incognitoMode</code> prior to writing video playback entries. When Incognito Mode is toggled on, playback history recording is completely bypassed, ensuring zero trace of media viewing is stored in <code>falcon_history.json</code>.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 13: ARCHITECTURE AND DATA FLOW
    # =========================================================================
    story.append(Paragraph("Chapter 13  -  Architecture and Data Flow", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("13.1 Unidirectional Data Flow (UDF) Mechanics", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player strictly adheres to the Unidirectional Data Flow pattern. UI state flows downwards from ViewModels to Composables via immutable <code>StateFlow</code> objects, while user events (clicks, drags, seeks) flow upwards as lambdas.",
        styles['Body']
    ))

    udf_diagram = """
           +---------------------------------------------+
           |                 VIEW MODEL                  |
           |  _uiState = MutableStateFlow(PlayerUiState) |
           |  uiState: StateFlow<PlayerUiState>          |
           +---------------------+-----------------------+
                                 |  Emits Immutable State
                                 v
           +---------------------------------------------+
           |              COMPOSE UI SCREEN              |
           |  val uiState by viewModel.uiState           |
           |    .collectAsStateWithLifecycle()           |
           |  Renders PlayerView, Controls, HUDs...      |
           +---------------------+-----------------------+
                                 |  Dispatches User Events
                                 v  (onPlayPause, onSeek, toggleResize...)
           +---------------------------------------------+
           |             VIEW MODEL ACTIONS              |
           |  fun onSeek(pos) -> player.seekTo(pos)      |
           |  Updates _uiState via .update { it.copy() } |
           +---------------------------------------------+
"""
    story.extend(build_code_box(udf_diagram, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("13.2 Storage Tiering: DataStore vs JSON Files", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player employs two distinct persistent storage mechanisms based on data access patterns:<br/>"
        "1. <b>Jetpack DataStore Preferences (<code>PlaybackPositionRepository</code>):</b> Key-value storage mapping video URI strings to millisecond playback timestamps. Highly optimized for rapid, frequent writes as playback progresses.<br/>"
        "2. <b>Thread-Safe JSON Files (<code>PlaylistRepository</code>, <code>DisplaySettingsRepository</code>, <code>FavoritesRepository</code>, <code>HistoryRepository</code>):</b> Structured lists serialized via Kotlinx Serialization into <code>context.filesDir</code>. Each repository utilizes a Kotlin <code>Mutex</code> (<code>mutex.withLock { ... }</code>) ensuring that parallel coroutines never corrupt file data during read/write cycles.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 14: DEPENDENCIES AND EXTERNAL LIBRARIES
    # =========================================================================
    story.append(Paragraph("Chapter 14  -  Dependencies and External Libraries", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("14.1 Gradle Version Catalog Analysis (libs.versions.toml)", styles['SectionHeading']))
    story.append(Paragraph(
        "All project dependencies are pinned and centrally managed in <code>gradle/libs.versions.toml</code>:",
        styles['Body']
    ))

    dep_table = [
        [Paragraph("Dependency / Library", styles['TableHeader']), Paragraph("Catalog Version", styles['TableHeader']), Paragraph("Purpose & Impact on Application", styles['TableHeader'])],
        [Paragraph("<code>androidx-media3-exoplayer</code>", styles['TableCellCode']), Paragraph("1.7.0", styles['TableCell']), Paragraph("Core playback engine for video/audio decoding, buffering, seeking.", styles['TableCell'])],
        [Paragraph("<code>androidx-media3-ui</code>", styles['TableCellCode']), Paragraph("1.7.0", styles['TableCell']), Paragraph("Provides <code>PlayerView</code>, <code>AspectRatioFrameLayout</code>, and subtitle renderers.", styles['TableCell'])],
        [Paragraph("<code>androidx-media3-session</code>", styles['TableCellCode']), Paragraph("1.7.0", styles['TableCell']), Paragraph("MediaSession APIs for system controls and background notification sync.", styles['TableCell'])],
        [Paragraph("<code>androidx-compose-bom</code>", styles['TableCellCode']), Paragraph("2026.03.01", styles['TableCell']), Paragraph("Bill of Materials aligning Compose UI, Foundation, and Material 3 versions.", styles['TableCell'])],
        [Paragraph("<code>androidx-navigation3-ui</code>", styles['TableCellCode']), Paragraph("1.0.1", styles['TableCell']), Paragraph("Latest type-safe navigation framework using <code>NavKey</code> and <code>NavDisplay</code>.", styles['TableCell'])],
        [Paragraph("<code>hilt-android</code>", styles['TableCellCode']), Paragraph("2.60.1", styles['TableCell']), Paragraph("Dagger Hilt dependency injection for repositories, managers, and viewmodels.", styles['TableCell'])],
        [Paragraph("<code>androidx-datastore-preferences</code>", styles['TableCellCode']), Paragraph("1.1.1", styles['TableCell']), Paragraph("Modern replacement for SharedPreferences storing playback resume positions.", styles['TableCell'])],
        [Paragraph("<code>kotlinx-serialization-json</code>", styles['TableCellCode']), Paragraph("1.7.3", styles['TableCell']), Paragraph("Compile-time JSON serialization for playlists, favorites, and settings files.", styles['TableCell'])],
    ]
    t_dep = Table(dep_table, colWidths=[160, 70, 264])
    t_dep.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#E50914")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_dep)
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 15: BUILD SYSTEM AND APK DISTRIBUTION
    # =========================================================================
    story.append(Paragraph("Chapter 15  -  Build System and APK Distribution", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("15.1 Build Configuration & SDK Targets", styles['SectionHeading']))
    story.append(Paragraph(
        "From <code>app/build.gradle.kts</code>, Falcon Player targets the newest Android platform features while retaining backwards compatibility:<br/>"
        "• <code>compileSdk = 36</code>: Allows compilation against the latest Android platform APIs.<br/>"
        "• <code>targetSdk = 36</code>: Enforces modern Android runtime behaviors, scoped storage, and notification permissions.<br/>"
        "• <code>minSdk = 24</code>: Supports Android 7.0 (Nougat) and higher, covering over 96% of active Android devices globally.<br/>"
        "• <code>JavaVersion.VERSION_17</code>: Standard JVM bytecode target using Gradle toolchains.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("15.2 Building and Installing via ADB", styles['SectionHeading']))
    story.append(Paragraph(
        "To assemble, verify, and install Falcon Player on a physical device or emulator, execute the following commands:",
        styles['Body']
    ))
    code_build = """# 1. Clean and assemble Debug APK
./gradlew clean assembleDebug

# 2. Output location of generated APK:
# app/build/outputs/apk/debug/app-debug.apk

# 3. Install onto connected Android device via ADB:
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Launch Falcon Player directly from terminal:
adb shell am start -n com.example.falconplayer/.MainActivity"""
    story.extend(build_code_box(code_build, styles))
    story.append(PageBreak())

    return story
