package com.loony.bodokeyboard.ui.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loony.bodokeyboard.data.KeyboardMode
import com.loony.bodokeyboard.ui.keyboard.KeyButton
import com.loony.bodokeyboard.ui.keyboard.ToolBtn
import com.loony.bodokeyboard.ui.theme.ChipShape
import com.loony.bodokeyboard.ui.theme.DividerC
import com.loony.bodokeyboard.ui.theme.EnterBg
import com.loony.bodokeyboard.ui.theme.KbBg
import com.loony.bodokeyboard.ui.theme.KeySpec
import com.loony.bodokeyboard.ui.theme.SuggTxt
import com.loony.bodokeyboard.ui.theme.ToolTxt
import com.loony.bodokeyboard.viewmodel.KeyboardViewModel

/**
 * Full-screen emoji picker panel.
 *
 * Features:
 *  - Recently-used tab (in-memory, newest first)
 *  - 8 category tabs (smileys, people, animals, food, sports, travel, objects, symbols)
 *  - Keyword search with a mini-QWERTY input row
 *
 * @param viewModel  Provides and updates the recently-used emoji list.
 * @param onKeyClick Forwarded to the host (emits the chosen emoji or action key).
 */
@Composable
fun EmojiKeyboard(viewModel: KeyboardViewModel, onKeyClick: (String) -> Unit) {
    // -1 = Recently Used tab, 0..7 = category index
    var selectedCategory by remember { mutableIntStateOf(-1) }
    var searchQuery      by remember { mutableStateOf("") }
    var isSearching      by remember { mutableStateOf(false) }

    val searchResults: List<String>? = searchQuery.trim().takeIf { it.isNotEmpty() }?.let { q ->
        EMOJI_KEYWORDS.entries
            .filter { it.key.contains(q, ignoreCase = true) }
            .flatMap { it.value }
            .distinct()
    }

    val displayedEmojis: List<String> = searchResults
        ?: if (selectedCategory == -1) viewModel.recentEmojis
           else EMOJI_CATEGORIES[selectedCategory].second

    fun handleEmojiClick(emoji: String) {
        viewModel.addRecentEmoji(emoji)
        onKeyClick(emoji)
    }

    fun closeSearch() {
        isSearching  = false
        searchQuery  = ""
    }

    Column(
        modifier = Modifier
            .background(KbBg)
            .height(if (isSearching) 340.dp else 280.dp)
    ) {
        if (isSearching) {
            // Search screen header — back arrow returns to the emoji panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(ChipShape)
                        .clickable { closeSearch() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint     = SuggTxt,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text("Search emoji", color = SuggTxt, fontSize = 16.sp)
            }

            // Matched emoji as a single horizontal row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items(displayedEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KeySpec)
                            .clickable { handleEmojiClick(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerC))
        }

        // ── Search bar ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(KeySpec)
                .clickable(enabled = !isSearching) { isSearching = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ToolTxt, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text("Search emoji", color = ToolTxt, fontSize = 14.sp)
                    } else {
                        Text(searchQuery, color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp)
                    }
                }
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(ChipShape)
                            .background(DividerC)
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = SuggTxt, fontSize = 11.sp)
                    }
                }
            }
        }

        if (!isSearching) {
            // ── Category tab row ──────────────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KeySpec)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    EmojiTab(
                        icon       = Icons.Default.AccessTime,
                        isSelected = selectedCategory == -1,
                        onClick    = { selectedCategory = -1 }
                    )
                }
                itemsIndexed(EMOJI_CATEGORY_ICONS) { idx, icon ->
                    EmojiTab(
                        icon       = icon,
                        isSelected = selectedCategory == idx,
                        onClick    = { selectedCategory = idx }
                    )
                }
            }

            if (selectedCategory == -1) {
                Text(
                    text          = "RECENTLY USED",
                    color         = ToolTxt,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    modifier      = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 2.dp)
                )
            }
        }

        // ── Emoji grid ────────────────────────────────────────────────────────
        if (!isSearching) {
            if (displayedEmojis.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recently used emoji", color = ToolTxt, fontSize = 13.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns        = GridCells.Fixed(7),
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(displayedEmojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { handleEmojiClick(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 28.sp)
                        }
                    }
                }
            }
        } else if (displayedEmojis.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = if (searchResults != null) "No matching emoji" else "Type to search emoji",
                    color = ToolTxt,
                    fontSize = 13.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (isSearching) {
            // ── Mini QWERTY search keyboard ───────────────────────────────────
            // Key presses update searchQuery locally — never sent to the host app.
            val searchRows = listOf(
                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                listOf("z", "x", "c", "v", "b", "n", "m", "BACKSPACE")
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                searchRows.forEachIndexed { rowIdx, row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (rowIdx == 1) Spacer(Modifier.weight(0.5f))
                        row.forEach { key ->
                            KeyButton(
                                key       = key,
                                modifier  = Modifier.weight(if (key == "BACKSPACE") 1.9f else 1f),
                                mode      = KeyboardMode.ENGLISH,
                                isCapsLock = false,
                                viewModel = viewModel,
                                onClick   = {
                                    searchQuery = if (key == "BACKSPACE") searchQuery.dropLast(1)
                                                  else searchQuery + key
                                }
                            )
                        }
                        if (rowIdx == 1) Spacer(Modifier.weight(0.5f))
                    }
                }
            }
        } else {
            // ── Bottom action bar ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(KeySpec)
                    .padding(horizontal = 4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeyButton(
                    key       = "ABC",
                    modifier  = Modifier.size(42.dp),
                    mode      = KeyboardMode.EMOJI,
                    isCapsLock = false,
                    viewModel = viewModel,
                    onClick   = { onKeyClick("ABC") }
                )
                ToolBtn(icon = Icons.Default.ContentPaste)                       { onKeyClick("PASTE") }
                ToolBtn(icon = Icons.Default.EmojiEmotions, isHighlight = true)  { /* current panel */ }
                ToolBtn(icon = Icons.Default.InsertEmoticon)                     { /* stickers not yet implemented */ }
                ToolBtn(icon = Icons.AutoMirrored.Filled.Chat)                   { /* chat emoji not yet implemented */ }
                ToolBtn(label = "GIF")                                           { onKeyClick("GIF_SWITCH") }
                ToolBtn(label = ":-)")                                           { /* kaomoji not yet implemented */ }
                KeyButton(
                    key       = "BACKSPACE",
                    modifier  = Modifier.size(42.dp),
                    mode      = KeyboardMode.EMOJI,
                    isCapsLock = false,
                    viewModel = viewModel,
                    onClick   = { onKeyClick("BACKSPACE") }
                )
            }
        }
    }
}

/** A single category icon tab in the emoji panel. */
@Composable
private fun EmojiTab(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) EnterBg.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (isSelected) EnterBg else androidx.compose.ui.graphics.Color.White,
            modifier           = Modifier.size(18.dp)
        )
    }
}
