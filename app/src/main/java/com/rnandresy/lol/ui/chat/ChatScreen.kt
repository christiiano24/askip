package com.rnandresy.lol.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnandresy.lol.ui.feed.UserAvatar
import com.rnandresy.lol.ui.feed.formatTs
import com.rnandresy.lol.utils.isAdmin
import com.rnandresy.lol.viewmodel.AskipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: AskipViewModel,
    convId: String,
    otherUserId: String,
    otherUsername: String,
    onOpenProfile: (String) -> Unit,
    onBack: () -> Unit
) {
    val messages  by vm.messages.collectAsState()
    val uid        = vm.currentUserId
    var text      by remember { mutableStateOf("") }
    val listState  = rememberLazyListState()

    LaunchedEffect(convId) {
        vm.listenMessages(convId)
        vm.markRead(convId)
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.clickable { onOpenProfile(otherUserId) }
                    ) {
                        UserAvatar(
                            username = otherUsername,
                            size     = 34,
                            isAdmin  = isAdmin(otherUserId)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                otherUsername,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Voir le profil",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = text,
                        onValueChange = { text = it },
                        placeholder   = { Text("Message…") },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(24.dp),
                        maxLines      = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                vm.sendMessage(convId, text.trim())
                                text = ""
                            }
                        },
                        enabled = text.isNotBlank()
                    ) { Icon(Icons.Default.Send, null) }
                }
            }
        }
    ) { pad ->
        LazyColumn(
            state               = listState,
            modifier            = Modifier.fillMaxSize().padding(pad),
            contentPadding      = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == uid
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    if (!isMe) {
                        UserAvatar(
                            username = msg.senderUsername,
                            size     = 28,
                            onClick  = { onOpenProfile(msg.senderId) }
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart    = 16.dp,
                                        topEnd      = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd   = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .background(
                                    if (isMe) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .widthIn(max = 280.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                msg.content,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            formatTs(msg.timestamp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                    if (isMe) Spacer(Modifier.width(6.dp))
                }
            }
        }
    }
}