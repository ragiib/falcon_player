from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
from scratch.pdf_builder import build_callout, build_code_box

def get_chapters_part4(styles):
    story = []

    # =========================================================================
    # CHAPTER 16: DEBUGGING AND TROUBLESHOOTING
    # =========================================================================
    story.append(Paragraph("Chapter 16  -  Debugging and Troubleshooting", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("16.1 Logcat Diagnostic Tagging Strategy", styles['SectionHeading']))
    story.append(Paragraph(
        "Falcon Player incorporates structured logging across all data and playback components. When diagnosing runtime issues in Android Studio Logcat, use these specific package tags:",
        styles['Body']
    ))
    story.append(Paragraph(
        "• <code>tag:VideoRepository</code>: Traces MediaStore queries, row counts, and deletion result codes.<br/>"
        "• <code>tag:AudioRepository</code>: Monitors music scanning, album art URI resolution, and deduplication.<br/>"
        "• <code>tag:AudioPlaybackManager</code>: Logs audio playback state, queue indices, audio focus gains/losses.<br/>"
        "• <code>tag:PlaylistRepository</code>: Inspects JSON load/save operations, validation rejections, and mutex locks.<br/>"
        "• <code>tag:BrowseRepository</code>: Tracks file counting, secondary SD card detection, and permission checks.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("16.2 Common Technical Issues & Systematic Resolution Procedures", styles['SectionHeading']))
    
    trouble_data = [
        [Paragraph("Symptom / Failure", styles['TableHeader']), Paragraph("Probable Root Cause", styles['TableHeader']), Paragraph("Logical Debugging & Fix Procedure", styles['TableHeader'])],
        [
            Paragraph("<b>Playback Error Dialog appears immediately on opening a video</b>", styles['TableCellBold']),
            Paragraph("Video codec unsupported by device hardware (e.g. 10-bit AV1 on older SoC) or file URI permission revoked.", styles['TableCell']),
            Paragraph("Inspect <code>onPlayerError(error: PlaybackException)</code> in <code>PlayerViewModel.kt</code>. Check Logcat for <code>MediaCodecRenderer$DecoderInitializationException</code>. Verify content URI validity.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Delete fails on Android 11+ without prompt</b>", styles['TableCellBold']),
            Paragraph("Attempting direct file deletion instead of launching the OS <code>IntentSender</code>.", styles['TableCell']),
            Paragraph("Ensure <code>HomeViewModel.deleteVideo()</code> emits <code>deleteIntentSender</code> and that <code>HomeScreen</code>'s <code>rememberLauncherForActivityResult</code> receives and launches the <code>IntentSenderRequest</code>.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Screen stays locked in landscape after exiting player</b>", styles['TableCellBold']),
            Paragraph("Activity orientation override not reset during navigation transition.", styles['TableCell']),
            Paragraph("Verify that <code>PlayerScreen.kt</code> includes <code>DisposableEffect(Unit) { onDispose { activity.requestedOrientation = SCREEN_ORIENTATION_UNSPECIFIED } }</code>.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Mini player does not pause when headphones disconnected</b>", styles['TableCellBold']),
            Paragraph("<code>setHandleAudioBecomingNoisy</code> disabled or audio attributes missing.", styles['TableCell']),
            Paragraph("Check <code>AudioPlaybackManager.kt</code> line 57: confirm <code>setHandleAudioBecomingNoisy(true)</code> is invoked on the ExoPlayer builder.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Video thumbnails fail to load or cause OutOfMemoryError</b>", styles['TableCellBold']),
            Paragraph("Thumbnail bitmap allocation exceeding heap; LruCache size incorrectly calculated.", styles['TableCell']),
            Paragraph("Check <code>ThumbnailLoader.kt</code>: ensure <code>cacheSize = maxMemory / 8</code> and that <code>sizeOf</code> correctly calculates <code>bitmap.byteCount / 1024</code>.", styles['TableCell'])
        ]
    ]
    t_trouble = Table(trouble_data, colWidths=[130, 160, 204])
    t_trouble.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#2D3748")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_trouble)
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 17: SOURCE-CODE REFERENCE
    # =========================================================================
    story.append(Paragraph("Chapter 17  -  Source-Code Reference", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("17.1 Core Class & Method Reference", styles['SectionHeading']))
    story.append(Paragraph(
        "This section documents the signatures, responsibilities, and call flows of the application's most critical functions:",
        styles['Body']
    ))

    story.append(Paragraph("<b>1. VideoRepository.getVideos(): List&lt;VideoItem&gt;</b>", styles['SubSectionHeading']))
    story.append(Paragraph(
        "• <b>Location:</b> <code>com.example.falconplayer.data.VideoRepository</code><br/>"
        "• <b>Parameters:</b> None. Runs asynchronously on <code>Dispatchers.IO</code>.<br/>"
        "• <b>Returns:</b> A list of fully populated <code>VideoItem</code> domain models.<br/>"
        "• <b>Called by:</b> <code>HomeViewModel.loadMedia()</code>, <code>PlaylistViewModel.getPlaylistDetailState()</code>.<br/>"
        "• <b>Calls:</b> <code>ContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ...)</code>, <code>calculateResolutionBadge()</code>.<br/>"
        "• <b>Side Effects:</b> Reads media database. No disk writes.<br/>"
        "• <b>Failure Consequence:</b> If modified incorrectly, the home video library will display empty or crash due to null column indices.",
        styles['Body']
    ))
    story.append(Spacer(1, 6))

    story.append(Paragraph("<b>2. VideoRepository.deleteVideo(videoUri: Uri): DeleteResult</b>", styles['SubSectionHeading']))
    story.append(Paragraph(
        "• <b>Location:</b> <code>com.example.falconplayer.data.VideoRepository</code><br/>"
        "• <b>Parameters:</b> <code>videoUri: Uri</code>  -  Content URI of the video to delete.<br/>"
        "• <b>Returns:</b> <code>DeleteResult.Success</code>, <code>NeedsPermission(IntentSender)</code>, or <code>Failure(reason)</code>.<br/>"
        "• <b>Called by:</b> <code>HomeViewModel.deleteVideo(video)</code>.<br/>"
        "• <b>Calls:</b> <code>MediaStore.createDeleteRequest</code> (API 30+) or <code>ContentResolver.delete</code>.<br/>"
        "• <b>Failure Consequence:</b> Deletions on Android 11+ will fail silently or throw security exceptions.",
        styles['Body']
    ))
    story.append(Spacer(1, 6))

    story.append(Paragraph("<b>3. PlayerViewModel.triggerDoubleTapSeek(isForward: Boolean)</b>", styles['SubSectionHeading']))
    story.append(Paragraph(
        "• <b>Location:</b> <code>com.example.falconplayer.ui.player.PlayerViewModel</code><br/>"
        "• <b>Parameters:</b> <code>isForward: Boolean</code>  -  <code>true</code> for forward (+10s), <code>false</code> for rewind (-10s).<br/>"
        "• <b>Side Effects:</b> Immediately updates <code>player.seekTo</code>, aggregates <code>doubleTapSeekAccumulatedMs</code>, emits visual <code>SeekFeedback</code> to <code>_uiState</code>, and cancels/restarts the 900ms reset timer.<br/>"
        "• <b>Called by:</b> <code>PlayerScreen.kt</code> pointer gesture tap detector.",
        styles['Body']
    ))
    story.append(Spacer(1, 6))

    story.append(Paragraph("<b>4. AudioPlaybackManager.playQueue(tracks: List&lt;AudioItem&gt;, startIndex: Int)</b>", styles['SubSectionHeading']))
    story.append(Paragraph(
        "• <b>Location:</b> <code>com.example.falconplayer.data.AudioPlaybackManager</code><br/>"
        "• <b>Parameters:</b> <code>tracks: List&lt;AudioItem&gt;</code>, <code>startIndex: Int = 0</code>.<br/>"
        "• <b>Side Effects:</b> Clears existing player queue, loads target media URI, starts playback, launches 250ms progress polling coroutine.",
        styles['Body']
    ))
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 18: PROJECT-BASED LEARNING COURSE
    # =========================================================================
    story.append(Paragraph("Chapter 18  -  Project-Based Learning Course", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("18.1 Course Structure: From Novice to Architect", styles['SectionHeading']))
    story.append(Paragraph(
        "This project-based curriculum teaches you Android development by exploring, experimenting with, and extending Falcon Player through 6 progressive stages:",
        styles['Body']
    ))

    stages_data = [
        [Paragraph("Stage", styles['TableHeader']), Paragraph("Learning Focus & Modules", styles['TableHeader']), Paragraph("Practical Exercise & Verification", styles['TableHeader'])],
        [
            Paragraph("<b>Stage A</b><br/>Orientation", styles['TableCellBold']),
            Paragraph("Understand module layout, Gradle catalog, Manifest permissions, and entry points.", styles['TableCell']),
            Paragraph("<b>Read-Only:</b> Trace execution from <code>FalconPlayerApp</code> to <code>MainActivity</code> and <code>MainNavigation</code>.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Stage B</b><br/>UI & Compose", styles['TableCellBold']),
            Paragraph("Understand Compose layout trees, state hoisting, colors, and design tokens.", styles['TableCell']),
            Paragraph("<b>Code Change:</b> Modify <code>FalconRed</code> in <code>Color.kt</code> and observe how all accent controls update dynamically.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Stage C</b><br/>Media Discovery", styles['TableCellBold']),
            Paragraph("Learn ContentResolver, MediaStore cursors, and scoped storage rules.", styles['TableCell']),
            Paragraph("<b>Code Change:</b> Add a new filter in <code>HomeViewModel.kt</code> that only shows videos longer than 60 seconds.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Stage D</b><br/>Playback Engine", styles['TableCellBold']),
            Paragraph("ExoPlayer initialization, MediaItem creation, playback state listeners.", styles['TableCell']),
            Paragraph("<b>Code Change:</b> Add a 3.0x speed option to <code>PlayerSettingsSheet.kt</code>.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Stage E</b><br/>Gestures & Tracks", styles['TableCellBold']),
            Paragraph("Custom Canvas drawing, drag gesture calculation, audio track switching.", styles['TableCell']),
            Paragraph("<b>Code Change:</b> Change double-tap seek delta from 10 seconds to 15 seconds in <code>PlayerViewModel.kt</code>.", styles['TableCell'])
        ],
        [
            Paragraph("<b>Stage F</b><br/>Independent Dev", styles['TableCellBold']),
            Paragraph("Create new features, manage DataStore preferences, debug with Logcat.", styles['TableCell']),
            Paragraph("<b>Capstone:</b> Build an 'A-B Loop' feature that repeats a video between two selected timestamps.", styles['TableCell'])
        ]
    ]
    t_stage = Table(stages_data, colWidths=[90, 200, 204])
    t_stage.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#E50914")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_stage)
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 19: FEATURE STATUS AND TECHNICAL DEBT
    # =========================================================================
    story.append(Paragraph("Chapter 19  -  Feature Status and Technical Debt", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("19.1 Feature Implementation Inventory", styles['SectionHeading']))
    story.append(Paragraph(
        "Based on systematic source code inspection across all directories, the following table presents the confirmed implementation status of Falcon Player features:",
        styles['Body']
    ))

    debt_data = [
        [Paragraph("Feature / Component", styles['TableHeader']), Paragraph("Status in Codebase", styles['TableHeader']), Paragraph("Technical Evidence & Notes", styles['TableHeader'])],
        [Paragraph("Hardware Video Playback", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("Media3 ExoPlayer 1.7.0 in <code>PlayerViewModel</code> and <code>PlayerScreen</code>.", styles['TableCell'])],
        [Paragraph("Double-Tap Accumulating Seek", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("<code>triggerDoubleTapSeek</code> in <code>PlayerViewModel.kt</code> with 900ms window.", styles['TableCell'])],
        [Paragraph("Audio & Subtitle Track Switching", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("<code>TrackSelectionSheet.kt</code> using <code>TrackSelectionOverride</code>.", styles['TableCell'])],
        [Paragraph("Single & Bulk Video Delete", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("Scoped storage <code>createDeleteRequest</code> in <code>VideoRepository.kt</code>.", styles['TableCell'])],
        [Paragraph("Audio Background Engine & Focus", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("<code>AudioPlaybackManager.kt</code> with <code>setHandleAudioBecomingNoisy</code>.", styles['TableCell'])],
        [Paragraph("Custom Video Playlists", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("JSON storage, reordering, creation, and deletion in <code>PlaylistRepository.kt</code>.", styles['TableCell'])],
        [Paragraph("File System Explorer (Browse)", styles['TableCellBold']), Paragraph("Implemented & Confirmed", styles['TableCell']), Paragraph("Java File scanning with SD card detection in <code>BrowseRepository.kt</code>.", styles['TableCell'])],
        [Paragraph("Media Notification Service", styles['TableCellBold']), Paragraph("Partially Implemented", styles['TableCell']), Paragraph("Permission declared in manifest; session bindings present in manager; notification UI can be extended.", styles['TableCell'])],
        [Paragraph("External Subtitle (.srt) File Import", styles['TableCellBold']), Paragraph("Not Implemented", styles['TableCell']), Paragraph("Currently discovers container-embedded subtitle tracks; external file picker is absent.", styles['TableCell'])],
        [Paragraph("Equalizer / Audio DSP Effects", styles['TableCellBold']), Paragraph("Not Implemented", styles['TableCell']), Paragraph("No audio equalizer DSP UI or AudioEffect integration in current codebase.", styles['TableCell'])],
    ]
    t_debt = Table(debt_data, colWidths=[150, 120, 224])
    t_debt.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#2D3748")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_debt)
    story.append(PageBreak())

    # =========================================================================
    # CHAPTER 20: GLOSSARY, FAQ, AND FINAL REFERENCE
    # =========================================================================
    story.append(Paragraph("Chapter 20  -  Glossary, FAQ, and Final Reference", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("20.1 Technical Glossary", styles['SectionHeading']))
    story.append(Paragraph(
        "• <b>ExoPlayer:</b> An application-level media player for Android, now part of AndroidX Media3, that supports DASH, HLS, SmoothStreaming, and extensive local container formats.<br/>"
        "• <b>Media3:</b> Google's unified media library combining ExoPlayer, media sessions, and UI controls under <code>androidx.media3</code>.<br/>"
        "• <b>Scoped Storage:</b> Android's privacy framework restricting direct file path access and requiring <code>MediaStore</code> APIs or <code>IntentSender</code> confirmation to delete files.<br/>"
        "• <b>NavKey:</b> An AndroidX Navigation 3 type-safe serializable destination identifier replacing legacy URL strings.<br/>"
        "• <b>StateFlow:</b> A state-holder observable flow from Kotlin Coroutines that emits current and updated state to collectors.",
        styles['Body']
    ))
    story.append(Spacer(1, 8))

    story.append(Paragraph("20.2 50 Essential Project-Specific Questions & Answers", styles['SectionHeading']))
    story.append(Paragraph(
        "The following 50 questions and answers are derived directly from the real Falcon Player codebase to serve as an authoritative technical reference:",
        styles['Body']
    ))

    # We will generate 50 structured Q&As based on actual files
    faqs = [
        ("Q1: What is the root Application class of Falcon Player and what annotation does it require?",
         "The root application class is FalconPlayerApp (located at app/src/main/java/com/example/falconplayer/FalconPlayerApp.kt). It extends Application() and requires the @HiltAndroidApp annotation to trigger Dagger Hilt's code generation for dependency injection."),
        
        ("Q2: Which activity serves as the application entry point?",
         "MainActivity (com.example.falconplayer.MainActivity), which extends ComponentActivity and is annotated with @AndroidEntryPoint."),
        
        ("Q3: How does Falcon Player achieve an immersive fullscreen mode on startup?",
         "Inside MainActivity.onCreate(), it calls WindowCompat.getInsetsController(window, window.decorView), configures systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE, and calls hide(systemBars())."),
        
        ("Q4: Which navigation library does Falcon Player use?",
         "AndroidX Navigation 3 (specifically androidx.navigation3:navigation3-runtime:1.0.1 and navigation3-ui), utilizing NavDisplay and rememberNavBackStack."),
        
        ("Q5: What are the three NavKey navigation routes defined in the project?",
         "Home (data object Home : NavKey), PlaylistDetail(val playlistId: String), and Player(val videoUri: String?, val title: String?, val playlistId: String?, val playlistIndex: Int)."),
        
        ("Q6: Where is the Media3 ExoPlayer instance created for video playback?",
         "Inside PlayerViewModel (com.example.falconplayer.ui.player.PlayerViewModel), initialized as 'val player: ExoPlayer = ExoPlayer.Builder(context).build()'."),
        
        ("Q7: How are custom Compose controls overlaid on top of the video surface?",
         "In PlayerScreen.kt, a Box container places AndroidView(PlayerView) at the lowest z-index, followed by gesture interceptors, HUD layers, and PlayerControls."),
        
        ("Q8: Why is PlayerView.useController set to false?",
         "To disable Media3's default XML-based playback controls, allowing Falcon Player's custom Compose controls (PlayerControls.kt) to manage the UI."),
        
        ("Q9: How does double-tap seeking accumulate seek time across multiple taps?",
         "In PlayerViewModel.triggerDoubleTapSeek(), doubleTapSeekAccumulatedMs is incremented by 10,000L per tap in the same direction, emitting total seconds to seekFeedback. A 900ms delay resets the accumulator."),
        
        ("Q10: How does Falcon Player prevent permanent landscape lock when exiting the player?",
         "PlayerScreen.kt uses DisposableEffect(Unit) whose onDispose block resets activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED."),
        
        ("Q11: How does Falcon Player adjust screen brightness during playback?",
         "Vertical drag gestures on the left 45% of PlayerScreen modify activity.window.attributes.screenBrightness between 0.01f and 1.0f."),
        
        ("Q12: How does Falcon Player adjust audio volume during video playback?",
         "Vertical drag gestures on the right 55% of PlayerScreen adjust AudioManager.STREAM_MUSIC via audioManager.setStreamVolume()."),
        
        ("Q13: Which aspect ratio modes does Falcon Player support?",
         "VideoResizeMode.FIT (RESIZE_MODE_FIT), ZOOM (RESIZE_MODE_ZOOM), and STRETCH (RESIZE_MODE_FILL) via AspectRatioFrameLayout."),
        
        ("Q14: How are video tracks (audio and subtitles) discovered by the player?",
         "PlayerViewModel registers an onTracksChanged(tracks: Tracks) listener on ExoPlayer, iterating through track groups and checking C.TRACK_TYPE_AUDIO and C.TRACK_TYPE_TEXT."),
        
        ("Q15: How are embedded subtitle formats identified in Falcon Player?",
         "In PlayerViewModel.formatSubtitleTechnicalInfo(), format.sampleMimeType is inspected for WebVTT, SRT (subrip), ASS/SSA, PGS, DVB, and TX3G."),
        
        ("Q16: How does Falcon Player disable subtitles completely?",
         "PlayerViewModel.disableSubtitles() calls player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).clearOverridesOfType(C.TRACK_TYPE_TEXT).build()."),
        
        ("Q17: How is playback position saved across app sessions?",
         "PlaybackPositionRepository uses Jetpack DataStore Preferences (name = 'playback_positions'), storing longPreferencesKey(videoUri) to positionMs."),
        
        ("Q18: Under what condition is playback position NOT saved?",
         "If the video duration is zero or the current position is within 5 seconds of the video end, position saving is skipped to prevent resuming at the credits."),
        
        ("Q19: How are video thumbnails extracted and cached?",
         "ThumbnailLoader.kt uses a global LruCache sized to maxMemory / 8. On Android 10+ it uses ContentResolver.loadThumbnail(Size(320, 200)), with MediaMetadataRetriever as fallback."),
        
        ("Q20: What columns are queried from MediaStore for video files?",
         "_ID, DISPLAY_NAME, TITLE, DURATION, WIDTH, HEIGHT, SIZE, BUCKET_ID, BUCKET_DISPLAY_NAME, DATA, and DATE_ADDED in VideoRepository.kt."),
        
        ("Q21: How does Falcon Player group videos into folders?",
         "VideoRepository.getFolders() groups scanned VideoItem records by bucketId (videos.groupBy { it.bucketId }) and maps them to FolderItem."),
        
        ("Q22: How does Falcon Player handle video deletion on Android 11+ (API 30+)?",
         "VideoRepository.deleteVideo() calls MediaStore.createDeleteRequest(context.contentResolver, listOf(videoUri)).intentSender and returns DeleteResult.NeedsPermission."),
        
        ("Q23: How does Falcon Player handle video deletion on Android 10 (API 29)?",
         "It attempts contentResolver.delete(), catches android.app.RecoverableSecurityException, and extracts e.userAction.actionIntent.intentSender."),
        
        ("Q24: How does Falcon Player perform atomic bulk video deletion?",
         "HomeViewModel.deleteSelectedVideos() passes selected URIs to VideoRepository.deleteVideos(), which calls MediaStore.createDeleteRequest with the entire list on Android 11+."),
        
        ("Q25: How does the user activate selection mode in HomeScreen?",
         "By long-pressing any video card via combinedClickable(onLongClick = { viewModel.enterSelectionModeWithVideo(video.id) })."),
        
        ("Q26: Where are user-created playlists stored?",
         "Inside context.filesDir / 'falcon_playlists.json', serialized as JSON using Kotlinx Serialization with a Mutex for thread safety."),
        
        ("Q27: What validation rules are applied when creating or renaming a playlist?",
         "PlaylistRepository.validatePlaylistName() requires non-empty names, limits length to 50 characters, and enforces case-insensitive uniqueness."),
        
        ("Q28: How does drag-and-drop reordering work in PlaylistRepository?",
         "PlaylistRepository.moveVideoInPlaylist() removes the item at fromIndex, inserts it at toIndex, and persists the reordered list to JSON."),
        
        ("Q29: How does Falcon Player discover audio files in AudioRepository?",
         "It queries MediaStore.Audio.Media.EXTERNAL_CONTENT_URI with a filter including IS_MUSIC != 0, MIME_TYPE 'audio/%', common extensions (.mp3, .flac, .m4a...), and duration >= 1000ms."),
        
        ("Q30: How are audio tracks grouped in AudioScreen?",
         "Into 4 tabs: Artists (grouped by artist), Albums (grouped by albumId/album), Tracks (sorted by title), and Genres (grouped by genre name)."),
        
        ("Q31: How is audio playback managed separately from video?",
         "Via AudioPlaybackManager, a singleton class holding an ExoPlayer instance that remains active across navigation tab switches."),
        
        ("Q32: How does Falcon Player automatically handle headphone disconnection?",
         "AudioPlaybackManager sets setHandleAudioBecomingNoisy(true) on its ExoPlayer instance, automatically pausing audio when headphones unplug."),
        
        ("Q33: How does Falcon Player handle Audio Focus for music playback?",
         "AudioPlaybackManager sets AudioAttributes with USAGE_MEDIA and CONTENT_TYPE_MUSIC and passes handleAudioFocus = true to setAudioAttributes."),
        
        ("Q34: How does shuffle work in AudioPlaybackManager without interrupting the current track?",
         "AudioPlaybackManager.toggleShuffle() keeps the current track at index 0 and shuffles only the remaining tracks from originalQueue."),
        
        ("Q35: What repeat modes are supported in AudioPlaybackManager?",
         "AudioRepeatMode.OFF (stops at end), AudioRepeatMode.ALL (loops back to track 0), and AudioRepeatMode.ONE (seeks to 0 and replays current track)."),
        
        ("Q36: What is the AudioMiniPlayer?",
         "A floating Composable bar anchored above the bottom NavigationBar in HomeScreen that displays current song title, artist, album art, play/pause, next, and a progress bar."),
        
        ("Q37: What is the BrowseScreen?",
         "A file manager screen in Falcon Player that allows users to explore device storage directories directly, with quick access to Download, Movies, and Music folders."),
        
        ("Q38: How does BrowseRepository detect secondary storage (SD cards)?",
         "By scanning subdirectories in '/storage' for readable directories other than 'emulated' and 'self'."),
        
        ("Q39: What is Incognito Mode in Falcon Player?",
         "A toggleable mode stored in falcon_display_settings.json. When active, HistoryRepository.recordVideoPlayed() skips recording video playback entirely."),
        
        ("Q40: What sorting options are supported in DisplaySettingsRepository?",
         "SortType: NAME_ASC, NAME_DESC, LENGTH_ASC, LENGTH_DESC, ADDED_ASC, ADDED_DESC, TRACKS_DESC, TRACKS_ASC, INSERTION_ASC, INSERTION_DESC."),
        
        ("Q41: How does Falcon Player toggle between Grid and List video layouts?",
         "DisplaySettingsRepository stores isListView. HomeScreen dynamically switches between RealVideoGridCard and RealVideoListCard."),
        
        ("Q42: What is the primary brand accent color of Falcon Player?",
         "FalconRed (Color(0xFFE50914)), inspired by high-contrast crimson aesthetics."),
        
        ("Q43: How is the Falcon brand logo drawn?",
         "Via FalconLogo.kt using Compose Canvas, drawing 3 wing feathers, a gradient aerodynamic beak, crest highlight, and a piercing white eye."),
        
        ("Q44: What minimum Android SDK version does Falcon Player require?",
         "minSdk = 24 (Android 7.0 Nougat)."),
        
        ("Q45: What target and compile SDK versions are configured?",
         "compileSdk = 36 and targetSdk = 36."),
        
        ("Q46: Which Java toolchain version is used?",
         "Java 17 (jvmToolchain(17))."),
        
        ("Q47: What dependency injection framework is used?",
         "Dagger Hilt (com.google.dagger:hilt-android:2.60.1) processed via KSP (Kotlin Symbol Processing)."),
        
        ("Q48: How does Picture-in-Picture (PiP) work in PlayerScreen?",
         "When user taps the PiP button, context.enterPictureInPictureMode(PictureInPictureParams.Builder().build()) is called on Android 8.0+ (API 26+)."),
        
        ("Q49: How are unit tests structured in the project?",
         "In app/src/test/java/com/example/falconplayer/ui/player/PlayerUiStateTest.kt, testing default media info initialization and PlaybackState.statusText mappings."),
        
        ("Q50: How do you build the debug APK using Gradle wrapper?",
         "Run './gradlew assembleDebug'. The APK is generated at 'app/build/outputs/apk/debug/app-debug.apk'.")
    ]

    for q, a in faqs:
        p_q = Paragraph(f"<b>{q}</b>", styles['SubSectionHeading'])
        p_a = Paragraph(a, styles['Body'])
        story.append(KeepTogether([p_q, p_a]))
        story.append(Spacer(1, 4))

    story.append(PageBreak())

    # Final Reference & Roadmap
    story.append(Paragraph("20.3 Long-Term Developer Mastery Roadmap", styles['SectionHeading']))
    story.append(Paragraph(
        "To advance your mastery of Android systems engineering using Falcon Player, proceed through these 4 architectural milestones:<br/>"
        "• <b>Phase 1: MediaCodec & ExoPlayer Internals:</b> Study ExoPlayer's <code>MediaCodecRenderer</code> and <code>DefaultRenderersFactory</code> to understand how hardware buffers flow from decoders to SurfaceViews.<br/>"
        "• <b>Phase 2: Custom DataSource & Caching:</b> Extend Media3 with <code>CacheDataSource</code> and <code>SimpleCache</code> to support streaming video playback with offline chunk caching.<br/>"
        "• <b>Phase 3: Foreground Media Session:</b> Upgrade <code>AudioPlaybackManager</code> to a full AndroidX <code>MediaSessionService</code> providing lock-screen media notifications and Android Auto support.<br/>"
        "• <b>Phase 4: Room Database Migration:</b> Transition <code>PlaylistRepository</code> from JSON files to an encrypted SQLite Room database with foreign key cascades and reactive Paging 3 queries.",
        styles['Body']
    ))

    return story
