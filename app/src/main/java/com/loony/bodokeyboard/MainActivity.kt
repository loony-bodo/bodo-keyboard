package com.loony.bodokeyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Palette (mirrors keyboard) ────────────────────────────────────────────────
private val AppBg     = Color(0xFF090B16)
private val Surface1  = Color(0xFF0E1022)
private val Surface2  = Color(0xFF141626)
private val AccentL   = Color(0xFF2563EB)
private val AccentR   = Color(0xFF7C3AED)
private val AccentBrush = Brush.linearGradient(listOf(AccentL, AccentR))
private val GreenDone = Color(0xFF22C55E)
private val TextPri   = Color(0xFFE2E8F0)
private val TextSec   = Color(0xFF94A3B8)
private val DivLine   = Color.White.copy(alpha = 0.07f)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BodoTheme { MainScreen() } }
    }
}

@Composable
fun BodoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary    = AccentL,
            secondary  = AccentR,
            background = AppBg,
            surface    = Surface1,
            onPrimary  = Color.White,
            onBackground = TextPri,
            onSurface  = TextPri
        ),
        content = content
    )
}

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
                    .border(
                        width = 1.dp,
                        color = DivLine,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                    )
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick  = { tab = 0 },
                        icon = {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = if (tab == 0) AccentL else TextSec)
                        },
                        label = { Text("Setup",
                            color = if (tab == 0) AccentL else TextSec, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick  = { tab = 1 },
                        icon = {
                            Icon(Icons.Default.Keyboard, null,
                                tint = if (tab == 1) AccentL else TextSec)
                        },
                        label = { Text("Try It",
                            color = if (tab == 1) AccentL else TextSec, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick  = { tab = 2 },
                        icon = {
                            Icon(Icons.Default.Settings, null,
                                tint = if (tab == 2) AccentL else TextSec)
                        },
                        label = { Text("Settings",
                            color = if (tab == 2) AccentL else TextSec, fontSize = 12.sp) },
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

// ── Setup ─────────────────────────────────────────────────────────────────────
@Composable
fun SetupView() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            isEnabled = imm.enabledInputMethodList.any { it.packageName == context.packageName }
            val cur = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            isSelected = cur?.contains(context.packageName) == true
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Logo ─────────────────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AccentL.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            )
            // Inner circle
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AccentBrush),
                contentAlignment = Alignment.Center
            ) {
                Text("बर'", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Bodo Keyboard",
            fontSize   = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = TextPri,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "बड' भाषा • Devanagari Script",
            fontSize = 14.sp,
            color    = TextSec,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // ── Progress indicator ────────────────────────────────────────────────
        SetupProgressBar(step1Done = isEnabled, step2Done = isSelected)

        Spacer(Modifier.height(28.dp))

        // ── Step 1 ────────────────────────────────────────────────────────────
        SetupStepCard(
            number      = 1,
            title       = "Enable Keyboard",
            description = "Add Bodo Keyboard to your list of input methods in System Settings.",
            icon        = Icons.Default.Settings,
            isDone      = isEnabled,
            onClick     = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))

        // ── Step 2 ────────────────────────────────────────────────────────────
        SetupStepCard(
            number      = 2,
            title       = "Select Keyboard",
            description = "Switch to Bodo Keyboard as your active input method.",
            icon        = Icons.Default.Info,
            isDone      = isSelected,
            onClick     = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        )

        if (isEnabled && isSelected) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenDone.copy(alpha = 0.12f))
                    .border(1.dp, GreenDone.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓  You're all set! Bodo Keyboard is active.",
                    color      = GreenDone,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    textAlign  = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SetupProgressBar(step1Done: Boolean, step2Done: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgressDot(done = step1Done, label = "Enable")
        ProgressLine(done = step1Done && step2Done, modifier = Modifier.weight(1f))
        ProgressDot(done = step2Done, label = "Select")
    }
}

@Composable
private fun ProgressDot(done: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) AccentBrush else Brush.linearGradient(listOf(Surface2, Surface2)))
                .border(1.dp, if (done) Color.Transparent else DivLine, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(if (done) "✓" else "·", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = if (done) AccentL else TextSec)
    }
}

@Composable
private fun ProgressLine(done: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(if (done) AccentBrush else Brush.linearGradient(listOf(Surface2, Surface2)))
    )
}

