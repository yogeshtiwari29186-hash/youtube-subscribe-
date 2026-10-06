package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DiamondTransaction
import com.example.data.model.EarnTask
import com.example.data.model.EconomySettings
import com.example.data.model.PromotionItem
import com.example.data.model.PromotionStatus
import com.example.data.model.PromotionType
import com.example.data.model.ReportItem
import com.example.data.model.TransactionType
import com.example.data.model.UserAccount
import com.example.data.model.WalletOverview
import android.content.Intent
import com.example.auth.FirebaseGoogleAuth
import com.example.auth.FirebaseUserData
import com.example.data.remote.FirebaseCloudSync
import com.example.data.repository.CreatorDiamondRepository
import com.example.data.repository.OperationResult
import com.example.util.YouTubeUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object YouTubeAuthorization : Screen("youtube_authorization")
    object Onboarding : Screen("onboarding")
    object ChannelSetup : Screen("channel_setup")
    object QuickVideoPromotion : Screen("quick_video_promotion")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Earn : Screen("earn")
    object TaskDetails : Screen("task_details")
    object ContentFeed : Screen("content_feed")
    object ContentDetails : Screen("content_details")
    object CreatePromotion : Screen("create_promotion")
    object SelectPromotionType : Screen("select_promotion_type")
    object AddYouTubeChannel : Screen("add_youtube_channel")
    object AddYouTubeVideo : Screen("add_youtube_video")
    object PromotionBudget : Screen("promotion_budget")
    object PromotionConfirmation : Screen("promotion_confirmation")
    object PromotionSuccess : Screen("promotion_success")
    object MyContent : Screen("my_content")
    object CreatorProfile : Screen("creator_profile")
    object Wallet : Screen("wallet")
    object TransactionHistory : Screen("transaction_history")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object ReportContent : Screen("report_content")
    object HelpSupport : Screen("help_support")
    object Terms : Screen("terms")
    object PrivacyPolicy : Screen("privacy_policy")
    object AdminPanel : Screen("admin_panel")
}

data class PromotionCreationState(
    val type: PromotionType = PromotionType.YOUTUBE_VIDEO,
    val title: String = "",
    val targetUrl: String = "",
    val targetId: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val budget: Long = 500L,
    val durationDays: Int = 3,
    val isResolvingMetadata: Boolean = false,
    val resolutionSuccess: Boolean = false,
    val resolutionError: String? = null,
    val isSubmitting: Boolean = false,
    val createdPromotionId: String? = null,
    val submitError: String? = null
)

