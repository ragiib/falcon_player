import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable, ListFlowable, ListItem
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT, TA_JUSTIFY

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        if self._pageNumber == 1:
            # Skip decorations on the cover page
            return

        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#718096"))

        # Header
        self.drawString(54, letter[1] - 36, "FALCON PLAYER  -  COMPLETE PROJECT DOCUMENTATION & LEARNING TEXTBOOK")
        self.setStrokeColor(colors.HexColor("#E2E8F0"))
        self.setLineWidth(0.75)
        self.line(54, letter[1] - 42, letter[0] - 54, letter[1] - 42)

        # Footer
        self.line(54, 45, letter[0] - 54, 45)
        self.drawString(54, 32, "Confidential  -  Internal Architecture & Developer Study Manual")
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(letter[0] - 54, 32, page_str)
        self.restoreState()

def create_styles():
    styles = getSampleStyleSheet()

    primary_color = colors.HexColor("#E50914") # Falcon Red
    text_dark = colors.HexColor("#1A202C")
    text_muted = colors.HexColor("#4A5568")

    styles.add(ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=30,
        leading=38,
        textColor=primary_color,
        alignment=TA_CENTER,
        spaceAfter=12
    ))

    styles.add(ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=15,
        leading=22,
        textColor=text_dark,
        alignment=TA_CENTER,
        spaceAfter=24
    ))

    styles.add(ParagraphStyle(
        'CoverMeta',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=16,
        textColor=text_muted,
        alignment=TA_CENTER,
        spaceAfter=6
    ))

    styles.add(ParagraphStyle(
        'ChapterHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=26,
        textColor=primary_color,
        spaceBefore=16,
        spaceAfter=10,
        keepWithNext=True
    ))

    styles.add(ParagraphStyle(
        'SectionHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=18,
        textColor=colors.HexColor("#2D3748"),
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    ))

    styles.add(ParagraphStyle(
        'SubSectionHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=15,
        textColor=colors.HexColor("#4A5568"),
        spaceBefore=9,
        spaceAfter=4,
        keepWithNext=True
    ))

    styles.add(ParagraphStyle(
        'Body',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14.5,
        textColor=text_dark,
        alignment=TA_JUSTIFY,
        spaceAfter=7
    ))

    styles.add(ParagraphStyle(
        'BodyBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=14.5,
        textColor=text_dark,
        spaceAfter=7
    ))

    styles.add(ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=7.8,
        leading=10.8,
        textColor=colors.HexColor("#1A202C")
    ))

    styles.add(ParagraphStyle(
        'CalloutText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=13,
        textColor=colors.HexColor("#2D3748")
    ))

    styles.add(ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11,
        textColor=colors.white,
        alignment=TA_LEFT
    ))

    styles.add(ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11,
        textColor=text_dark,
        alignment=TA_LEFT
    ))

    styles.add(ParagraphStyle(
        'TableCellBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=11,
        textColor=text_dark,
        alignment=TA_LEFT
    ))

    styles.add(ParagraphStyle(
        'TableCellCode',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=7.5,
        leading=10,
        textColor=colors.HexColor("#B81D24"),
        alignment=TA_LEFT
    ))

    return styles

def build_callout(title, text, style_type="note", styles=None):
    if styles is None:
        styles = create_styles()

    color_map = {
        "note": (colors.HexColor("#0284C7"), colors.HexColor("#F0F9FF")),
        "tip": (colors.HexColor("#059669"), colors.HexColor("#ECFDF5")),
        "warning": (colors.HexColor("#D97706"), colors.HexColor("#FFFBEB")),
        "danger": (colors.HexColor("#DC2626"), colors.HexColor("#FEF2F2")),
        "arch": (colors.HexColor("#E50914"), colors.HexColor("#FFF5F5"))
    }
    border_col, bg_col = color_map.get(style_type, color_map["note"])

    content = [
        Paragraph(f"<b><font color='{border_col.hexval()}'>{title.upper()}:</font></b> {text}", styles['CalloutText'])
    ]

    t = Table([[content]], colWidths=[letter[0] - 108])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), bg_col),
        ('BOX', (0,0), (-1,-1), 0.75, border_col),
        ('LINELEFT', (0,0), (-1,-1), 3.5, border_col),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    return t

def build_code_box(code_text, styles=None, max_lines=30):
    if styles is None:
        styles = create_styles()
    
    escaped = code_text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    lines = escaped.strip().split("\n")
    
    flowables = []
    for i in range(0, len(lines), max_lines):
        chunk = lines[i:i + max_lines]
        formatted_html = "<br/>".join(chunk)
        p = Paragraph(formatted_html, styles['CodeSnippet'])
        t = Table([[p]], colWidths=[letter[0] - 108])
        t.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8F9FA")),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
            ('TOPPADDING', (0,0), (-1,-1), 5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 5),
            ('LEFTPADDING', (0,0), (-1,-1), 8),
            ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ]))
        flowables.append(t)
        if i + max_lines < len(lines):
            flowables.append(Spacer(1, 4))
            
    return flowables

