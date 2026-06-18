package com.loony.bodokeyboard.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Keyboard palette — Gboard-inspired dark theme ─────────────────────────────

internal val KbBg       = Color(0xFF171717)   // keyboard background
internal val KeyNorm    = Color(0xFF303134)   // normal key background
internal val KeyPressed = Color(0xFF424346)   // normal key pressed state
internal val KeySpec    = Color(0xFF1F1F1F)   // special keys (shift, backspace, etc.)
internal val KeySpecP   = Color(0xFF303134)   // special key pressed state
internal val EnterBg    = Color(0xFF8AB4F8)   // enter / action key (Gboard blue)
internal val CapsActive = Color(0xFF8AB4F8)   // CapsLock indicator tint
internal val SuggBg     = Color(0xFF171717)   // suggestion bar background
internal val SuggTxt    = Color(0xFFE8EAED)   // suggestion text
internal val DividerC   = Color(0xFF3C4043)   // horizontal / vertical dividers
internal val ChipHighBg = Color(0xFF8AB4F8)   // highlighted alternate chip
internal val ChipBg     = Color(0xFF303134)   // normal alternate chip
internal val KeyTxt     = Color(0xFFE8EAED)   // key label text
internal val HintTxt    = Color(0xFF9AA0A6)   // number hint text (top-right corner)
internal val ToolTxt    = Color(0xFF9AA0A6)   // toolbar icon / label tint

// ── Shapes ────────────────────────────────────────────────────────────────────

internal val KeyShape  = RoundedCornerShape(6.dp)
internal val ChipShape = RoundedCornerShape(50)
