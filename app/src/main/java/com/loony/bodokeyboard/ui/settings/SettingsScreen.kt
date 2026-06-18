package com.loony.bodokeyboard.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.ui.theme.AccentL
import com.loony.bodokeyboard.ui.theme.DivLine
import com.loony.bodokeyboard.ui.theme.Surface1
import com.loony.bodokeyboard.ui.theme.Surface2
import com.loony.bodokeyboard.ui.theme.TextPri
import com.loony.bodokeyboard.ui.theme.TextSec

/**
 * Settings screen for adjusting haptic feedback, key sound, and keyboard height.
 * All settings are persisted immediately to SharedPreferences.
 */
@Composable
fun SettingsView() {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("keyboard_settings", Context.MODE_PRIVATE) }
    var haptic  by remember { mutableStateOf(prefs.getBoolean("haptic", true)) }
    var sound   by remember { mutableStateOf(prefs.getBoolean("sound", false)) }
    var height  by remember { mutableFloatStateOf(prefs.getFloat("height", 1.0f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        Text("Settings",   fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
        Text("Customize your typing experience", fontSize = 13.sp, color = TextSec)

        Spacer(Modifier.height(28.dp))

        // ── Feedback ──────────────────────────────────────────────────────────
        SettingsSection("Feedback") {
            SettingsToggleRow(
                icon     = "📳",
                title    = "Vibration",
                subtitle = "Haptic feedback on keypress",
                checked  = haptic,
                onChanged = {
                    haptic = it
                    prefs.edit().putBoolean("haptic", it).apply()
                }
            )
            SettingsDivider()
            SettingsToggleRow(
                icon     = "🔊",
                title    = "Key Sounds",
                subtitle = "Click sound on keypress",
                checked  = sound,
                onChanged = {
                    sound = it
                    prefs.edit().putBoolean("sound", it).apply()
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Layout ────────────────────────────────────────────────────────────
        SettingsSection("Layout") {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⌨️", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Keyboard Height", color = TextPri, fontWeight = FontWeight.SemiBold)
                        Text("Adjust key row height", fontSize = 12.sp, color = TextSec)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = when {
                            height < 0.9f -> "Short"
                            height > 1.1f -> "Tall"
                            else          -> "Normal"
                        },
                        color      = AccentL,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value         = height,
                    onValueChange = {
                        height = it
                        prefs.edit().putFloat("height", it).apply()
                    },
                    valueRange = 0.8f..1.3f,
                    steps      = 4,
                    colors     = SliderDefaults.colors(
                        thumbColor         = AccentL,
                        activeTrackColor   = AccentL,
                        inactiveTrackColor = Surface2
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                    Text("Short",  fontSize = 11.sp, color = TextSec)
                    Text("Normal", fontSize = 11.sp, color = TextSec)
                    Text("Tall",   fontSize = 11.sp, color = TextSec)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, DivLine, RoundedCornerShape(14.dp))
    ) {
        Text(
            text          = title.uppercase(),
            modifier      = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextSec,
            letterSpacing = 1.sp
        )
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 56.dp)
            .height(1.dp)
            .background(DivLine)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    color = TextPri, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = TextSec, fontSize = 12.sp)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChanged,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = AccentL,
                uncheckedTrackColor = Surface2,
                uncheckedThumbColor = TextSec
            )
        )
    }
}
