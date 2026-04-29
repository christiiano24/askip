package com.rnandresy.lol.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnandresy.lol.utils.STORY_COLORS
import com.rnandresy.lol.utils.STORY_EMOJIS
import com.rnandresy.lol.viewmodel.AskipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    vm: AskipViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var content  by remember { mutableStateOf("") }
    var selColor by remember { mutableStateOf(STORY_COLORS.first()) }
    var selEmoji by remember { mutableStateOf(STORY_EMOJIS.first()) }

    val bgColor = runCatching {
        Color(android.graphics.Color.parseColor(selColor))
    }.getOrElse { Color(0xFF7C4DFF) }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Nouvelle story 24h 📖") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    FilledIconButton(
                        onClick  = {
                            vm.createStory(content.trim(), selEmoji, selColor)
                            onDone()
                        },
                        enabled = content.isNotBlank()
                    ) { Icon(Icons.Default.Send, null) }
                }
            )
        }
    ) { pad ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Aperçu ────────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(20.dp)
                ) {
                    Text(selEmoji, fontSize = 52.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        content.ifBlank { "Écris quelque chose…" },
                        color      = Color.White,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                }
            }

            // ── Texte ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = content,
                onValueChange = { if (it.length <= 200) content = it },
                placeholder   = { Text("Dis quelque chose… 💭") },
                modifier      = Modifier.fillMaxWidth().height(100.dp),
                shape         = RoundedCornerShape(14.dp),
                maxLines      = 5
            )
            Text(
                "${content.length}/200",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Couleur ───────────────────────────────────────────────────────
            Text(
                "Couleur de fond",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(STORY_COLORS) { hex ->
                    val c = runCatching {
                        Color(android.graphics.Color.parseColor(hex))
                    }.getOrElse { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (selColor == hex)
                                    Modifier.border(3.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .clickable { selColor = hex }
                    )
                }
            }

            // ── Emoji ─────────────────────────────────────────────────────────
            Text(
                "Emoji",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(STORY_EMOJIS) { emoji ->
                    Surface(
                        onClick = { selEmoji = emoji },
                        color   = if (selEmoji == emoji)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape   = RoundedCornerShape(10.dp)
                    ) {
                        Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}