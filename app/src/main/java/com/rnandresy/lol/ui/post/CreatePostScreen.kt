package com.rnandresy.lol.ui.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnandresy.lol.ui.feed.MentionTextField
import com.rnandresy.lol.utils.isAdmin
import com.rnandresy.lol.viewmodel.AskipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    vm: AskipViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val loading     by vm.loading.collectAsState()
    val allProfiles by vm.allProfiles.collectAsState()
    val myProfile   by vm.myProfile.collectAsState()

    var postType    by remember { mutableStateOf("normal") }
    var contentTfv  by remember { mutableStateOf(TextFieldValue("")) }
    var opt1        by remember { mutableStateOf("") }
    var opt2        by remember { mutableStateOf("") }

    val content     = contentTfv.text
    val isAdminUser = isAdmin(vm.currentUserId) || myProfile?.isAdmin == true

    val canPost = when (postType) {
        "poll" -> content.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()
        else   -> content.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (postType) {
                            "poll"       -> "Nouveau sondage 📊"
                            "confession" -> "Confession anonyme 🎭"
                            else         -> "Nouvelle rumeur 📢"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    FilledIconButton(
                        onClick  = {
                            vm.createPost(content.trim(), postType, opt1.trim(), opt2.trim())
                            onDone()
                        },
                        enabled = canPost && !loading
                    ) {
                        if (loading)
                            CircularProgressIndicator(
                                Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp
                            )
                        else Icon(Icons.Default.Send, null)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier            = Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Sélecteur de type ─────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "normal"     to "📢 Rumeur",
                    "poll"       to "📊 Sondage",
                    "confession" to "🎭 Confession"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = postType == type,
                        onClick  = { postType = type },
                        label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // ── Info confession ───────────────────────────────────────────────
            if (postType == "confession") {
                Surface(
                    color  = MaterialTheme.colorScheme.surfaceVariant,
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🕵️", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ton identité sera cachée. Tu apparaîtras comme \"Quelqu'un 🎭\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Info mentions ─────────────────────────────────────────────────
            Surface(
                color  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape  = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        buildString {
                            append("Tape @ pour mentionner quelqu'un")
                            if (isAdminUser) append(" · @everyone pour tout le monde 📢")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ── Champ texte avec mentions ─────────────────────────────────────
            MentionTextField(
                value         = contentTfv,
                onValueChange = { if (it.text.length <= 500) contentTfv = it },
                allProfiles   = allProfiles,
                currentUserId = vm.currentUserId,
                isAdminUser   = isAdminUser,
                placeholder   = when (postType) {
                    "poll"       -> "De quoi parle ce sondage ?"
                    "confession" -> "Ta confession secrète…"
                    else         -> "Askip... qu'est-ce qui se passe ? 👀"
                },
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${content.length}/500",
                style = MaterialTheme.typography.labelSmall,
                color = if (content.length > 450) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Options sondage ───────────────────────────────────────────────
            if (postType == "poll") {
                Text(
                    "Options du sondage",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value         = opt1,
                    onValueChange = { if (it.length <= 60) opt1 = it },
                    label         = { Text("Option A") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value         = opt2,
                    onValueChange = { if (it.length <= 60) opt2 = it },
                    label         = { Text("Option B") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}