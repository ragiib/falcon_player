import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT

# Add current workspace to path
sys.path.insert(0, os.path.abspath("."))

from scratch.pdf_builder import NumberedCanvas, create_styles, build_callout
from scratch.chapters_part1 import get_chapters_part1
from scratch.chapters_part2 import get_chapters_part2
from scratch.chapters_part3 import get_chapters_part3
from scratch.chapters_part4 import get_chapters_part4

def build_pdf():
    pdf_filename = os.path.join("docs", "Falcon_Player_Complete_Project_Documentation.pdf")
    os.makedirs(os.path.dirname(pdf_filename), exist_ok=True)

    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = create_styles()
    story = []

    # =========================================================================
    # COVER PAGE
    # =========================================================================
    story.append(Spacer(1, 40))
    story.append(Paragraph("FALCON PLAYER", styles['CoverTitle']))
    story.append(HRFlowable(width="60%", thickness=3, color=colors.HexColor("#E50914"), spaceBefore=4, spaceAfter=18))
    
    story.append(Paragraph("Complete Project Architecture, Engineering Reference &amp;<br/>Project-Based Learning Textbook", styles['CoverSubtitle']))
    story.append(Spacer(1, 24))

    cover_box_content = [
        Paragraph("<b>Target Codebase:</b> <code>com.example.falconplayer</code>", styles['CalloutText']),
        Paragraph("<b>Platform &amp; Engine:</b> Android SDK 36 • AndroidX Media3 (ExoPlayer 1.7.0) • Jetpack Compose Material 3", styles['CalloutText']),
        Paragraph("<b>Architecture Pattern:</b> Unidirectional Data Flow (UDF) • MVVM • Dagger Hilt DI • AndroidX Navigation 3", styles['CalloutText']),
        Paragraph("<b>Author / Role:</b> Senior Android Systems Architect &amp; Technical Writer", styles['CalloutText']),
        Paragraph("<b>Document Classification:</b> Complete Technical Specification &amp; Step-by-Step Developer Manual", styles['CalloutText'])
    ]
    t_box = Table([[cover_box_content]], colWidths=[letter[0] - 108])
    t_box.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#CBD5E1")),
        ('LINELEFT', (0,0), (-1,-1), 4, colors.HexColor("#E50914")),
        ('TOPPADDING', (0,0), (-1,-1), 14),
        ('BOTTOMPADDING', (0,0), (-1,-1), 14),
        ('LEFTPADDING', (0,0), (-1,-1), 16),
        ('RIGHTPADDING', (0,0), (-1,-1), 16),
    ]))
    story.append(t_box)

    story.append(Spacer(1, 60))
    story.append(Paragraph("A comprehensive, source-grounded guide to developing, debugging, maintaining, and extending the Falcon Player Android codebase.", styles['CoverMeta']))
    story.append(Paragraph("Compiled from direct static inspection of the Falcon Player production repository.", styles['CoverMeta']))
    story.append(PageBreak())

    # =========================================================================
    # TABLE OF CONTENTS
    # =========================================================================
    story.append(Paragraph("Table of Contents", styles['ChapterHeading']))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#E50914"), spaceBefore=2, spaceAfter=14))

    toc_items = [
        ("Chapter 1  -  Project Overview", "High-level architecture, feature matrix, and system flow"),
        ("Chapter 2  -  Complete Project Structure", "Directory tree and prioritized analysis of top 20 core source files"),
        ("Chapter 3  -  Programming Foundations Used in Falcon Player", "Data classes, sealed interfaces, extension functions, coroutines"),
        ("Chapter 4  -  Android Fundamentals Through Falcon Player", "Activities, window insets, configChanges, and scoped storage"),
        ("Chapter 5  -  Application Startup and Navigation", "AndroidX Navigation 3, NavKey routes, and startup lifecycle trace"),
        ("Chapter 6  -  Complete UI and Design System", "Color tokens, typography, Falcon logo Canvas drawing, and layouts"),
        ("Chapter 7  -  Media Discovery and Library Management", "MediaStore queries, resolution badges, and scoped storage deletes"),
        ("Chapter 8  -  Video Playback Engine: Deep Technical Explanation", "Media3 ExoPlayer, PlayerView surface, and accumulating seek"),
        ("Chapter 9  -  Audio Playback and Audio Library", "AudioPlaybackManager, queue management, audio focus, noise safety"),
        ("Chapter 10  -  Audio Tracks and Subtitle System", "Track discovery, technical codec badges, and selection overrides"),
        ("Chapter 11  -  Timeline, Controls, Gestures, and Fullscreen", "Custom scrubbing Canvas, brightness/volume HUDs, orientation fix"),
        ("Chapter 12  -  Android Permissions, Security, and Storage", "Granular API 33+ permissions, Incognito Mode, and storage boundaries"),
        ("Chapter 13  -  Architecture and Data Flow", "Unidirectional Data Flow (UDF), StateFlow, and Mutex persistence"),
        ("Chapter 14  -  Dependencies and External Libraries", "libs.versions.toml version catalog and dependency impact analysis"),
        ("Chapter 15  -  Build System and APK Distribution", "Gradle build scripts, compileSdk 36, and ADB deployment commands"),
        ("Chapter 16  -  Debugging and Troubleshooting", "Logcat tags, playback error diagnosis, and leak prevention"),
        ("Chapter 17  -  Source-Code Reference", "Detailed method signatures, side effects, and caller dependencies"),
        ("Chapter 18  -  Project-Based Learning Course", "6 progressive developer stages (A to F) with practical exercises"),
        ("Chapter 19  -  Feature Status and Technical Debt", "Source-confirmed feature matrix and architectural debt analysis"),
        ("Chapter 20  -  Glossary, FAQ, and Final Reference", "50 project-specific questions & answers and developer mastery roadmap")
    ]

    toc_table_data = [
        [Paragraph("Chapter", styles['TableHeader']), Paragraph("Synopsis & Key Topics", styles['TableHeader'])]
    ]
    for ch_title, ch_desc in toc_items:
        toc_table_data.append([
            Paragraph(f"<b>{ch_title}</b>", styles['TableCellBold']),
            Paragraph(ch_desc, styles['TableCell'])
        ])

    t_toc = Table(toc_table_data, colWidths=[200, 304])
    t_toc.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#2D3748")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_toc)
    story.append(PageBreak())

    # =========================================================================
    # APPEND ALL 20 CHAPTERS
    # =========================================================================
    story.extend(get_chapters_part1(styles))
    story.extend(get_chapters_part2(styles))
    story.extend(get_chapters_part3(styles))
    story.extend(get_chapters_part4(styles))

    print("Building PDF document...")
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Document successfully created at: {pdf_filename}")

if __name__ == "__main__":
    build_pdf()
