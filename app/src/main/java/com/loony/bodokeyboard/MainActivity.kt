package com.loony.bodokeyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.ui.preview.PreviewView
import com.loony.bodokeyboard.ui.settings.SettingsView
import com.loony.bodokeyboard.ui.setup.SetupView
import com.loony.bodokeyboard.ui.theme.AccentL
import com.loony.bodokeyboard.ui.theme.AccentR
import com.loony.bodokeyboard.ui.theme.AppBg
import com.loony.bodokeyboard.ui.theme.DivLine
import com.loony.bodokeyboard.ui.theme.Surface1
import com.loony.bodokeyboard.ui.theme.TextPri
import com.loony.bodokeyboard.ui.theme.TextSec

/**
 * Entry-point activity for the settings / onboarding app.
 * Hosts three tabs: Setup, Try It (preview), and Settings.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BodoTheme { MainScreen() } }
    }
}

/** Material3 dark theme wired to the app accent palette. */
@Composable
fun BodoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary      = AccentL,
            secondary    = AccentR,
            background   = AppBg,
            surface      = Surface1,
            onPrimary    = Color.White,
            onBackground = TextPri,
            onSurface    = TextPri
        ),
        content = content
    )
}

/** Bottom-nav shell switching between Setup, Preview, and Settings screens. */
@Composable
fun MainScreen() {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface1)
                    .border(width = 1.dp, color = DivLine, shape = RoundedCornerShape(0.dp))
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick  = { tab = 0 },
                        icon  = { Icon(Icons.Default.CheckCircle, null, tint = if (tab == 0) AccentL else TextSec) },
                        label = { Text("Setup",    color = if (tab == 0) AccentL else TextSec, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick  = { tab = 1 },
                        icon  = { Icon(Icons.Default.Keyboard, null, tint = if (tab == 1) AccentL else TextSec) },
                        label = { Text("Try It",   color = if (tab == 1) AccentL else TextSec, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick  = { tab = 2 },
                        icon  = { Icon(Icons.Default.Settings, null, tint = if (tab == 2) AccentL else TextSec) },
                        label = { Text("Settings", color = if (tab == 2) AccentL else TextSec, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
        ) {
            when (tab) {
                0 -> SetupView()
                1 -> PreviewView()
                2 -> SettingsView()
            }
        }
    }
}
