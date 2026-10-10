from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
from scratch.pdf_builder import build_callout, build_code_box

def get_chapters_part1(styles):
    story = []
    
    # =========================================================================
    # CHAPTER 1: PROJECT OVERVIEW
    # =========================================================================
    story.append(Paragraph("Chapter 1  -  Project Overview", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("1.1 Executive Summary & Mission", styles['SectionHeading']))
    story.append(Paragraph(
        "<b>Falcon Player</b> (package <code>com.example.falconplayer</code>) is a state-of-the-art, high-performance multimedia playback application for Android. Engineered entirely using <b>Jetpack Compose</b> (Material 3), Google's cutting-edge <b>AndroidX Media3 (ExoPlayer 1.7.0)</b> engine, and <b>Dagger Hilt</b> dependency injection, Falcon Player provides an uncompromising local playback experience for high-definition video and lossless audio.",
        styles['Body']
    ))
    story.append(Paragraph(
        "Unlike generic Android media players that wrap legacy Android Views or rely on outdated ExoPlayer v2 APIs, Falcon Player is built from the ground up on modern declarative Android development paradigms. It features hardware-accelerated video rendering with custom interactive gestures, immersive system bar management, scoped storage discovery and deletion, multi-track audio and subtitle selection, background music queue management, and a custom aerodynamic branding system.",
        styles['Body']
    ))

    story.append(Paragraph("1.2 Core Capabilities & Feature Map", styles['SectionHeading']))
    
    feature_data = [
        [Paragraph("Functional Area", styles['TableHeader']), Paragraph("Core Capabilities Implemented", styles['TableHeader']), Paragraph("Underlying Technology", styles['TableHeader'])],
        [
            Paragraph("<b>Video Playback Engine</b>", styles['TableCellBold']),
            Paragraph("Hardware-accelerated decoding, aspect ratio switching (Fit/Zoom/Stretch), dynamic playback speed (0.25x - 2.0x), double-tap seek with time accumulation, vertical drag brightness & volume HUDs, picture-in-picture mode, timeline scrubbing with buffer display.", styles['TableCell']),
            Paragraph("AndroidX Media3 ExoPlayer 1.7.0, PlayerView, Compose AndroidView interop", styles['TableCellCode'])
        ],
        [
            Paragraph("<b>Audio Player & Library</b>", styles['TableCellBold']),
            Paragraph("Categorized music browsing (Artists, Albums, Tracks, Genres), persistent playback queue, shuffle & repeat modes (Off, All, One), floating expandable Mini Player, full-screen playback sheet with album art, automatic audio focus management, and headphone disconnection safety.", styles['TableCell']),
            Paragraph("Media3 ExoPlayer, AudioManager, ContentResolver thumbnail loader", styles['TableCellCode'])
        ],
        [
            Paragraph("<b>Media Discovery</b>", styles['TableCellBold']),
            Paragraph("Zero-lag MediaStore video & audio scanning, automatic bucket/folder grouping, resolution badge computation (4K, 1080p, 720p, 480p), memory-efficient LruCache thumbnail extraction with ContentResolver and MediaMetadataRetriever fallbacks.", styles['TableCell']),
            Paragraph("MediaStore.Video/Audio, LruCache, Kotlin Coroutines Dispatchers.IO", styles['TableCellCode'])
        ],
        [
            Paragraph("<b>Scoped Storage & Deletion</b>", styles['TableCellBold']),
            Paragraph("Full compatibility with Android 11+ (API 30+) scoped storage delete requests, Android 10 (API 29) RecoverableSecurityException handling, single-item deletion, long-press multi-selection, and atomic bulk file deletion.", styles['TableCell']),
            Paragraph("MediaStore.createDeleteRequest, IntentSender, ActivityResultContracts", styles['TableCellCode'])
        ],
        [
            Paragraph("<b>Playlists & Organization</b>", styles['TableCellBold']),
            Paragraph("Custom video playlists with validation, unique ID generation, add/remove videos, drag-and-drop reordering, JSON file persistence with thread-safe Mutex locking, search filtering, and incognito mode history exclusion.", styles['TableCell']),
            Paragraph("Kotlinx Serialization, internal storage JSON, Kotlin Mutex", styles['TableCellCode'])
        ],
        [
            Paragraph("<b>File Explorer (Browse)</b>", styles['TableCellBold']),
            Paragraph("Direct file system exploration, primary internal storage traversal, secondary SD card detection, quick-access shortcuts (Download, Movies, Music), and media file counting.", styles['TableCell']),
            Paragraph("Java File API, Environment.getExternalStorageDirectory", styles['TableCellCode'])
        ],
    ]
    t_feat = Table(feature_data, colWidths=[120, 240, 144])
    t_feat.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#E50914")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_feat)
    story.append(Spacer(1, 10))

    story.append(Paragraph("1.3 High-Level System Architecture & Layering", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player follows a decoupled <b>Unidirectional Data Flow (UDF)</b> layered architecture with Clean Architecture principles adapted for modern Jetpack Compose applications:",
        styles['Body']
    ))

    arch_diagram = """
+-----------------------------------------------------------------------------------+
|                                PRESENTATION LAYER                                 |
|  MainActivity -> MainNavigation (AndroidX Navigation 3 NavDisplay & NavKey)       |
|                                                                                   |
|  [HomeScreen]           [PlayerScreen]           [AudioScreen]    [BrowseScreen]  |
|  - Video/Playlist Tabs  - PlayerView Surface     - TabRow (4 tabs)- Storage Tree  |
|  - Selection Toolbar    - Touch Gesture Scrim    - MiniPlayer Bar - Breadcrumbs   |
|  - Grid/List Video Card - Audio/Sub Sheet        - PlayerSheet    - Folder Nodes  |
|        ^                       ^                        ^                ^        |
|        | StateFlow             | StateFlow              | StateFlow      | StateF.|
|  [HomeViewModel]        [PlayerViewModel]        [AudioViewModel] [BrowseViewModel]|
+--------+-----------------------+------------------------+----------------+--------+
         |                       |                        |                |
         +-----------------------+-----------+------------+----------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------+
|                                  DATA LAYER                                       |
|  [Repositories & Managers]                                                        |
|  - VideoRepository        : MediaStore queries, bucket aggregation, scoped delete |
|  - AudioRepository        : MediaStore audio tracks, albums, artists, genres      |
|  - AudioPlaybackManager   : Application-scoped ExoPlayer audio playback engine    |
|  - BrowseRepository       : Direct filesystem file/folder enumeration             |
|  - PlaylistRepository     : JSON persistence (falcon_playlists.json) with Mutex   |
|  - DisplaySettingsRepo    : JSON persistence (falcon_display_settings.json)       |
|  - FavoritesRepository    : JSON persistence (falcon_favorites.json)              |
|  - HistoryRepository      : JSON persistence (falcon_history.json) with Incognito |
|  - PlaybackPositionRepo   : Jetpack DataStore Preferences (playback_positions)    |
+--------------------------------------------+--------------------------------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------+
|                           ANDROID OS & SYSTEM SERVICES                            |
|  - MediaStore / ContentResolver (android.provider.MediaStore)                     |
|  - AudioFocus / AudioManager (android.media.AudioManager)                        |
|  - WindowInsetsControllerCompat (Edge-to-Edge & Immersive transient bars)         |
|  - Local File System (context.filesDir & /storage/emulated/0)                     |
|  - ActivityResultContracts (Permissions & Scoped Storage IntentSender dialogs)    |
+-----------------------------------------------------------------------------------+
"""
    story.extend(build_code_box(arch_diagram, styles))
    story.append(Spacer(1, 10))

    story.append(Paragraph("1.4 What Happens When the Application Launches", styles['SectionHeading']))
    story.append(Paragraph(
        "When the user taps the Falcon Player icon on their device home screen, the Android system triggers a precise initialization cascade:",
        styles['Body']
    ))
    story.append(Paragraph(
        "<b>Step 1: Application Subclass Initialization.</b> The operating system instantiates <code>FalconPlayerApp</code>, which is annotated with <code>@HiltAndroidApp</code>. Dagger Hilt builds the dependency graph and registers singleton providers in memory.",
        styles['Body']
    ))
    story.append(Paragraph(
        "<b>Step 2: Activity Creation & Edge-to-Edge Configuration.</b> The system launches <code>MainActivity</code> (annotated with <code>@AndroidEntryPoint</code>). Inside <code>onCreate()</code>, <code>enableEdgeToEdge()</code> is invoked, and <code>WindowCompat.getInsetsController()</code> sets <code>BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE</code>, hiding system status and navigation bars for an edge-to-edge cinematic canvas.",
        styles['Body']
    ))
    story.append(Paragraph(
        "<b>Step 3: Compose Root Attachment.</b> <code>setContent</code> renders <code>FalconPlayerTheme</code> wrapping <code>MainNavigation()</code>. Navigation 3 initializes <code>rememberNavBackStack(Home)</code> with the <code>Home</code> NavKey.",
        styles['Body']
    ))
    story.append(Paragraph(
        "<b>Step 4: Permission Verification & Media Scanning.</b> <code>HomeScreen</code> queries storage permissions. On Android 13+ (API 33+), it requests <code>READ_MEDIA_VIDEO</code> and <code>READ_MEDIA_AUDIO</code>; on older platforms, it requests <code>READ_EXTERNAL_STORAGE</code>. Once granted, <code>HomeViewModel.loadMedia()</code> executes a background MediaStore query via <code>Dispatchers.IO</code>, extracts video metadata, builds folder groupings, and emits the data via <code>HomeUiState</code> to render the media grid.",
        styles['Body']
    ))

    story.append(build_callout(
        "Modern Architecture Highlight",
        "Falcon Player completely avoids deprecated Android APIs. It uses AndroidX Navigation 3 (NavDisplay and NavKey) instead of legacy string-based route parsing, AndroidX Media3 instead of legacy com.google.android.exoplayer2, and Jetpack DataStore Preferences instead of legacy SharedPreferences.",
        "arch", styles
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 2: COMPLETE PROJECT STRUCTURE
    # =========================================================================
    story.append(Paragraph("Chapter 2  -  Complete Project Structure", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("2.1 Codebase File Tree", styles['SectionHeading']))
    story.append(Paragraph(
        "The complete source structure of the Falcon Player application demonstrates clean separation between data modeling, storage repositories, UI presentation, design tokens, and components:",
        styles['Body']
    ))

    tree_str = """
c:\\falcon_player\\
├── app/
│   ├── build.gradle.kts                      # Module build script, dependencies, SDK configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml           # Manifest permissions, activity & hardware configs
│   │   │   ├── java/com/example/falconplayer/
│   │   │   │   ├── FalconPlayerApp.kt        # Application class with @HiltAndroidApp
│   │   │   │   ├── MainActivity.kt           # ComponentActivity entry point, immersive controller
│   │   │   │   ├── Navigation.kt             # Navigation 3 NavDisplay & entryProvider
│   │   │   │   ├── NavigationKeys.kt         # NavKey serializable routes (Home, Player, PlaylistDetail)
│   │   │   │   ├── data/
│   │   │   │   │   ├── MediaItem.kt          # VideoItem, FolderItem, AudioItem, ArtistItem, AlbumItem
│   │   │   │   │   ├── Playlist.kt           # Playlist data model (id, name, videoUris)
│   │   │   │   │   ├── BrowseItem.kt         # BrowseFolderItem, BrowseFileItem, BrowseNodeItem
│   │   │   │   │   ├── VideoRepository.kt    # MediaStore video queries, folders, scoped delete
│   │   │   │   │   ├── AudioRepository.kt    # MediaStore audio tracks, artists, albums, genres
│   │   │   │   │   ├── AudioPlaybackManager.kt # Singleton ExoPlayer for audio, queue, audio focus
│   │   │   │   │   ├── BrowseRepository.kt   # File system directory scanner & storage root finder
│   │   │   │   │   ├── PlaylistRepository.kt # JSON file storage for playlists, CRUD, validation
│   │   │   │   │   ├── DisplaySettingsRepository.kt # JSON storage for view modes & sort orders
│   │   │   │   │   ├── FavoritesRepository.kt# JSON storage for favorite media URIs
│   │   │   │   │   ├── HistoryRepository.kt  # JSON storage for playback history & incognito mode
│   │   │   │   │   └── PlaybackPositionRepository.kt # DataStore Preferences for resume positions
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt              # FalconRed (#E50914), Dark surfaces, text tokens
│   │   │   │   │   ├── Theme.kt              # FalconPlayerTheme with darkColorScheme
│   │   │   │   │   └── Type.kt               # Typography system
│   │   │   │   └── ui/
│   │   │   │       ├── audio/
│   │   │   │       │   ├── AudioScreen.kt    # 4-tab audio library (Artists, Albums, Tracks, Genres)
│   │   │   │       │   ├── AudioViewModel.kt # Audio state management & search filtering
│   │   │   │       │   └── components/
│   │   │   │       │       ├── AudioMiniPlayer.kt  # Persistent floating mini player bar
│   │   │   │       │       └── AudioPlayerSheet.kt # Expandable full-screen music player sheet
│   │   │   │       ├── browse/
│   │   │   │       │   ├── BrowseScreen.kt   # File manager interface with storage disks
│   │   │   │       │   └── BrowseViewModel.kt# Directory navigation stack & search
│   │   │   │       ├── components/
│   │   │   │       │   ├── FalconLogo.kt     # Custom vector Canvas drawing of brand falcon
│   │   │   │       │   └── ThumbnailLoader.kt# Memory LruCache video thumbnail bitmap extractor
│   │   │   │       ├── home/
│   │   │   │       │   ├── HomeScreen.kt     # Main container with 5-tab bottom bar & video grid
│   │   │   │       │   ├── HomeViewModel.kt  # Media scanning, sorting, multi-select bulk delete
│   │   │   │       │   ├── DisplaySettingsScreen.kt # Sort & display customization screen
│   │   │   │       │   └── MoreScreen.kt     # Settings, history, and app about dialog
│   │   │   │       ├── player/
│   │   │   │       │   ├── PlayerScreen.kt   # Media3 PlayerView, gestures, overlays, HUDs
│   │   │   │       │   ├── PlayerViewModel.kt# ExoPlayer controller, seek, tracks, speed, queue
│   │   │   │       │   ├── PlayerUiState.kt  # State models, TrackItem, VideoResizeMode
│   │   │   │       │   └── components/
│   │   │   │       │       ├── PlaybackStatusIndicator.kt # Animated status pill
│   │   │   │       │       ├── PlayerBottomControls.kt    # Bottom action bar & speed selector
│   │   │   │       │       ├── PlayerCenterControls.kt    # Centered minimal play/skip cluster
│   │   │   │       │       ├── PlayerControls.kt          # Animated visibility parent overlay
│   │   │   │       │       ├── PlayerSettingsSheet.kt     # Playback speed & info bottom sheet
│   │   │   │       │       ├── PlayerTopBar.kt            # Back button, title, track buttons
│   │   │   │       │       ├── TrackSelectionSheet.kt     # Audio & subtitle selector sheet
│   │   │   │       │       ├── VideoInfoDialog.kt         # Technical video codec metadata dialog
│   │   │   │       │       └── VideoTimeline.kt           # Custom Canvas interactive scrubbing bar
│   │   │   │       └── playlist/
│   │   │   │           ├── PlaylistDetailScreen.kt # Playlist tracks, duration & reordering
│   │   │   │           ├── PlaylistViewModel.kt    # Playlist state & dialog triggers
│   │   │   │           └── components/
│   │   │   │               └── PlaylistDialogs.kt  # Create, Rename, Delete, AddTo dialogs
│   │   │   └── res/                          # XML resources, drawables, launcher icons, themes
│   │   └── test/                             # Unit tests (PlayerUiStateTest.kt)
├── gradle/
│   └── libs.versions.toml                    # Version catalog with pinned dependency versions
├── build.gradle.kts                          # Root project build file
├── settings.gradle.kts                       # Repository management and module inclusion
└── gradle.properties                         # JVM arguments and AndroidX flags
"""
    story.extend(build_code_box(tree_str, styles))
    story.append(Spacer(1, 10))

    story.append(Paragraph("2.2 Prioritized Analysis of the Top 20 Architectural Files", styles['SectionHeading']))
    story.append(Paragraph(
        "To master, maintain, and extend Falcon Player, developers should study these 20 foundational files in the recommended sequence:",
        styles['Body']
    ))

    file_catalog = [
        [Paragraph("Priority & File Path", styles['TableHeader']), Paragraph("Responsibility & Key Members", styles['TableHeader']), Paragraph("Impacted Features & Next Study Target", styles['TableHeader'])],
        [
            Paragraph("<b>#1</b><br/><code>data/VideoRepository.kt</code>", styles['TableCell']),
            Paragraph("Queries MediaStore for video rows; aggregates by bucket/folder; executes scoped storage deletes via <code>createDeleteRequest</code>.", styles['TableCell']),
            Paragraph("Affects video library display, folder browsing, file deletion.<br/><b>Next:</b> <code>HomeViewModel.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#2</b><br/><code>ui/player/PlayerViewModel.kt</code>", styles['TableCell']),
            Paragraph("Manages <code>ExoPlayer</code> instance; track selection (audio/subs); double-tap seeking; speed regulation; position saving.", styles['TableCell']),
            Paragraph("Core video playback engine, controls visibility, resume playback.<br/><b>Next:</b> <code>PlayerScreen.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#3</b><br/><code>ui/player/PlayerScreen.kt</code>", styles['TableCell']),
            Paragraph("Renders Media3 <code>PlayerView</code>; vertical drag gestures for brightness/volume; double-tap seek detection; bottom sheets.", styles['TableCell']),
            Paragraph("Player UI, gesture response, PiP, orientation changes.<br/><b>Next:</b> <code>PlayerControls.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#4</b><br/><code>data/AudioPlaybackManager.kt</code>", styles['TableCell']),
            Paragraph("Singleton ExoPlayer engine for background audio; queue management; audio focus handling; headphone noise disconnection.", styles['TableCell']),
            Paragraph("All audio playback, queue shuffle/repeat, mini player.<br/><b>Next:</b> <code>AudioViewModel.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#5</b><br/><code>ui/home/HomeViewModel.kt</code>", styles['TableCell']),
            Paragraph("Mediates between VideoRepository and HomeScreen; implements search, sorting, folder filtering, and multi-select bulk delete.", styles['TableCell']),
            Paragraph("Video library UI, sorting, search, bulk deletion.<br/><b>Next:</b> <code>HomeScreen.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#6</b><br/><code>ui/home/HomeScreen.kt</code>", styles['TableCell']),
            Paragraph("Main screen scaffold; 5-tab bottom navigation bar; floating action button; selection mode toolbar; list/grid toggle.", styles['TableCell']),
            Paragraph("App home navigation, tab switching, video card list.<br/><b>Next:</b> <code>Navigation.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#7</b><br/><code>Navigation.kt</code>", styles['TableCell']),
            Paragraph("Configures AndroidX Navigation 3; defines <code>NavDisplay</code> and <code>entryProvider</code> mapping NavKeys to Compose screens.", styles['TableCell']),
            Paragraph("Entire application navigation hierarchy and backstack.<br/><b>Next:</b> <code>NavigationKeys.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#8</b><br/><code>NavigationKeys.kt</code>", styles['TableCell']),
            Paragraph("Defines type-safe, serializable navigation routes: <code>Home</code>, <code>PlaylistDetail</code>, and <code>Player</code>.", styles['TableCell']),
            Paragraph("Screen routing parameters and deep links.<br/><b>Next:</b> <code>MainActivity.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#9</b><br/><code>data/AudioRepository.kt</code>", styles['TableCell']),
            Paragraph("Queries MediaStore for audio files; parses artists, albums, and genres; resolves album artwork content URIs.", styles['TableCell']),
            Paragraph("Music library data, artist/album categorization.<br/><b>Next:</b> <code>AudioScreen.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#10</b><br/><code>ui/player/components/VideoTimeline.kt</code>", styles['TableCell']),
            Paragraph("Custom Canvas timeline scrubber; renders elapsed, buffered, and remaining time; handles scrubbing drag gestures.", styles['TableCell']),
            Paragraph("Playback seeking precision and visual progress.<br/><b>Next:</b> <code>PlayerBottomControls.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#11</b><br/><code>ui/player/components/TrackSelectionSheet.kt</code>", styles['TableCell']),
            Paragraph("Modal bottom sheet for selecting audio languages and subtitle tracks; provides instant codec details and disable option.", styles['TableCell']),
            Paragraph("Multi-language audio & subtitle switching.<br/><b>Next:</b> <code>PlayerSettingsSheet.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#12</b><br/><code>data/PlaylistRepository.kt</code>", styles['TableCell']),
            Paragraph("Thread-safe JSON persistence for custom user playlists; validates playlist names; handles reordering and deletions.", styles['TableCell']),
            Paragraph("Playlist persistence, playlist creation & editing.<br/><b>Next:</b> <code>PlaylistViewModel.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#13</b><br/><code>data/BrowseRepository.kt</code>", styles['TableCell']),
            Paragraph("File system scanner; discovers internal storage and removable SD cards; filters playable media files.", styles['TableCell']),
            Paragraph("File browser tab, folder navigation, direct file playback.<br/><b>Next:</b> <code>BrowseScreen.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#14</b><br/><code>ui/components/ThumbnailLoader.kt</code>", styles['TableCell']),
            Paragraph("Global in-memory <code>LruCache</code> for video thumbnail bitmaps; leverages <code>ContentResolver.loadThumbnail</code> with fallbacks.", styles['TableCell']),
            Paragraph("Media card thumbnail rendering performance and memory safety.<br/><b>Next:</b> <code>RealVideoGridCard</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#15</b><br/><code>data/PlaybackPositionRepository.kt</code>", styles['TableCell']),
            Paragraph("Manages Jetpack DataStore Preferences for saving and restoring exact video playback millisecond timestamps.", styles['TableCell']),
            Paragraph("Resume playback functionality across sessions.<br/><b>Next:</b> <code>PlayerViewModel.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#16</b><br/><code>data/MediaItem.kt</code>", styles['TableCell']),
            Paragraph("Fundamental domain entities: <code>VideoItem</code>, <code>AudioItem</code>, <code>FolderItem</code>, <code>ArtistItem</code>, <code>AlbumItem</code>.", styles['TableCell']),
            Paragraph("All data layers and UI representations.<br/><b>Next:</b> <code>VideoRepository.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#17</b><br/><code>ui/audio/components/AudioPlayerSheet.kt</code>", styles['TableCell']),
            Paragraph("Full-screen expandable audio sheet with artwork, queue list, shuffle/repeat toggles, and playback speed pill.", styles['TableCell']),
            Paragraph("Now playing music experience, queue inspection.<br/><b>Next:</b> <code>AudioMiniPlayer.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#18</b><br/><code>ui/home/DisplaySettingsScreen.kt</code>", styles['TableCell']),
            Paragraph("Customization screen allowing users to toggle list/grid layouts, filter favorites, and switch sort orders.", styles['TableCell']),
            Paragraph("User preferences, sorting algorithms, favorites filter.<br/><b>Next:</b> <code>DisplaySettingsRepository.kt</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#19</b><br/><code>MainActivity.kt</code>", styles['TableCell']),
            Paragraph("Activity entry point; enables edge-to-edge layout; configures transient system bars; mounts root Compose tree.", styles['TableCell']),
            Paragraph("App startup, window insets, system bar behavior.<br/><b>Next:</b> <code>AndroidManifest.xml</code>", styles['TableCell'])
        ],
        [
            Paragraph("<b>#20</b><br/><code>ui/components/FalconLogo.kt</code>", styles['TableCell']),
            Paragraph("Pure Compose Canvas implementation of the Falcon brand logo; computes geometric feathers, aerodynamic beak, and eye highlight.", styles['TableCell']),
            Paragraph("Branding aesthetics, vector drawing techniques.<br/><b>Next:</b> <code>Theme.kt</code>", styles['TableCell'])
        ]
    ]

    t_cat = Table(file_catalog, colWidths=[120, 210, 174])
    t_cat.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#2D3748")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_cat)
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 3: PROGRAMMING FOUNDATIONS USED IN FALCON PLAYER
    # =========================================================================
    story.append(Paragraph("Chapter 3  -  Programming Foundations Used in Falcon Player", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("3.1 Core Kotlin Concepts Encountered in the Codebase", styles['SectionHeading']))
    story.append(Paragraph(
        "To develop and modify Falcon Player confidently, you must understand the exact Kotlin programming language features utilized across its components. Rather than abstract theory, each concept below is explained directly through actual project code.",
        styles['Body']
    ))

    # Concept 1: Data Classes
    story.append(Paragraph("Concept 1: Data Classes and Immutability", styles['SubSectionHeading']))
    story.append(Paragraph(
        "A <code>data class</code> in Kotlin is a specialized class designed to hold immutable state. The Kotlin compiler automatically generates <code>equals()</code>, <code>hashCode()</code>, <code>toString()</code>, and the crucial <code>copy()</code> method.",
        styles['Body']
    ))
    code_dc = """// Location: app/src/main/java/com/example/falconplayer/data/MediaItem.kt
data class VideoItem(
    val id: Long,
    val contentUri: Uri,
    val title: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val bucketId: String,
    val bucketName: String,
    val dateAddedSec: Long = 0L,
    val resolutionBadge: String? = null,
    val durationFormatted: String = formatDuration(durationMs)
)"""
    story.extend(build_code_box(code_dc, styles))
    story.append(Paragraph(
        "<b>Line-by-Line Explanation:</b><br/>"
        "• <code>val</code> declares read-only properties, preventing accidental mutation of video records in memory.<br/>"
        "• Default arguments like <code>dateAddedSec = 0L</code> allow callers to instantiate the class without providing every field.<br/>"
        "• <code>durationFormatted</code> uses a computed default value: whenever a <code>VideoItem</code> is created, <code>formatDuration(durationMs)</code> is executed immediately to produce a formatted timestamp like <code>\"04:12\"</code>.<br/>"
        "• <b>Where it appears:</b> <code>MediaItem.kt</code>, <code>Playlist.kt</code>, <code>PlayerUiState.kt</code>, <code>BrowseItem.kt</code>.<br/>"
        "• <b>Student Exercise:</b> Add a new property <code>val fileExtension: String</code> to <code>VideoItem</code> that extracts the lowercase extension from <code>title</code>.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    # Concept 2: Sealed Interfaces & Pattern Matching
    story.append(Paragraph("Concept 2: Sealed Interfaces & Exhaustive Pattern Matching", styles['SubSectionHeading']))
    story.append(Paragraph(
        "A <code>sealed interface</code> restricts hierarchy inheritance to the file in which it is declared. This allows the Kotlin compiler to enforce <b>exhaustive <code>when</code> expressions</b> without requiring an <code>else</code> branch.",
        styles['Body']
    ))
    code_si = """// Location: app/src/main/java/com/example/falconplayer/ui/player/PlayerUiState.kt
sealed interface PlaybackState {
    object Idle : PlaybackState
    object Buffering : PlaybackState
    object Playing : PlaybackState
    object Paused : PlaybackState
    object Ended : PlaybackState
    data class Error(val message: String) : PlaybackState
}

val PlaybackState.statusText: String
    get() = when (this) {
        PlaybackState.Buffering -> "Loading"
        PlaybackState.Playing -> "Playing"
        PlaybackState.Paused, PlaybackState.Idle, PlaybackState.Ended -> "Paused"
        is PlaybackState.Error -> "Error"
    }"""
    story.extend(build_code_box(code_si, styles))
    story.append(Paragraph(
        "<b>Line-by-Line Explanation:</b><br/>"
        "• <code>PlaybackState</code> represents all possible operational states of the ExoPlayer engine.<br/>"
        "• <code>object Idle</code> is a memory-efficient singleton because it carries no dynamic state.<br/>"
        "• <code>data class Error(val message: String)</code> can encapsulate variable payloads such as a decoder exception.<br/>"
        "• In the extension property <code>statusText</code>, the <code>when</code> expression checks every subtype. If a developer adds a new state (e.g. <code>object Stalled</code>) in the future, the compiler will refuse to compile until that state is handled.<br/>"
        "• <b>Where it appears:</b> <code>PlayerUiState.kt</code> (PlaybackState, ControlsState), <code>VideoRepository.kt</code> (DeleteResult), <code>BrowseItem.kt</code> (BrowseNodeItem).<br/>"
        "• <b>Student Exercise:</b> Write a <code>when</code> statement that takes a <code>PlaybackState</code> and returns a boolean indicating whether the center play button should show a Pause icon.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    # Concept 3: Kotlin Coroutines & StateFlow
    story.append(Paragraph("Concept 3: Coroutines, Dispatchers, and StateFlow", styles['SubSectionHeading']))
    story.append(Paragraph(
        "Falcon Player executes all disk and database I/O asynchronously off the main thread using Kotlin Coroutines, and broadcasts observable UI state using <code>StateFlow</code>.",
        styles['Body']
    ))
    code_coro = """// Location: app/src/main/java/com/example/falconplayer/data/VideoRepository.kt
suspend fun getVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
    val videos = mutableListOf<VideoItem>()
    context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null, null, null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            // Read columns and add to list...
        }
    }
    videos
}"""
    story.extend(build_code_box(code_coro, styles))
    story.append(Paragraph(
        "<b>Line-by-Line Explanation:</b><br/>"
        "• <code>suspend</code> marks a function that can pause execution without blocking the calling thread.<br/>"
        "• <code>withContext(Dispatchers.IO)</code> switches execution to an optimized thread pool dedicated to disk and database operations, keeping the UI perfectly responsive at 120 FPS.<br/>"
        "• <code>cursor.use { }</code> is an extension function that automatically closes the <code>Cursor</code> when the block finishes or throws an exception, preventing memory leaks.<br/>"
        "• <b>Student Exercise:</b> Trace how <code>HomeViewModel.loadMedia()</code> calls <code>videoRepository.getVideos()</code> inside <code>viewModelScope.launch</code> and updates <code>_uiState.update { it.copy(...) }</code>.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 4: ANDROID FUNDAMENTALS THROUGH FALCON PLAYER
    # =========================================================================
    story.append(Paragraph("Chapter 4  -  Android Fundamentals Through Falcon Player", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("4.1 Manifest Declarations & Hardware Configuration", styles['SectionHeading']))
    story.append(Paragraph(
        "The <code>AndroidManifest.xml</code> defines the contract between Falcon Player and the Android operating system. Inspecting the actual manifest reveals key architectural decisions:",
        styles['Body']
    ))
    code_manifest = """<!-- Location: app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />
    <uses-permission android:name="android.permission.MANAGE_MEDIA" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".FalconPlayerApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.FalconPlayer">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:supportsPictureInPicture="true"
            android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>"""
    story.extend(build_code_box(code_manifest, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("4.2 Why configChanges is Critical for Video Playback", styles['SectionHeading']))
    story.append(Paragraph(
        "Notice the attribute: <code>android:configChanges=\"screenSize|smallestScreenSize|screenLayout|orientation\"</code>. In standard Android applications, rotating the device from portrait to landscape destroys and recreates the entire <code>Activity</code>. In a high-end video player, recreating the Activity would cause the video decoder to reset, stutter, re-buffer, and lose surface rendering.",
        styles['Body']
    ))
    story.append(Paragraph(
        "By declaring these <code>configChanges</code>, Falcon Player instructs Android to <b>retain the existing Activity instance</b> during orientation and layout shifts. The Jetpack Compose UI seamlessly recalculates its layout constraints, while the underlying ExoPlayer engine continues decoding without dropping a single frame.",
        styles['Body']
    ))

    story.append(Paragraph("4.3 Immersive Fullscreen and Window Insets", styles['SectionHeading']))
    story.append(Paragraph(
        "In <code>MainActivity.kt</code>, Falcon Player suppresses the Android system bars (status bar and navigation pill) to give videos an edge-to-edge canvas:",
        styles['Body']
    ))
    code_insets = """// Location: app/src/main/java/com/example/falconplayer/MainActivity.kt
enableEdgeToEdge()

val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
windowInsetsController.systemBarsBehavior = 
    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())"""
    story.extend(build_code_box(code_insets, styles))
    story.append(Paragraph(
        "<b>Behavioral Impact:</b> <code>BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE</code> allows the user to swipe from the screen edge to reveal transient system bars that automatically fade out after a brief interval, ensuring that playback controls remain undisturbed.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 5: APPLICATION STARTUP AND NAVIGATION
    # =========================================================================
    story.append(Paragraph("Chapter 5  -  Application Startup and Navigation", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("5.1 AndroidX Navigation 3 Architecture", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player utilizes the latest <b>AndroidX Navigation 3</b> framework (<code>androidx.navigation3:navigation3-runtime:1.0.1</code> and <code>navigation3-ui</code>). Unlike legacy Navigation Compose which relied on fragile string path concatenation (such as <code>\"player/{videoId}?title={title}\"</code>), Navigation 3 is built upon type-safe, compile-time verified <b>NavKey</b> objects.",
        styles['Body']
    ))

    code_navkeys = """// Location: app/src/main/java/com/example/falconplayer/NavigationKeys.kt
package com.example.falconplayer

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable 
data object Home : NavKey

@Serializable 
data class PlaylistDetail(val playlistId: String) : NavKey

@Serializable 
data class Player(
    val videoUri: String? = null,
    val title: String? = null,
    val playlistId: String? = null,
    val playlistIndex: Int = 0
) : NavKey"""
    story.extend(build_code_box(code_navkeys, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("5.2 Navigation Controller & NavDisplay Implementation", styles['SectionHeading']))
    story.append(Paragraph(
        "In <code>Navigation.kt</code>, the <code>MainNavigation</code> Composable initializes the navigation stack and registers the screen entry providers:",
        styles['Body']
    ))

    code_nav = """// Location: app/src/main/java/com/example/falconplayer/Navigation.kt
@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onPlayMedia = { uri, title ->
                        backStack.add(Player(videoUri = uri?.toString(), title = title))
                    },
                    onOpenPlaylist = { playlistId ->
                        backStack.add(PlaylistDetail(playlistId = playlistId))
                    }
                )
            }
            entry<PlaylistDetail> { key ->
                PlaylistDetailScreen(
                    playlistId = key.playlistId,
                    onBackClick = { backStack.removeLastOrNull() },
                    onPlayVideoInPlaylist = { uri, title, playlistId, index ->
                        backStack.add(Player(uri?.toString(), title, playlistId, index))
                    }
                )
            }
            entry<Player> { key ->
                PlayerScreen(
                    onBackClick = { backStack.removeLastOrNull() },
                    initialVideoUri = key.videoUri?.let { Uri.parse(it) },
                    initialTitle = key.title,
                    initialPlaylistId = key.playlistId,
                    initialPlaylistIndex = key.playlistIndex
                )
            }
        }
    )
}"""
    story.extend(build_code_box(code_nav, styles))
    story.append(Spacer(1, 8))

    story.append(Paragraph("5.3 Complete Sequence Diagram: Video Selection to Playback", styles['SectionHeading']))
    seq_diagram = """
User                 HomeScreen               backStack          NavDisplay         PlayerScreen        PlayerViewModel
 |                       |                        |                  |                   |                     |
 |-- Tap Video Card ---->|                        |                  |                   |                     |
 |                       |-- backStack.add(Player)->                 |                   |                     |
 |                       |                        |-- Push Key ----->|                   |                     |
 |                       |                        |                  |-- Mount Screen -->|                     |
 |                       |                        |                  |                   |-- loadVideo(uri) -->|
 |                       |                        |                  |                   |                     |-- MediaItem.fromUri
 |                       |                        |                  |                   |                     |-- player.prepare()
 |                       |                        |                  |                   |                     |-- seekTo(savedPos)
 |                       |                        |                  |                   |                     |-- player.play()
 |                       |                        |                  |                   |<-- Buffering -------|
 |                       |                        |                  |<-- Playing -------|                     |
 |<-- Render Video Frame ----------------------------------------------------------------+                     |
"""
    story.extend(build_code_box(seq_diagram, styles))
    story.append(Spacer(1, 10))
    story.append(PageBreak())

    return story
