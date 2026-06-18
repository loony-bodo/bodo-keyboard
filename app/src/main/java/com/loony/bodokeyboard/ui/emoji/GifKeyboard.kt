package com.loony.bodokeyboard.ui.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.ui.keyboard.KeyButton
import com.loony.bodokeyboard.ui.theme.KbBg
import com.loony.bodokeyboard.ui.theme.KeyNorm
import com.loony.bodokeyboard.ui.theme.ToolTxt

/**
 * Placeholder GIF keyboard panel.
 * Currently shows sample emojis; replace with a real GIF API integration.
 */
@Composable
fun GifKeyboard(onKeyClick: (String) -> Unit) {
    val sampleGifs = listOf("🔥", "🎉", "❤️", "😂", "👍", "🙏", "✨", "🙌")

    Column(
        modifier = Modifier
            .background(KbBg)
            .height(240.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GIF Search (Placeholder)", color = ToolTxt, fontSize = 16.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sampleGifs.forEach { gif ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(KeyNorm, RoundedCornerShape(8.dp))
                        .clickable { onKeyClick(gif) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(gif, fontSize = 20.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        KeyButton(
            key        = "ABC",
            modifier   = Modifier.fillMaxWidth(0.3f),
            mode       = KeyboardMode.GIF,
            isCapsLock = false,
            viewModel  = null,
            onClick    = { onKeyClick("ABC") }
        )
    }
}
