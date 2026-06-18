package com.loony.bodokeyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.ui.theme.ChipShape
import com.loony.bodokeyboard.ui.theme.DividerC
import com.loony.bodokeyboard.ui.theme.EnterBg
import com.loony.bodokeyboard.ui.theme.KbBg

private val ToolIconSize = 22.dp

/**
 * The thin toolbar row sitting between the suggestion bar and the key rows.
 * Contains: collapse button, language toggle, emoji, GIF, paste, settings, microphone.
 */
@Composable
internal fun ToolbarRow(mode: KeyboardMode, onKeyClick: (String) -> Unit) {
    val modeLabel = if (mode == KeyboardMode.BODO) "EN" else "बर'"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(KbBg)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Collapse / back chevron with a subtle accent ring
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(ChipShape)
                .background(EnterBg.copy(alpha = 0.25f))
                .clickable { onKeyClick("COLLAPSE") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Collapse",
                tint               = EnterBg,
                modifier           = Modifier.size(ToolIconSize)
            )
        }

        // Language toggle (EN ↔ बर')
        ToolBtn(label = modeLabel) { onKeyClick("MODE_SWITCH") }
        ToolBtn(icon = Icons.Default.EmojiEmotions)  { onKeyClick("EMOJI_SWITCH") }
        ToolBtn(label = "GIF")                       { onKeyClick("GIF_SWITCH") }
        ToolBtn(icon = Icons.Default.ContentPaste)   { onKeyClick("PASTE") }

        // Vertical divider
        Box(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(DividerC)
        )

        ToolBtn(icon = Icons.Default.MoreHoriz) { onKeyClick("SETTINGS_OPEN") }
        ToolBtn(icon = Icons.Default.Mic)       { /* voice input not yet implemented */ }
    }
}

/** A single square button in the toolbar — renders either an icon or a text label. */
@Composable
internal fun ToolBtn(
    icon: ImageVector? = null,
    label: String? = null,
    isHighlight: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isHighlight) Modifier.background(EnterBg.copy(alpha = 0.25f)) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (isHighlight) EnterBg else Color.White,
                modifier           = Modifier.size(ToolIconSize)
            )
        } else if (label != null) {
            Text(
                text       = label,
                color      = if (isHighlight) EnterBg else Color.White,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