@Composable
private fun SetupStepCard(
    number: Int,
    title: String,
    description: String,
    icon: ImageVector,
    isDone: Boolean,
    onClick: () -> Unit
) {
    val border = if (isDone)
        Brush.linearGradient(listOf(AccentL.copy(alpha = 0.5f), AccentR.copy(alpha = 0.5f)))
    else
        Brush.linearGradient(listOf(DivLine, DivLine))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDone) 8.dp else 2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDone) Surface2 else Surface1)
            .border(1.dp, border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number / check circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDone) AccentBrush else Brush.linearGradient(listOf(Surface2, Surface2)))
                    .border(1.dp, if (isDone) Color.Transparent else DivLine, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Text("✓", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("$number", color = TextSec, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPri, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 13.sp, color = TextSec, lineHeight = 18.sp)
            }

            Spacer(Modifier.width(8.dp))

            // Action button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBrush)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable(onClick = onClick, indication = null,
                        interactionSource = remember { MutableInteractionSource() }),
            ) {
                Text(
                    if (isDone) "Done" else "Open",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────
@Composable
fun PreviewView() {
    val viewModel: KeyboardViewModel = viewModel()
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.initDatabase(context)
        val prefs = context.getSharedPreferences("keyboard_settings", Context.MODE_PRIVATE)
        viewModel.hapticEnabled.value = prefs.getBoolean("haptic", true)
        viewModel.soundEnabled.value = prefs.getBoolean("sound", false)
        viewModel.keyboardHeightMultiplier.value = prefs.getFloat("height", 1.0f)

        // Load word lists for preview
        val bodoWords = try {
            context.assets.open("bodo_words.txt").bufferedReader().readLines()
                .map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        } catch (_: Exception) { emptyList() }
        val englishWords = try {
            context.assets.open("english_words.txt").bufferedReader().readLines()
                .map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        } catch (_: Exception) { emptyList() }
        viewModel.setWordLists(bodoWords, englishWords)
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Try the Bodo Keyboard below",
                    color = TextSec,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface1,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DivLine)
                ) {
                    Box(Modifier.padding(16.dp)) {
                        if (text.isEmpty()) {
                            Text("Type something...", color = TextSec.copy(alpha = 0.5f))
                        }
                        Text(text, color = TextPri, fontSize = 18.sp)
                    }
                }
            }
        }

        KeyboardScreen(
            viewModel = viewModel,
            onKeyClick = { key ->
                when (key) {
                    "BACKSPACE" -> {
                        if (viewModel.isTranslitMode() && viewModel.translitBuffer.value.isNotEmpty()) {
                            viewModel.translitBackspace()
                            text = text.dropLast(1) 
                        } else if (text.isNotEmpty()) {
                            text = text.dropLast(1)
                        }
                    }
                    "SPACE" -> {
                        if (viewModel.isTranslitMode()) {
                            text += viewModel.flushTranslit()
                        }
                        text += " "
                        viewModel.updateSuggestions("")
                    }
                    "ENTER" -> {
                        if (viewModel.isTranslitMode()) text += viewModel.flushTranslit()
                        text += "\n"
                        viewModel.updateSuggestions("")
                    }
                    "SHIFT" -> viewModel.toggleShift()
                    "SYM" -> viewModel.toggleSymbols()
                    "SYM_PAGE" -> viewModel.toggleSymbolsPage()
                    "ABC" -> {
                        if (viewModel.keyboardMode.value == KeyboardMode.EMOJI || 
                            viewModel.keyboardMode.value == KeyboardMode.GIF) {
                            viewModel.setMode(KeyboardMode.BODO)
                        } else {
                            viewModel.toggleSymbols()
                        }
                    }
                    "MODE_SWITCH" -> {
                        if (viewModel.isTranslitMode()) text += viewModel.flushTranslit()
                        viewModel.setMode(
                            if (viewModel.keyboardMode.value == KeyboardMode.BODO) KeyboardMode.ENGLISH
                            else KeyboardMode.BODO
                        )
                    }
                    "TRANSLIT_TOGGLE" -> {
                        if (viewModel.isTranslitMode()) text += viewModel.flushTranslit()
                        viewModel.setMode(
                            if (viewModel.keyboardMode.value == KeyboardMode.TRANSLIT) KeyboardMode.ENGLISH
                            else KeyboardMode.TRANSLIT
                        )
                    }
                    "EMOJI_SWITCH" -> {
                        if (viewModel.isTranslitMode()) text += viewModel.flushTranslit()
                        viewModel.setMode(KeyboardMode.EMOJI)
                    }
                    "GIF_SWITCH" -> {
                        if (viewModel.isTranslitMode()) text += viewModel.flushTranslit()
                        viewModel.setMode(KeyboardMode.GIF)
                    }
                    "SETTINGS_OPEN" -> { /* tab = 2 */ }
                    else -> {
                        if (viewModel.isTranslitMode() && key.length == 1 && key[0].isLetter()) {
                            val result = viewModel.feedTranslit(key[0])
                            // Simple preview logic: replace last char(s) with pending translit
                            // This is just for visualization in the app.
                            text = text.substring(0, text.length - viewModel.translitBuffer.value.length + 1) + result.pending
                        } else {
                            text += key
                            viewModel.autoResetShift()
                        }
                    }
                }
                if (!viewModel.isTranslitMode()) {
                    val lastWord = text.split(" ", "\n").lastOrNull() ?: ""
                    viewModel.updateSuggestions(lastWord)
                }
            },
            onSuggestionClick = { word ->
                val lastSpace = text.lastIndexOf(' ')
                text = if (lastSpace != -1) text.substring(0, lastSpace + 1) + word + " "
                       else word + " "
                viewModel.clearTranslitBuffer()
                viewModel.updateSuggestions("")
            }
        )
    }
}

// ── Settings ──────────────────────────────────────────────────────────────────
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

        Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
        Text("Customize your typing experience", fontSize = 13.sp, color = TextSec)

        Spacer(Modifier.height(28.dp))

        // ── Feedback section ──────────────────────────────────────────────────
        SettingsSection("Feedback") {
            SettingsToggleRow(
                icon        = "📳",
                title       = "Vibration",
                subtitle    = "Haptic feedback on keypress",
                checked     = haptic,
                onChanged   = {
                    haptic = it
                    prefs.edit().putBoolean("haptic", it).apply()
                }
            )
            SettingsDivider()
            SettingsToggleRow(
                icon        = "🔊",
                title       = "Key Sounds",
                subtitle    = "Click sound on keypress",
                checked     = sound,
                onChanged   = {
                    sound = it
                    prefs.edit().putBoolean("sound", it).apply()
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Layout section ────────────────────────────────────────────────────
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
                        color = AccentL, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
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
                        thumbColor            = AccentL,
                        activeTrackColor      = AccentL,
                        inactiveTrackColor    = Surface2
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
            text     = title.uppercase(),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color    = TextSec,
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
                checkedThumbColor  = Color.White,
                checkedTrackColor  = AccentL,
                uncheckedTrackColor = Surface2,
                uncheckedThumbColor = TextSec
            )
        )
    }
}
