package com.rnandresy.lol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rnandresy.lol.model.*
import com.rnandresy.lol.repository.FirebaseRepository
import com.rnandresy.lol.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AskipViewModel(application: Application) : AndroidViewModel(application) {

    private val repo     = FirebaseRepository()
    private val notif    = NotificationHelper(application)
    private val settings = SettingsRepository(application)
    val dataTracker      = DataUsageTracker()

    // ── Auth ──────────────────────────────────────────────────────────────────

    val isLoggedIn    = MutableStateFlow(repo.isLoggedIn())
    val currentUserId get() = repo.currentUserId()
    val currentEmail  get() = repo.currentEmail()

    // ── Profiles ──────────────────────────────────────────────────────────────

    private val _myProfile     = MutableStateFlow<UserProfile?>(null)
    val myProfile: StateFlow<UserProfile?> = _myProfile

    private val _viewedProfile = MutableStateFlow<UserProfile?>(null)
    val viewedProfile: StateFlow<UserProfile?> = _viewedProfile

    private val _allProfiles   = MutableStateFlow<List<UserProfile>>(emptyList())
    val allProfiles: StateFlow<List<UserProfile>> = _allProfiles

    // ── Posts & Stories ───────────────────────────────────────────────────────

    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())

    val feedPosts: StateFlow<List<Post>> = _allPosts
        .map { posts ->
            posts.filter { it.postType != "confession" }
                .sortedWith(compareByDescending<Post> { it.isPinned }.thenByDescending { it.timestamp })
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val confessions: StateFlow<List<Post>> = _allPosts
        .map { it.filter { p -> p.postType == "confession" }.sortedByDescending { it.timestamp } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _stories      = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // ── Comments ──────────────────────────────────────────────────────────────

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    // ── Conversations ─────────────────────────────────────────────────────────

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // ── Badges ────────────────────────────────────────────────────────────────

    private val _allBadges = MutableStateFlow<List<Badge>>(emptyList())
    val allBadges: StateFlow<List<Badge>> = _allBadges

    private val _myBadges = MutableStateFlow<List<Badge>>(emptyList())
    val myBadges: StateFlow<List<Badge>> = _myBadges

    // ── Achievements ──────────────────────────────────────────────────────────

    private val _myAchievements     = MutableStateFlow<List<Achievement>>(emptyList())
    val myAchievements: StateFlow<List<Achievement>> = _myAchievements

    private val _viewedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val viewedAchievements: StateFlow<List<Achievement>> = _viewedAchievements

    // ── Settings ──────────────────────────────────────────────────────────────

    val notifyMessages   = settings.notifyMessages.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notifyPosts      = settings.notifyPosts.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val totalBytesStored = settings.totalBytes.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // ── UI ────────────────────────────────────────────────────────────────────

    val error   = MutableStateFlow<String?>(null)
    val loading = MutableStateFlow(false)

    private var postsJob:   Job? = null
    private var storiesJob: Job? = null
    private var convJob:    Job? = null
    private var msgJob:     Job? = null
    private var commentJob: Job? = null
    private var badgesJob:  Job? = null
    private var profileJob: Job? = null
    private var profilesJob: Job? = null

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        FirebaseAuth.getInstance().addAuthStateListener { fa ->
            isLoggedIn.value = fa.currentUser != null
            if (fa.currentUser != null) startAll() else stopAll()
        }
        if (repo.isLoggedIn()) startAll()
    }

    private fun startAll() {
        listenMyProfile()
        listenAllProfiles()
        listenPosts()
        listenStories()
        listenConversations()
        listenAllBadges()
    }

    private fun stopAll() {
        listOf(postsJob, storiesJob, convJob, msgJob, commentJob, badgesJob, profileJob, profilesJob)
            .forEach { it?.cancel() }
        _allPosts.value = emptyList()
        _stories.value  = emptyList()
        _conversations.value = emptyList()
        _messages.value = emptyList()
        _myProfile.value = null
        _allProfiles.value = emptyList()
        _allBadges.value = emptyList()
        _myBadges.value  = emptyList()
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) = viewModelScope.launch {
        loading.value = true
        runCatching { repo.login(email.trim(), password.trim()) }
            .onFailure { error.value = friendly(it.message) }
        loading.value = false
    }

    fun register(email: String, password: String, username: String) = viewModelScope.launch {
        loading.value = true
        runCatching {
            val uid = repo.register(email.trim(), password.trim())
            repo.createDefaultProfile(uid, username.trim())
        }.onFailure { error.value = friendly(it.message) }
        loading.value = false
    }

    fun logout() = viewModelScope.launch {
        val bytes = dataTracker.getSessionBytes()
        if (bytes > 0) settings.addBytes(bytes)
        runCatching { repo.logout() }
    }

    fun updateEmail(newEmail: String, pwd: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            runCatching { repo.updateEmail(newEmail, pwd) }
                .onSuccess { onDone(true, null) }
                .onFailure { onDone(false, it.message) }
        }

    fun updatePassword(current: String, newPwd: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            runCatching { repo.updatePassword(current, newPwd) }
                .onSuccess { onDone(true, null) }
                .onFailure { onDone(false, it.message) }
        }

    private fun friendly(msg: String?) = when {
        msg == null -> "Erreur inconnue"
        "password" in msg.lowercase()          -> "Mot de passe incorrect"
        "already" in msg.lowercase()           -> "Email déjà utilisé"
        "no user" in msg.lowercase()           -> "Aucun compte trouvé"
        "network" in msg.lowercase()           -> "Vérifiez votre connexion"
        "invalid" in msg.lowercase()           -> "Email ou mot de passe invalide"
        "weak" in msg.lowercase()              -> "Mot de passe trop faible (6 min)"
        else -> msg
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    private fun listenMyProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repo.listenToProfile(currentUserId).collect { profile ->
                _myProfile.value = profile
                profile?.let {
                    _myBadges.value = _allBadges.value.filter { b -> b.id in it.badgeIds }
                }
            }
        }
        viewModelScope.launch {
            _myAchievements.value = repo.getAchievements(currentUserId)
        }
    }

    private fun listenAllProfiles() {
        profilesJob?.cancel()
        profilesJob = viewModelScope.launch {
            repo.listenToAllProfiles().collect { profiles ->
                _allProfiles.value = profiles.filter { it.userId != currentUserId }
            }
        }
    }

    fun loadProfile(uid: String) = viewModelScope.launch {
        _viewedProfile.value  = repo.getProfile(uid)
        _viewedAchievements.value = repo.getAchievements(uid)
    }

    fun updateProfile(data: Map<String, Any?>, onDone: (() -> Unit)? = null) =
        viewModelScope.launch {
            runCatching {
                repo.updateProfile(currentUserId, data)
                onDone?.invoke()
            }.onFailure { error.value = it.message }
        }

    // ── Achievement engine ────────────────────────────────────────────────────

    private fun checkAchievements(profile: UserProfile) = viewModelScope.launch {
        val uid      = currentUserId
        val unlocked = _myAchievements.value.map { it.id }.toSet()
        suspend fun unlock(id: String) {
            if (id !in unlocked) {
                repo.unlockAchievement(uid, id)
                _myAchievements.value = repo.getAchievements(uid)
            }
        }
        if (profile.postsCount >= 1)                   unlock("first_post")
        if (profile.postsCount >= 10)                  unlock("ten_posts")
        if (profile.postsCount >= 25)                  unlock("twenty_five_p")
        if (profile.totalReactionsReceived >= 1)        unlock("first_react")
        if (profile.totalReactionsReceived >= 50)       unlock("popular")
        if (profile.totalReactionsReceived >= 200)      unlock("viral")
        if (profile.commentsCount >= 20)               unlock("commentator")
        if (profile.confessionsCount >= 1)             unlock("confessor")
        if (profile.confessionsCount >= 5)             unlock("dark_confessor")
        if (profile.pollsCount >= 3)                   unlock("poll_creator")
        if (profile.pollsCount >= 10)                  unlock("poll_master")
        if (profile.convsStarted >= 5)                 unlock("social")
        if (profile.storiesCount >= 5)                 unlock("storyteller")
        if (profile.streak >= 3)                       unlock("streak_3")
        if (profile.streak >= 7)                       unlock("streak_7")
        if (profile.hasBadgeENI)                       unlock("eni_pride")
        if (profile.badgeIds.isNotEmpty())             unlock("badge_maker")
    }

    // ── Badges ────────────────────────────────────────────────────────────────

    private fun listenAllBadges() {
        badgesJob?.cancel()
        badgesJob = viewModelScope.launch {
            repo.listenToAllBadges().collect { badges ->
                _allBadges.value = badges
                val myIds = _myProfile.value?.badgeIds ?: emptyList()
                _myBadges.value = badges.filter { it.id in myIds }
            }
        }
    }

    fun createOrWearBadge(
        displayName: String, colorHex: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val profile    = _myProfile.value ?: return
        val userIsAdmin = isAdmin(currentUserId) || profile.isAdmin
        viewModelScope.launch {
            try {
                val trimmed = displayName.trim()
                if (trimmed.isBlank())
                    return@launch onError("Le nom ne peut pas être vide")
                if (trimmed.lowercase() == ADMIN_BADGE_NAME)
                    return@launch onError("Ce nom est réservé")
                val existing = repo.findBadgeByName(trimmed)
                if (existing != null) {
                    if (_myBadges.value.any { it.id == existing.id })
                        return@launch onError("Tu portes déjà ce badge !")
                    if (!userIsAdmin && _myBadges.value.isNotEmpty())
                        return@launch onError("Retire ton badge actuel pour en porter un autre")
                    repo.wearBadge(existing.id, currentUserId)
                } else {
                    if (!userIsAdmin && _myBadges.value.isNotEmpty())
                        return@launch onError("Tu as déjà un badge. Retire-le d'abord")
                    repo.createBadge(trimmed, colorHex, currentUserId)
                }
                val updated = repo.getProfile(currentUserId)
                updated?.let { checkAchievements(it) }
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Erreur") }
        }
    }

    fun wearExistingBadge(badgeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val profile    = _myProfile.value ?: return
        val userIsAdmin = isAdmin(currentUserId) || profile.isAdmin
        viewModelScope.launch {
            try {
                val badge = _allBadges.value.find { it.id == badgeId }
                    ?: return@launch onError("Badge introuvable")
                if (badge.name == ADMIN_BADGE_NAME)
                    return@launch onError("Badge réservé à l'admin")
                if (_myBadges.value.any { it.id == badgeId })
                    return@launch onError("Tu portes déjà ce badge !")
                if (!userIsAdmin && _myBadges.value.isNotEmpty())
                    return@launch onError("Retire ton badge actuel avant")
                repo.wearBadge(badgeId, currentUserId)
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Erreur") }
        }
    }

    fun unwearBadge(badgeId: String) = viewModelScope.launch {
        runCatching { repo.unwearBadge(badgeId, currentUserId) }
    }

    fun updateBadge(
        badgeId: String, displayName: String, colorHex: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val profile    = _myProfile.value ?: return
        val userIsAdmin = isAdmin(currentUserId) || profile.isAdmin
        viewModelScope.launch {
            try {
                val badge = _allBadges.value.find { it.id == badgeId }
                    ?: return@launch onError("Badge introuvable")
                if (!userIsAdmin && badge.createdBy != currentUserId)
                    return@launch onError("Tu ne peux modifier que tes badges")
                val trimmed = displayName.trim()
                if (trimmed.isBlank()) return@launch onError("Nom vide")
                if (trimmed.lowercase() != badge.name) {
                    if (repo.findBadgeByName(trimmed) != null)
                        return@launch onError("Ce nom existe déjà")
                }
                repo.updateBadge(badgeId, trimmed, colorHex)
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Erreur") }
        }
    }

    fun deleteBadge(badgeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val profile    = _myProfile.value ?: return
        val userIsAdmin = isAdmin(currentUserId) || profile.isAdmin
        viewModelScope.launch {
            try {
                val badge = _allBadges.value.find { it.id == badgeId }
                    ?: return@launch onError("Badge introuvable")
                if (!userIsAdmin && badge.createdBy != currentUserId)
                    return@launch onError("Tu ne peux supprimer que tes badges")
                repo.deleteBadge(badgeId)
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Erreur") }
        }
    }

    // ── Posts ─────────────────────────────────────────────────────────────────

    private fun listenPosts() {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            repo.listenToPosts().collect { posts ->
                val prev = _allPosts.value
                if (prev.isNotEmpty() && notifyPosts.value) {
                    posts.firstOrNull { n -> prev.none { it.id == n.id } && n.userId != currentUserId }
                        ?.let { notif.showPostNotification(if (it.isAnonymous) "Quelqu'un 🎭" else it.username, it.content) }
                }
                _allPosts.value   = posts
                _isRefreshing.value = false
            }
        }
    }

    fun refreshFeed() {
        _isRefreshing.value = true
        listenPosts()
    }

    fun createPost(content: String, type: String = "normal",
                   pollOpt1: String = "", pollOpt2: String = "") {
        val profile = _myProfile.value ?: return
        val isConf  = type == "confession"
        viewModelScope.launch {
            runCatching {
                repo.createPost(mapOf(
                    "userId"       to currentUserId,
                    "username"     to if (isConf) "Quelqu'un 🎭" else profile.username,
                    "content"      to if (type != "poll") "Askip $content" else content,
                    "postType"     to type,
                    "isAnonymous"  to isConf,
                    "pollOption1"  to pollOpt1,
                    "pollOption2"  to pollOpt2,
                    "pollVotes1"   to 0,
                    "pollVotes2"   to 0,
                    "pollVoters"   to emptyList<String>(),
                    "likedBy"      to emptyList<String>(),
                    "fireBy"       to emptyList<String>(),
                    "lolBy"        to emptyList<String>(),
                    "shockBy"      to emptyList<String>(),
                    "eyesBy"       to emptyList<String>(),
                    "commentCount" to 0,
                    "isPinned"     to false,
                    "timestamp"    to System.currentTimeMillis()
                ))
                val counterField = when (type) {
                    "confession" -> "confessionsCount"
                    "poll"       -> "pollsCount"
                    else         -> "postsCount"
                }
                repo.incrementCounter(currentUserId, counterField)
                repo.updateStreak(currentUserId)
                repo.getProfile(currentUserId)?.let { checkAchievements(it) }
            }.onFailure { error.value = it.message }
        }
    }

    fun deletePost(postId: String) = viewModelScope.launch {
        runCatching { repo.deletePost(postId) }
    }

    fun togglePin(post: Post) = viewModelScope.launch {
        runCatching { repo.togglePin(post.id, post.isPinned) }
    }

    fun toggleReaction(post: Post, emoji: String) {
        val uid     = currentUserId
        val current = post.getUserReaction(uid)
        viewModelScope.launch {
            runCatching {
                if (current == emoji) repo.removeReaction(post.id, uid, emoji)
                else {
                    repo.addReaction(post.id, uid, emoji, current, post.userId)
                    repo.getProfile(post.userId)?.let { checkAchievements(it) }
                }
            }
        }
    }

    fun votePoll(postId: String, option: Int) {
        val uid  = currentUserId
        val post = _allPosts.value.find { it.id == postId } ?: return
        if (uid in post.pollVoters) return
        viewModelScope.launch { runCatching { repo.votePoll(postId, uid, option) } }
    }

    // ── Stories ───────────────────────────────────────────────────────────────

    private fun listenStories() {
        storiesJob?.cancel()
        storiesJob = viewModelScope.launch {
            repo.listenToStories().collect { _stories.value = it }
        }
    }

    fun createStory(content: String, emoji: String, bgColor: String) {
        val profile = _myProfile.value ?: return
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                repo.createStory(mapOf(
                    "userId"          to currentUserId,
                    "username"        to profile.username,
                    "content"         to content,
                    "emoji"           to emoji,
                    "backgroundColor" to bgColor,
                    "timestamp"       to now,
                    "expiresAt"       to (now + 24L * 3600_000L)
                ))
                repo.incrementCounter(currentUserId, "storiesCount")
                repo.updateStreak(currentUserId)
                repo.getProfile(currentUserId)?.let { checkAchievements(it) }
            }.onFailure { error.value = it.message }
        }
    }

    fun deleteStory(storyId: String) = viewModelScope.launch {
        runCatching { repo.deleteStory(storyId) }
    }

    // ── Comments ──────────────────────────────────────────────────────────────

    fun listenComments(postId: String) {
        commentJob?.cancel()
        commentJob = viewModelScope.launch {
            repo.listenToComments(postId).collect { _comments.value = it }
        }
    }

    fun addComment(postId: String, content: String) {
        val profile = _myProfile.value ?: return
        viewModelScope.launch {
            runCatching {
                repo.addComment(postId, mapOf(
                    "postId"    to postId,
                    "userId"    to currentUserId,
                    "username"  to profile.username,
                    "content"   to content,
                    "timestamp" to System.currentTimeMillis()
                ))
                repo.incrementCounter(currentUserId, "commentsCount")
                repo.updateStreak(currentUserId)
                repo.getProfile(currentUserId)?.let { checkAchievements(it) }
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) = viewModelScope.launch {
        runCatching { repo.deleteComment(postId, commentId) }
    }

    // ── Conversations ─────────────────────────────────────────────────────────

    private fun listenConversations() {
        convJob?.cancel()
        convJob = viewModelScope.launch {
            repo.listenToConversations(currentUserId).collect { _conversations.value = it }
        }
    }

    fun startConversation(otherId: String, otherUsername: String, onDone: (String) -> Unit) {
        val me = _myProfile.value ?: return
        viewModelScope.launch {
            runCatching {
                val convId = repo.getOrCreateConversation(currentUserId, me.username, otherId, otherUsername)
                repo.incrementCounter(currentUserId, "convsStarted")
                repo.getProfile(currentUserId)?.let { checkAchievements(it) }
                onDone(convId)
            }.onFailure { error.value = it.message }
        }
    }

    fun listenMessages(convId: String) {
        msgJob?.cancel()
        msgJob = viewModelScope.launch {
            repo.listenToMessages(convId).collect { newMsgs ->
                val prev = _messages.value
                if (prev.isNotEmpty() && notifyMessages.value) {
                    newMsgs.firstOrNull { n -> prev.none { it.id == n.id } && n.senderId != currentUserId }
                        ?.let { notif.showMessageNotification(it.senderUsername, it.content) }
                }
                _messages.value = newMsgs
            }
        }
    }

    fun sendMessage(convId: String, content: String) {
        val profile    = _myProfile.value ?: return
        val conv       = _conversations.value.find { it.id == convId } ?: return
        val receiverId = conv.participants.firstOrNull { it != currentUserId } ?: return
        viewModelScope.launch {
            runCatching {
                repo.sendMessage(convId, mapOf(
                    "conversationId" to convId,
                    "senderId"       to currentUserId,
                    "senderUsername" to profile.username,
                    "content"        to content,
                    "timestamp"      to System.currentTimeMillis()
                ), receiverId)
            }
        }
    }

    fun markRead(convId: String) = viewModelScope.launch { repo.markRead(convId, currentUserId) }

    fun getUnread(conv: Conversation) = (conv.unreadCounts[currentUserId] ?: 0L).toInt()

    // ── Settings ──────────────────────────────────────────────────────────────

    fun setNotifyMessages(v: Boolean) = viewModelScope.launch { settings.setNotifyMessages(v) }
    fun setNotifyPosts(v: Boolean)    = viewModelScope.launch { settings.setNotifyPosts(v) }
    fun clearError() { error.value = null }
}