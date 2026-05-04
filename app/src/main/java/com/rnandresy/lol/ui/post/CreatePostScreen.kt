package com.rnandresy.lol.ui.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val loading        by vm.loading.collectAsState()
    val uploadProgress by vm.uploadProgress.collectAsState()
    val allProfiles    by vm.allProfiles.collectAsState()
    val myProfile      by vm.myProfile.collectAsState()

    var postType   by remember { mutableStateOf("normal") }
    var contentTfv by remember { mutableStateOf(TextFieldValue("")) }
    var opt1       by remember { mutableStateOf("") }
    var opt2       by remember { mutableStateOf("") }
    var imageUri   by remember { mutableStateOf<Uri?>(null) }
    var videoUri   by remember { mutableStateOf<Uri?>(null) }

    val content     = contentTfv.text
    val isAdminUser = isAdmin(vm.currentUserId) || myProfile?.isAdmin == true
    val hasMedia    = imageUri != null || videoUri != null

    val canPost = when (postType) {
        "poll" -> content.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()
        else   -> content.isNotBlank() || hasMedia
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { imageUri = it; videoUri = null } }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { videoUri = it; imageUri = null } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (postType) {
                        "poll"       -> "Sondage 📊"
                        "confession" -> "Confession 🎭"
                        else         -> "Nouvelle rumeur 📢"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !loading) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    FilledIconButton(
                        onClick  = {
                            vm.createPostWithMedia(
                                content  = content.trim(),
                                type     = postType,
                                pollOpt1 = opt1.trim(),
                                pollOpt2 = opt2.trim(),
                                imageUri = imageUri,
                                videoUri = videoUri
                            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Sélecteur de type ─────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("normal" to "📢 Rumeur", "poll" to "📊 Sondage", "confession" to "🎭 Confession")
                    .forEach { (type, label) ->
                        FilterChip(
                            selected = postType == type,
                            onClick  = { postType = type; imageUri = null; videoUri = null },
                            label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
            }

            // ── Info confession ───────────────────────────────────────────────
            if (postType == "confession") {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🕵️", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Text("Ton identité sera cachée.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                    "confession" -> "Ta confession (anonyme)…"
                    else         -> "Askip… qu'est-ce qui se passe ? 👀"
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (content.isNotEmpty()) {
                Text(
                    "${content.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (content.length > 450) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Aperçu image ──────────────────────────────────────────────────
            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model            = imageUri,
                        contentDescription = null,
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick  = { imageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = 0.55f)) {
                            Icon(Icons.Default.Close, null, tint = Color.White,
                                modifier = Modifier.padding(4.dp).size(18.dp))
                        }
                    }
                }
            }

            // ── Aperçu vidéo ──────────────────────────────────────────────────
            if (videoUri != null) {
                Surface(
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.PlayCircle, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Vidéo sélectionnée", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { videoUri = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Boutons médias (pas sur sondage/confession) ───────────────────
            if (postType == "normal") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape   = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Photo")
                    }
                    OutlinedButton(
                        onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                        shape   = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.VideoCall, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Vidéo")
                    }
                }
            }

            // ── Options sondage ───────────────────────────────────────────────
            if (postType == "poll") {
                Text("Options", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = opt1, onValueChange = { if (it.length <= 60) opt1 = it },
                    label = { Text("Option A") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = opt2, onValueChange = { if (it.length <= 60) opt2 = it },
                    label = { Text("Option B") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Barre de progression upload ───────────────────────────────────
            if (loading && uploadProgress > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { uploadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        if (uploadProgress < 100) "Upload : $uploadProgress%" else "Publication…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}