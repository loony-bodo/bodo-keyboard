package com.loony.bodokeyboard.ui.setup

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.loony.bodokeyboard.ui.theme.AccentBrush
import com.loony.bodokeyboard.ui.theme.AccentL
import com.loony.bodokeyboard.ui.theme.AccentR
import com.loony.bodokeyboard.ui.theme.DivLine
import com.loony.bodokeyboard.ui.theme.GreenDone
import com.loony.bodokeyboard.ui.theme.Surface1
import com.loony.bodokeyboard.ui.theme.Surface2
import com.loony.bodokeyboard.ui.theme.TextPri
import com.loony.bodokeyboard.ui.theme.TextSec

/**
 * Onboarding wizard that guides the user through two steps:
 *  1. Enable Bodo Keyboard in System Settings
 *  2. Select it as the active input method
 *
 * Status is polled every second via a LaunchedEffect loop.
 */
@Composable
fun SetupView() {
    val context     = LocalContext.current
    var isEnabled  by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            isEnabled  = imm.enabledInputMethodList.any { it.packageName == context.packageName }
            val cur    = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
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

        // ── Logo ──────────────────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(AccentL.copy(alpha = 0.25f), Color.Transparent)))
            )
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
            fontSize  = 14.sp,
            color     = TextSec,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        SetupProgressBar(step1Done = isEnabled, step2Done = isSelected)

        Spacer(Modifier.height(28.dp))

        SetupStepCard(
            number      = 1,
            title       = "Enable Keyboard",
            description = "Add Bodo Keyboard to your list of input methods in System Settings.",
            icon        = Icons.Default.Settings,
            isDone      = isEnabled,
            onClick     = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))

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
        ProgressLine(
            done     = step1Done && step2Done,
            modifier = Modifier.weight(1f)
        )
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
                .background(
                    if (done) AccentBrush
                    else Brush.linearGradient(listOf(Surface2, Surface2))
                )
                .border(1.dp, if (done) Color.Transparent else DivLine, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (done) "✓" else "·",
                color      = Color.White,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
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
            .background(
                if (done) AccentBrush
                else Brush.linearGradient(listOf(Surface2, Surface2))
            )
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
            // Step number or checkmark circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) AccentBrush
                        else Brush.linearGradient(listOf(Surface2, Surface2))
                    )
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
                Text(title,       fontWeight = FontWeight.Bold, color = TextPri, fontSize = 15.sp)
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
                    .clickable(
                        onClick           = onClick,
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
            ) {
                Text(
                    if (isDone) "Done" else "Open",
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