class CreatorDiamondViewModel(
    private val repository: CreatorDiamondRepository
) : ViewModel() {

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    fun completeGoogleSignIn(data: Intent?, nameOverride: String? = null) {
        _authLoading.value = true
        FirebaseGoogleAuth.completeSignIn(data, nameOverride) { result ->
            result.onSuccess { finishAuth(it, if(nameOverride.isNullOrBlank()) "Login successful" else "Account created successfully") }
                .onFailure { _authLoading.value=false; viewModelScope.launch{_uiEvents.emit("Login failed: "+(it.message?:"Try again"))} }
        }
    }

    fun loginWithEmail(email:String,password:String){
        _authLoading.value=true
        FirebaseGoogleAuth.signInWithEmail(email,password){result->
            result.onSuccess{finishAuth(it,"Login successful")}
                .onFailure{_authLoading.value=false;viewModelScope.launch{_uiEvents.emit("Login failed: "+(it.message?:"Check email and password"))}}
        }
    }

    fun registerWithEmail(name:String,email:String,password:String){
        _authLoading.value=true
        FirebaseGoogleAuth.createAccountWithEmail(name,email,password){result->
            result.onSuccess{finishAuth(it,"Account created successfully")}
                .onFailure{_authLoading.value=false;viewModelScope.launch{_uiEvents.emit("Signup failed: "+(it.message?:"Check your details"))}}
        }
    }

    fun resetPassword(email:String){
        _authLoading.value=true
        FirebaseGoogleAuth.sendPasswordReset(email){result->
            _authLoading.value=false
            viewModelScope.launch{_uiEvents.emit(if(result.isSuccess)"Password reset email sent" else "Reset failed: "+(result.exceptionOrNull()?.message?:"Try again"))}
        }
    }

    private fun finishAuth(account: FirebaseUserData, message:String){
        viewModelScope.launch{
            when(val op=repository.useFirebaseUser(account)){
                is OperationResult.Success->{_authLoading.value=false;_uiEvents.emit(message);navigateTo(if (repository.isCurrentUserProfileComplete()) Screen.Home else Screen.ChannelSetup)}
                is OperationResult.Error->{_authLoading.value=false;_uiEvents.emit(op.message)}
            }
        }
    }
    init {
        FirebaseGoogleAuth.currentUser()?.let { user ->
            viewModelScope.launch {
                repository.useFirebaseUser(
                    FirebaseUserData(
                        uid = user.uid,
                        name = user.displayName.orEmpty(),
                        email = user.email.orEmpty(),
                        photoUrl = user.photoUrl?.toString().orEmpty()
                    )
                )
            }
        }
    }

    fun finishSplash() {
        navigateTo(Screen.YouTubeAuthorization)
    }

    fun finishYouTubeAuthorization() {
        if (FirebaseGoogleAuth.currentUser() != null) navigateTo(Screen.Home)
        else navigateTo(Screen.Login)
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // User & Economy Flows
    val currentUser: StateFlow<UserAccount?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserAccount>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletOverview: StateFlow<WalletOverview> = repository.getWalletOverviewFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletOverview(0L, 0L, 0L, 0L, 0))

    val promotions: StateFlow<List<PromotionItem>> = repository.getAllPromotionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPromotions: StateFlow<List<PromotionItem>> = repository.getMyPromotionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<EarnTask>> = repository.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<DiamondTransaction>> = repository.getMyTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<DiamondTransaction>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.getMyNotificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportItem>> = repository.getAllReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.getAllAuditsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val economySettings: StateFlow<EconomySettings> = repository.economySettings

    // Transient UI selection states
    val selectedPromotion = MutableStateFlow<PromotionItem?>(null)
    val selectedTask = MutableStateFlow<EarnTask?>(null)
    val selectedCreator = MutableStateFlow<UserAccount?>(null)

    // Promotion Creation Flow State
    private val _creationState = MutableStateFlow(PromotionCreationState())
    val creationState: StateFlow<PromotionCreationState> = _creationState.asStateFlow()

    // Global Message / Toast Events
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    // Navigation Helper
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            true
        } else {
            if (_currentScreen.value != Screen.Home) {
                _currentScreen.value = Screen.Home
                true
            } else {
                false
            }
        }
    }

    fun openContentDetails(promo: PromotionItem) {
        selectedPromotion.value = promo
        viewModelScope.launch {
            repository.recordContentImpression(promo.promotionId)
        }
        navigateTo(Screen.ContentDetails)
    }

    fun openTaskDetails(task: EarnTask) {
        selectedTask.value = task
        navigateTo(Screen.TaskDetails)
    }

    fun openCreatorProfile(user: UserAccount) {
        selectedCreator.value = user
        navigateTo(Screen.CreatorProfile)
    }

    // Promotion Creation Steps
    fun startPromotionCreation() {
        _creationState.value = PromotionCreationState()
        navigateTo(Screen.SelectPromotionType)
    }

    fun setPromotionType(type: PromotionType) {
        _creationState.value = _creationState.value.copy(type = type)
        when (type) {
            PromotionType.YOUTUBE_VIDEO -> navigateTo(Screen.AddYouTubeVideo)
            PromotionType.YOUTUBE_CHANNEL -> navigateTo(Screen.AddYouTubeChannel)
            PromotionType.CREATOR_PROFILE -> {
                // Auto-fill from current user
                val current = currentUser.value
                _creationState.value = _creationState.value.copy(
                    title = current?.username ?: "Creator Profile",
                    description = current?.bio ?: "Discover my creative channel & original videos",
                    targetUrl = current?.youtubeChannelUrl.takeIf { !it.isNullOrEmpty() } ?: "https://www.youtube.com/@AlexCreatorTech",
                    targetId = current?.userId ?: "user_main_creator",
                    thumbnailUrl = current?.profileImage ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                    resolutionSuccess = true
                )
                navigateTo(Screen.PromotionBudget)
            }
        }
    }

    fun resolveYouTubeVideo(url: String, title: String, description: String) {
        val parsed = YouTubeUtils.parseVideoUrl(url)
        if (parsed != null) {
            _creationState.value = _creationState.value.copy(
                targetUrl = parsed.canonicalUrl,
                targetId = parsed.videoId,
                title = title.ifBlank { "Featured YouTube Video ($parsed.videoId)" },
                description = description.ifBlank { "Watch and discover this video on YouTube." },
                thumbnailUrl = parsed.thumbnailUrl,
                resolutionSuccess = true,
                resolutionError = null
            )
            navigateTo(Screen.PromotionBudget)
        } else {
            _creationState.value = _creationState.value.copy(
                resolutionError = "Invalid YouTube video URL. Example: https://www.youtube.com/watch?v=a1B2c3D4e5F or https://youtu.be/..."
            )
        }
    }

    fun resolveYouTubeChannel(url: String, title: String, description: String) {
        val parsed = YouTubeUtils.parseChannelUrl(url)
        if (parsed != null) {
            _creationState.value = _creationState.value.copy(
                targetUrl = parsed.canonicalUrl,
                targetId = parsed.channelIdentifier,
                title = title.ifBlank { "Official Channel: ${parsed.channelIdentifier}" },
                description = description.ifBlank { "Subscribe and discover high quality creative videos." },
                thumbnailUrl = parsed.avatarUrl,
                resolutionSuccess = true,
                resolutionError = null
            )
            navigateTo(Screen.PromotionBudget)
        } else {
            _creationState.value = _creationState.value.copy(
                resolutionError = "Invalid YouTube channel URL or handle. Example: https://www.youtube.com/@ChannelHandle or @MyHandle"
            )
        }
    }

    fun setBudgetAndDuration(budget: Long, durationDays: Int) {
        val daysFromDiamonds = (budget / 100L).coerceAtLeast(1L).toInt()
        _creationState.value = _creationState.value.copy(
            budget = budget,
            durationDays = daysFromDiamonds
        )
        navigateTo(Screen.PromotionConfirmation)
    }

    fun confirmAndLaunchPromotion() {
        val state = _creationState.value
        _creationState.value = state.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            val result = repository.createPromotion(
                title = state.title,
                type = state.type,
                targetUrl = state.targetUrl,
                targetId = state.targetId,
                description = state.description,
                thumbnailUrl = state.thumbnailUrl,
                budget = state.budget,
                durationDays = state.durationDays
            )

            when (result) {
                is OperationResult.Success -> {
                    _creationState.value = _creationState.value.copy(
                        isSubmitting = false,
                        createdPromotionId = result.data,
                        submitError = null
                    )
                    _uiEvents.emit("Promotion Created Successfully! ID: ${result.data}")
                    navigateTo(Screen.PromotionSuccess)
                }
                is OperationResult.Error -> {
                    _creationState.value = _creationState.value.copy(
                        isSubmitting = false,
                        submitError = result.message
                    )
                    _uiEvents.emit("Error: ${result.message}")
                }
            }
        }
    }

    fun promoteToTopList(promotionId: String) {
        viewModelScope.launch {
            when (val result = repository.promoteToTopList(promotionId)) {
                is OperationResult.Success -> _uiEvents.emit("Top List active for 24 hours. -100 💎")
                is OperationResult.Error -> _uiEvents.emit(result.message)
            }
        }
    }

    fun claimTaskReward(taskId: String) {
        viewModelScope.launch {
            when (val result = repository.completeEarnTask(taskId)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Claimed +${result.data} Diamonds! 💎")
                    navigateBack()
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }


    fun onYouTubeSubscriptionReturn() {
        viewModelScope.launch {
            _uiEvents.emit("YouTube se wapas aaye. Subscription verification required hai; verification successful hone par hi +40 💎 milega.")
        }
    }

    fun applyVerifiedYouTubeSubscriptionReward(promotionId: String) {
        viewModelScope.launch {
            when (val result = repository.grantVerifiedYouTubeSubscriptionReward(promotionId)) {
                is OperationResult.Success -> _uiEvents.emit("+${result.data} Diamonds added! 💎")
                is OperationResult.Error -> _uiEvents.emit(result.message)
            }
        }
    }

    fun submitReport(reason: String, details: String) {
        val promo = selectedPromotion.value ?: return
        viewModelScope.launch {
            when (val result = repository.submitReport(promo.promotionId, promo.title, reason, details)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Report submitted. Ticket ID: ${result.data}")
                    navigateBack()
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }

    fun confirmChannelSetup(channelName: String, channelUrl: String, channelId: String, thumbnailUrl: String) {
        viewModelScope.launch {
            when (val result = repository.confirmChannelSetup(channelName, channelUrl, channelId, thumbnailUrl)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Channel connected successfully! +200 💎 welcome balance ready.")
                    navigateTo(Screen.QuickVideoPromotion)
                }
                is OperationResult.Error -> _uiEvents.emit(result.message)
            }
        }
    }

    fun createQuickVideoPromotion(videoUrl: String) {
        val parsed = YouTubeUtils.parseVideoUrl(videoUrl)
        if (parsed == null) {
            viewModelScope.launch { _uiEvents.emit("Invalid YouTube video link.") }
            return
        }
        viewModelScope.launch {
            when (val result = repository.createPromotion(
                title = "YouTube Video",
                type = PromotionType.YOUTUBE_VIDEO,
                targetUrl = parsed.canonicalUrl,
                targetId = parsed.videoId,
                description = "Promoted YouTube video",
                thumbnailUrl = parsed.thumbnailUrl,
                budget = 100L,
                durationDays = 1
            )) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Video promotion created for 100 💎.")
                    navigateTo(Screen.Home)
                }
                is OperationResult.Error -> _uiEvents.emit(result.message)
            }
        }
    }

    fun updateProfile(username: String, bio: String, youtubeChannelUrl: String, youtubeChannelId: String) {
        viewModelScope.launch {
            when (val result = repository.updateProfile(username, bio, youtubeChannelUrl, youtubeChannelId)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Profile updated successfully!")
                    navigateBack()
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }

    // Admin Operations
    fun adminAdjustDiamonds(userId: String, delta: Long, reason: String) {
        viewModelScope.launch {
            when (val result = repository.adminAdjustDiamonds(userId, delta, reason)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Admin adjustment logged. Diamonds adjusted by $delta 💎")
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }

    fun adminModeratePromotion(promoId: String, status: PromotionStatus, reason: String) {
        viewModelScope.launch {
            when (val result = repository.adminUpdatePromotionStatus(promoId, status, reason)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("Promotion updated to ${status.name}")
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }

    fun adminToggleUserSuspension(userId: String, suspend: Boolean, reason: String) {
        viewModelScope.launch {
            when (val result = repository.adminToggleUserSuspension(userId, suspend, reason)) {
                is OperationResult.Success -> {
                    _uiEvents.emit("User ${if (suspend) "suspended" else "restored"}")
                }
                is OperationResult.Error -> {
                    _uiEvents.emit(result.message)
                }
            }
        }
    }

    fun updateEconomySettings(settings: EconomySettings) {
        repository.updateEconomySettings(settings)
        viewModelScope.launch {
            _uiEvents.emit("Live economy configuration saved!")
        }
    }
}

class CreatorDiamondViewModelFactory(
    private val repository: CreatorDiamondRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatorDiamondViewModel::class.java)) {
            return CreatorDiamondViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
