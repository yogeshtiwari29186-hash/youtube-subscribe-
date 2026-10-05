package com.example.data.repository

import com.example.auth.FirebaseUserData
import com.example.data.local.AppDatabase
import com.example.data.remote.FirebaseCloudSync
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DiamondTransaction
import com.example.data.model.EarnTask
import com.example.data.model.EconomySettings
import com.example.data.model.NotificationType
import com.example.data.model.PromotionItem
import com.example.data.model.PromotionStatus
import com.example.data.model.PromotionType
import com.example.data.model.ReportItem
import com.example.data.model.TaskCompletion
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserAccount
import com.example.data.model.WalletOverview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class OperationResult<out T> {
    data class Success<out T>(val data: T) : OperationResult<T>()
    data class Error(val message: String) : OperationResult<Nothing>()
}

class CreatorDiamondRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val userDao = database.userDao()
    private val promotionDao = database.promotionDao()
    private val taskDao = database.taskDao()
    private val transactionDao = database.transactionDao()
    private val notificationDao = database.notificationDao()
    private val reportDao = database.reportDao()
    private val auditDao = database.auditDao()

    private val _currentUserId = MutableStateFlow("user_main_creator")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _economySettings = MutableStateFlow(EconomySettings())
    val economySettings: StateFlow<EconomySettings> = _economySettings.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    fun getCurrentUserFlow(): Flow<UserAccount?> {
        return userDao.getUser(_currentUserId.value)
    }

    fun getAllUsersFlow(): Flow<List<UserAccount>> {
        return userDao.getAllUsers()
    }

    fun getAllPromotionsFlow(): Flow<List<PromotionItem>> {
        return promotionDao.getAllPromotions()
    }

    fun getMyPromotionsFlow(): Flow<List<PromotionItem>> {
        return promotionDao.getPromotionsByCreator(_currentUserId.value)
    }

    fun getPromotionByIdFlow(id: String): Flow<PromotionItem?> {
        return promotionDao.getPromotionById(id)
    }

    fun getAllTasksFlow(): Flow<List<EarnTask>> {
        return taskDao.getAllTasks()
    }

    fun getMyTransactionsFlow(): Flow<List<DiamondTransaction>> {
        return transactionDao.getTransactions(_currentUserId.value)
    }

    fun getAllTransactionsFlow(): Flow<List<DiamondTransaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getMyNotificationsFlow(): Flow<List<AppNotification>> {
        return notificationDao.getNotifications(_currentUserId.value)
    }

    fun getAllReportsFlow(): Flow<List<ReportItem>> {
        return reportDao.getAllReports()
    }

    fun getAllAuditsFlow(): Flow<List<AuditLog>> {
        return auditDao.getAllAudits()
    }

    fun getWalletOverviewFlow(): Flow<WalletOverview> {
        return combine(
            getCurrentUserFlow(),
            getMyTransactionsFlow(),
            getMyPromotionsFlow()
        ) { user, transactions, promotions ->
            val balance = user?.diamonds ?: 0L
            val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val spent = transactions.filter { it.amount < 0 }.sumOf { -it.amount }
            val activePromotions = promotions.count { it.status == PromotionStatus.ACTIVE }
            val pendingSpend = promotions.filter { it.status == PromotionStatus.PENDING }.sumOf { it.budget }

            WalletOverview(
                balance = balance,
                totalEarned = earned,
                totalSpent = spent,
                pendingBalance = pendingSpend,
                activePromotionsCount = activePromotions
            )
        }
    }

    // ==========================================
    // ATOMIC BACKEND WALLET & PROMOTION LOGIC
    // ==========================================

    suspend fun createPromotion(
        title: String,
        type: PromotionType,
        targetUrl: String,
        targetId: String,
        description: String,
        thumbnailUrl: String,
        budget: Long,
        durationDays: Int
    ): OperationResult<String> = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync(_currentUserId.value)
            ?: return@withContext OperationResult.Error("User session not found")

        if (user.isSuspended) {
            return@withContext OperationResult.Error("Your account is suspended from creating promotions.")
        }

        if (budget < _economySettings.value.minimumPromotionBudget) {
            return@withContext OperationResult.Error("Minimum promotion budget is ${_economySettings.value.minimumPromotionBudget} 💎.")
        }

        if (user.diamonds < budget) {
            return@withContext OperationResult.Error("Insufficient diamonds. You have ${user.diamonds} 💎, but need $budget 💎.")
        }

        val promotionId = "promo_${UUID.randomUUID().toString().take(8)}"
        val newBalance = user.diamonds - budget

        // 1. Atomic balance update
        userDao.updateDiamonds(user.userId, newBalance)

        // 2. Ledger transaction entry
        val transaction = DiamondTransaction(
            transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = user.userId,
            amount = -budget,
            type = TransactionType.SPENT,
            reason = "Promotion created: $title",
            timestamp = System.currentTimeMillis(),
            relatedId = promotionId,
            status = TransactionStatus.COMPLETED
        )
        transactionDao.insertTransaction(transaction)

        // 3. Promotion entry
        val promo = PromotionItem(
            promotionId = promotionId,
            creatorId = user.userId,
            creatorName = user.username,
            creatorAvatar = user.profileImage,
            type = type,
            targetUrl = targetUrl,
            targetId = targetId,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            budget = budget,
            remainingBudget = budget,
            durationDays = durationDays,
            status = PromotionStatus.ACTIVE,
            impressions = 0,
            clicks = 0,
            createdAt = System.currentTimeMillis()
        )
        promotionDao.insertPromotion(promo)

        // 4. In-app notification
        val notification = AppNotification(
            notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
            userId = user.userId,
            title = "Promotion Launched 🚀",
            message = "Your promotion for '$title' ($budget 💎) is now live in the community feed.",
            type = NotificationType.DIAMOND_SPENT,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)

        return@withContext OperationResult.Success(promotionId)
    }

    suspend fun completeEarnTask(taskId: String): OperationResult<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync(_currentUserId.value)
            ?: return@withContext OperationResult.Error("User not found")

        val task = taskDao.getTaskById(taskId)
            ?: return@withContext OperationResult.Error("Task not found")

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Anti-Fraud Checks
        if (task.isDaily) {
            if (user.lastCheckInDate == todayStr && task.actionType == "DAILY_CHECKIN") {
                return@withContext OperationResult.Error("Daily check-in already claimed for today! Come back tomorrow.")
            }
        } else {
            val count = taskDao.getCompletionCount(user.userId, taskId)
            if (count > 0) {
                return@withContext OperationResult.Error("This task has already been completed once.")
            }
        }

        val reward = task.reward
        val newBalance = user.diamonds + reward

        // Update user streak and balance
        val updatedUser = if (task.actionType == "DAILY_CHECKIN") {
            user.copy(
                diamonds = newBalance,
                lastCheckInDate = todayStr,
                checkInStreak = user.checkInStreak + 1,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            user.copy(
                diamonds = newBalance,
                updatedAt = System.currentTimeMillis()
            )
        }
        userDao.updateUser(updatedUser)

        // Record Task Completion
        val completion = TaskCompletion(
            completionId = "comp_${UUID.randomUUID().toString().take(8)}",
            userId = user.userId,
            taskId = taskId,
            reward = reward,
            completedAt = System.currentTimeMillis()
        )
        taskDao.insertTaskCompletion(completion)

        // Record Transaction Ledger
        val transaction = DiamondTransaction(
            transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = user.userId,
            amount = reward,
            type = TransactionType.EARNED,
            reason = "Task completed: ${task.title}",
            timestamp = System.currentTimeMillis(),
            relatedId = taskId,
            status = TransactionStatus.COMPLETED
        )
        transactionDao.insertTransaction(transaction)

        // Mark task state if one-time
        if (!task.isDaily) {
            taskDao.updateTask(task.copy(isCompleted = true))
        }

        // Notification
        notificationDao.insertNotification(
            AppNotification(
                notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.userId,
                title = "Reward Earned! 💎",
                message = "You earned +$reward 💎 for '${task.title}'.",
                type = NotificationType.DIAMOND_EARNED,
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext OperationResult.Success(reward)
    }

    suspend fun useFirebaseUser(account: FirebaseUserData): OperationResult<Unit> = withContext(Dispatchers.IO) {
        _currentUserId.value = account.uid
        val existing = userDao.getUserSync(account.uid)
        val user = existing?.copy(
            username = account.name.ifBlank { existing.username },
            email = account.email,
            profileImage = account.photoUrl,
            updatedAt = System.currentTimeMillis()
        ) ?: UserAccount(
            userId = account.uid,
            username = account.name.ifBlank { account.email.substringBefore("@").ifBlank { "Creator" } },
            email = account.email,
            profileImage = account.photoUrl,
            diamonds = 0L,
            creatorEnabled = true,
            isProfileComplete = false
        )
        userDao.insertUser(user)
        FirebaseCloudSync.saveUser(user)
        OperationResult.Success(Unit)
    }

    suspend fun updateProfile(
        username: String,
        bio: String,
        youtubeChannelUrl: String,
        youtubeChannelId: String
    ): OperationResult<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync(_currentUserId.value)
            ?: return@withContext OperationResult.Error("User not found")

        val updated = user.copy(
            username = username.trim().ifEmpty { user.username },
            bio = bio.trim(),
            youtubeChannelUrl = youtubeChannelUrl.trim(),
            youtubeChannelId = youtubeChannelId.trim(),
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateUser(updated)
        return@withContext OperationResult.Success(Unit)
    }


    suspend fun grantVerifiedYouTubeSubscriptionReward(promotionId: String): OperationResult<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync(_currentUserId.value)
            ?: return@withContext OperationResult.Error("User not found")
        val promotion = promotionDao.getPromotionByIdSync(promotionId)
            ?: return@withContext OperationResult.Error("Promotion not found")
        if (promotion.type != PromotionType.YOUTUBE_VIDEO && promotion.type != PromotionType.YOUTUBE_CHANNEL) {
            return@withContext OperationResult.Error("This promotion is not a YouTube subscription target.")
        }

        val relatedId = "youtube_subscription:$promotionId"
        val existing = transactionDao.getTransactions(user.userId).firstOrNull()
            ?.any { it.relatedId == relatedId && it.amount == 40L } == true
        if (existing) {
            return@withContext OperationResult.Error("40 💎 reward was already claimed for this creator.")
        }

        // Call this method only after an authorized YouTube subscription verifier
        // has positively confirmed that the current user subscribed to this creator.
        val reward = 40L
        userDao.updateDiamonds(user.userId, user.diamonds + reward)
        transactionDao.insertTransaction(
            DiamondTransaction(
                transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
                userId = user.userId,
                amount = reward,
                type = TransactionType.EARNED,
                reason = "Verified YouTube subscription: ${promotion.creatorName}",
                timestamp = System.currentTimeMillis(),
                relatedId = relatedId,
                status = TransactionStatus.COMPLETED
            )
        )
        notificationDao.insertNotification(
            AppNotification(
                notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.userId,
                title = "YouTube Subscription Reward 💎",
                message = "You earned +40 💎 for a verified subscription to ${promotion.creatorName}.",
                type = NotificationType.DIAMOND_EARNED,
                timestamp = System.currentTimeMillis()
            )
        )
        val creatorNotification = AppNotification(
            notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
            userId = promotion.creatorId,
            title = "New Verified Subscriber 🎉",
            message = "${user.username} subscribed to your YouTube promotion ${promotion.title}.",
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(creatorNotification)
        FirebaseCloudSync.saveUser(user.copy(diamonds = user.diamonds + reward))
        FirebaseCloudSync.saveNotification(creatorNotification)
        FirebaseCloudSync.recordSubscription(user, promotion)
        transactionDao.getTransactions(user.userId).firstOrNull()?.lastOrNull()?.let(FirebaseCloudSync::saveTransaction)
        OperationResult.Success(reward)
    }

    suspend fun submitReport(
        targetContentId: String,
        targetTitle: String,
        reason: String,
        details: String
    ): OperationResult<String> = withContext(Dispatchers.IO) {
        val reportId = "rep_${UUID.randomUUID().toString().take(8)}"
        val report = ReportItem(
            reportId = reportId,
            reporterUserId = _currentUserId.value,
            targetContentId = targetContentId,
            targetTitle = targetTitle,
            reason = reason,
            details = details,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        reportDao.insertReport(report)
        return@withContext OperationResult.Success(reportId)
    }

    suspend fun recordContentImpression(promotionId: String) = withContext(Dispatchers.IO) {
        promotionDao.incrementImpression(promotionId)
    }

    suspend fun recordContentClick(promotionId: String) = withContext(Dispatchers.IO) {
        promotionDao.incrementClick(promotionId)
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(_currentUserId.value)
    }

    // ==========================================
    // ADMIN ACTIONS & FRAUD AUDITING
    // ==========================================

    suspend fun adminAdjustDiamonds(
        targetUserId: String,
        delta: Long,
        reason: String
    ): OperationResult<Unit> = withContext(Dispatchers.IO) {
        if (reason.isBlank()) {
            return@withContext OperationResult.Error("An audit reason is strictly mandatory for diamond adjustments.")
        }
        val targetUser = userDao.getUserSync(targetUserId)
            ?: return@withContext OperationResult.Error("User not found")

        val newBalance = (targetUser.diamonds + delta).coerceAtLeast(0L)
        userDao.updateDiamonds(targetUserId, newBalance)

        val auditId = "audit_${UUID.randomUUID().toString().take(8)}"
        auditDao.insertAudit(
            AuditLog(
                auditId = auditId,
                adminName = "SystemAdmin",
                action = if (delta >= 0) "CREDIT_DIAMONDS" else "DEBIT_DIAMONDS",
                targetUserId = targetUserId,
                diamondDelta = delta,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )

        transactionDao.insertTransaction(
            DiamondTransaction(
                transactionId = "tx_${UUID.randomUUID().toString().take(8)}",
                userId = targetUserId,
                amount = delta,
                type = TransactionType.ADJUSTMENT,
                reason = "Admin Adjustment: $reason",
                timestamp = System.currentTimeMillis(),
                relatedId = auditId
            )
        )

        notificationDao.insertNotification(
            AppNotification(
                notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = targetUserId,
                title = "Diamond Balance Adjusted",
                message = "Your wallet was adjusted by ${if (delta >= 0) "+$delta" else "$delta"} 💎 ($reason)",
                type = NotificationType.SYSTEM_ANNOUNCEMENT,
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext OperationResult.Success(Unit)
    }

    suspend fun adminUpdatePromotionStatus(
        promotionId: String,
        status: PromotionStatus,
        reason: String
    ): OperationResult<Unit> = withContext(Dispatchers.IO) {
        val promo = promotionDao.getPromotionByIdSync(promotionId)
            ?: return@withContext OperationResult.Error("Promotion not found")

        promotionDao.updatePromotionStatus(promotionId, status)

        auditDao.insertAudit(
            AuditLog(
                auditId = "audit_${UUID.randomUUID().toString().take(8)}",
                adminName = "SystemAdmin",
                action = "PROMOTION_STATUS_CHANGE_${status.name}",
                targetUserId = promo.creatorId,
                diamondDelta = 0L,
                reason = "Promotion status set to ${status.name}. Reason: $reason",
                timestamp = System.currentTimeMillis()
            )
        )

        val notifType = if (status == PromotionStatus.ACTIVE) NotificationType.PROMOTION_APPROVED else NotificationType.PROMOTION_REJECTED
        notificationDao.insertNotification(
            AppNotification(
                notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = promo.creatorId,
                title = "Promotion Status Update",
                message = "Your promotion '${promo.title}' status was updated to ${status.name}.",
                type = notifType,
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext OperationResult.Success(Unit)
    }

    suspend fun adminToggleUserSuspension(targetUserId: String, suspend: Boolean, reason: String): OperationResult<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserSync(targetUserId)
            ?: return@withContext OperationResult.Error("User not found")

        userDao.updateUser(user.copy(isSuspended = suspend))

        auditDao.insertAudit(
            AuditLog(
                auditId = "audit_${UUID.randomUUID().toString().take(8)}",
                adminName = "SystemAdmin",
                action = if (suspend) "SUSPEND_USER" else "RESTORE_USER",
                targetUserId = targetUserId,
                diamondDelta = 0L,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
        return@withContext OperationResult.Success(Unit)
    }

    fun updateEconomySettings(settings: EconomySettings) {
        _economySettings.value = settings
    }

    // ==========================================
    // PRE-SEEDED INITIAL DATA & REAL CONTENT
    // ==========================================

    private suspend fun seedInitialDataIfEmpty() {
        val existingUser = userDao.getUserSync("user_main_creator")
        if (existingUser == null) {
            val mainUser = UserAccount(
                userId = "user_main_creator",
                username = "AlexCreator",
                email = "alex.creator@creatordiamond.com",
                profileImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                bio = "Tech creator & cinematic visual storyteller. Building high-impact video essays and creative tech tutorials.",
                diamonds = 2450L, // Matches prompt: "💎 2,450 Diamonds"
                youtubeChannelId = "UC_alexcreator_tech",
                youtubeChannelUrl = "https://www.youtube.com/@AlexCreatorTech",
                creatorEnabled = true,
                followersCount = 428,
                followingCount = 65,
                checkInStreak = 4
            )
            userDao.insertUser(mainUser)

            // Initial transactions matching prompt example:
            // "+25 💎 Profile completed Today", "-100 💎 Promotion created Today", "+2,525 💎 Welcome Activity"
            val now = System.currentTimeMillis()
            transactionDao.insertTransaction(
                DiamondTransaction(
                    transactionId = "tx_init_1",
                    userId = mainUser.userId,
                    amount = 2525L,
                    type = TransactionType.BONUS,
                    reason = "Welcome Creator Bonus",
                    timestamp = now - 86400000L
                )
            )
            transactionDao.insertTransaction(
                DiamondTransaction(
                    transactionId = "tx_init_2",
                    userId = mainUser.userId,
                    amount = 25L,
                    type = TransactionType.EARNED,
                    reason = "Profile completed",
                    timestamp = now - 3600000L,
                    relatedId = "task_profile"
                )
            )
            transactionDao.insertTransaction(
                DiamondTransaction(
                    transactionId = "tx_init_3",
                    userId = mainUser.userId,
                    amount = -100L,
                    type = TransactionType.SPENT,
                    reason = "Promotion created: Cyberpunk B-Roll Masterclass",
                    timestamp = now - 1800000L,
                    relatedId = "promo_1"
                )
            )

            // Seed sample active promotion by current user
            promotionDao.insertPromotion(
                PromotionItem(
                    promotionId = "promo_1",
                    creatorId = mainUser.userId,
                    creatorName = mainUser.username,
                    creatorAvatar = mainUser.profileImage,
                    type = PromotionType.YOUTUBE_VIDEO,
                    targetUrl = "https://www.youtube.com/watch?v=a1B2c3D4e5F",
                    targetId = "a1B2c3D4e5F",
                    title = "Cyberpunk B-Roll Masterclass: Color Grading & Lighting",
                    description = "Learn how to achieve cinematic neon tones, custom LUT workflows, and camera tracking.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800&q=80",
                    budget = 100L,
                    remainingBudget = 84L,
                    durationDays = 3,
                    status = PromotionStatus.ACTIVE,
                    impressions = 342,
                    clicks = 58
                )
            )

            // Seed other community creator promotions (so feed is rich and vibrant)
            val communityPromos = listOf(
                PromotionItem(
                    promotionId = "promo_2",
                    creatorId = "creator_maya",
                    creatorName = "Maya SoundLab",
                    creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&q=80",
                    type = PromotionType.YOUTUBE_VIDEO,
                    targetUrl = "https://www.youtube.com/watch?v=x9Y8z7W6v5U",
                    targetId = "x9Y8z7W6v5U",
                    title = "How I Mix Cinematic Vocals in Logic Pro (2026 Studio Tour)",
                    description = "Full breakdown of acoustic treatment, compressor chain, and vocal parallel processing.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&q=80",
                    budget = 500L,
                    remainingBudget = 390L,
                    durationDays = 7,
                    status = PromotionStatus.ACTIVE,
                    impressions = 890,
                    clicks = 142
                ),
                PromotionItem(
                    promotionId = "promo_3",
                    creatorId = "creator_dev_kai",
                    creatorName = "Kai DevLog",
                    creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80",
                    type = PromotionType.YOUTUBE_CHANNEL,
                    targetUrl = "https://www.youtube.com/@KaiDevLog",
                    targetId = "@KaiDevLog",
                    title = "Indie Game Dev Journey: Unreal Engine 5.5 Solo Project",
                    description = "Devlogs, mechanics prototyping, shader magic, and indie publishing advice.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                    budget = 250L,
                    remainingBudget = 180L,
                    durationDays = 5,
                    status = PromotionStatus.ACTIVE,
                    impressions = 612,
                    clicks = 94
                ),
                PromotionItem(
                    promotionId = "promo_4",
                    creatorId = "creator_elena_art",
                    creatorName = "Elena Digital Art",
                    creatorAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80",
                    type = PromotionType.CREATOR_PROFILE,
                    targetUrl = "https://www.youtube.com/@ElenaDigitalArt",
                    targetId = "@ElenaDigitalArt",
                    title = "Elena Art Studio: Procreate Character Design & Concept Art",
                    description = "Weekly speedpaints, brush sets, and color theory tips for digital illustrators.",
                    thumbnailUrl = "https://images.unsplash.com/photo-1561055657-b9e0bf0fa360?w=800&q=80",
                    budget = 300L,
                    remainingBudget = 260L,
                    durationDays = 4,
                    status = PromotionStatus.ACTIVE,
                    impressions = 450,
                    clicks = 78
                )
            )
            communityPromos.forEach { promotionDao.insertPromotion(it) }

            // Seed Tasks
            val tasks = listOf(
                EarnTask(
                    taskId = "task_daily",
                    title = "Daily Check-in",
                    description = "Claim your daily diamond reward to build your consecutive login streak.",
                    reward = 10L,
                    category = "DAILY",
                    isDaily = true,
                    actionType = "DAILY_CHECKIN"
                ),
                EarnTask(
                    taskId = "task_profile",
                    title = "Profile Completion",
                    description = "Add a bio, display name, and avatar to complete your creator identity.",
                    reward = 25L,
                    category = "ONBOARDING",
                    isDaily = false,
                    actionType = "PROFILE_COMPLETE",
                    isCompleted = true
                ),
                EarnTask(
                    taskId = "task_guidelines",
                    title = "Creator Community Guidelines Quiz",
                    description = "Review our YouTube-compliant, non-incentivized community standards.",
                    reward = 15L,
                    category = "COMMUNITY",
                    isDaily = false,
                    actionType = "COMMUNITY_QUIZ"
                ),
                EarnTask(
                    taskId = "task_link_yt",
                    title = "Link Your YouTube Channel",
                    description = "Connect your verified YouTube channel handle to unlock instant promotion pre-fills.",
                    reward = 50L,
                    category = "CREATOR",
                    isDaily = false,
                    actionType = "LINK_YOUTUBE"
                ),
                EarnTask(
                    taskId = "task_tutorial",
                    title = "Complete App Tour & Tutorial",
                    description = "Learn how to discover content, budget promotions, and manage your diamond wallet.",
                    reward = 20L,
                    category = "ONBOARDING",
                    isDaily = false,
                    actionType = "APP_TOUR"
                ),
                EarnTask(
                    taskId = "task_referral",
                    title = "Invite a Fellow Creator",
                    description = "Share your unique creator referral invite code with other creators.",
                    reward = 30L,
                    category = "COMMUNITY",
                    isDaily = false,
                    actionType = "REFERRAL_CODE"
                ),
                EarnTask(
                    taskId = "task_weekly_challenge",
                    title = "Weekly Creator Challenge",
                    description = "Participate in this week's community spotlight topic showcase.",
                    reward = 50L,
                    category = "COMMUNITY",
                    isDaily = false,
                    actionType = "WEEKLY_CHALLENGE"
                )
            )
            taskDao.insertTasks(tasks)

            // Notifications
            val notifs = listOf(
                AppNotification(
                    notificationId = "notif_1",
                    userId = mainUser.userId,
                    title = "Welcome to CreatorDiamond! 💎",
                    message = "Your unified Viewer + Creator account has been activated with 2,450 Diamonds.",
                    type = NotificationType.SYSTEM_ANNOUNCEMENT,
                    timestamp = now - 7200000L
                ),
                AppNotification(
                    notificationId = "notif_2",
                    userId = mainUser.userId,
                    title = "Promotion Approved & Live",
                    message = "Your promotion for 'Cyberpunk B-Roll Masterclass' is now circulating.",
                    type = NotificationType.PROMOTION_APPROVED,
                    timestamp = now - 1800000L
                ),
                AppNotification(
                    notificationId = "notif_3",
                    userId = mainUser.userId,
                    title = "+25 💎 Task Reward",
                    message = "You earned 25 diamonds for completing your creator profile.",
                    type = NotificationType.DIAMOND_EARNED,
                    timestamp = now - 3600000L
                )
            )
            notifs.forEach { notificationDao.insertNotification(it) }
        }
    }
}
