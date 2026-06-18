package com.loony.bodokeyboard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── App / settings palette — shared by setup, preview, and settings screens ──

internal val AppBg       = Color(0xFF090B16)
internal val Surface1    = Color(0xFF0E1022)
internal val Surface2    = Color(0xFF141626)
internal val AccentL     = Color(0xFF2563EB)
internal val AccentR     = Color(0xFF7C3AED)
internal val AccentBrush = Brush.linearGradient(listOf(AccentL, AccentR))
internal val GreenDone   = Color(0xFF22C55E)
internal val TextPri     = Color(0xFFE2E8F0)
internal val TextSec     = Color(0xFF94A3B8)
internal val DivLine     = Color.White.copy(alpha = 0.07f)
