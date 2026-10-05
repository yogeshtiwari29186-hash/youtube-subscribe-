package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PromotionType {
    YOUTUBE_VIDEO,
    YOUTUBE_CHANNEL,
    CREATOR_PROFILE
}

enum class PromotionStatus {
    ACTIVE,
    PENDING,
    COMPLETED,
    REJECTED,
    PAUSED
}

enum class TransactionType {
    EARNED,
    SPENT,
    ADJUSTMENT,
    BONUS
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

enum class NotificationType {
    PROMOTION_APPROVED,
    PROMOTION_REJECTED,
    PROMOTION_COMPLETED,
    DIAMOND_EARNED,
    DIAMOND_SPENT,
    SECURITY_ALERT,
    SYSTEM_ANNOUNCEMENT
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val userId: String,
    val username: String,
    val email: String,
    val profileImage: String = "",
    val bio: String = "",
    val diamonds: Long = 2450L, // Matches prompt sample 2,450 💎
    val youtubeChannelId: String = "",
    val youtubeChannelUrl: String = "",
    val creatorEnabled: Boolean = true, // Unified Viewer + Creator capability
    val followersCount: Int = 128,
    val followingCount: Int = 42,
    val isSuspended: Boolean = false,
    val isProfileComplete: Boolean = true,
    val lastCheckInDate: String = "",
    val checkInStreak: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "promotions")
data class PromotionItem(
    @PrimaryKey val promotionId: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val type: PromotionType,
    val targetUrl: String,
    val targetId: String, // Video ID or Channel ID
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val budget: Long,
    val remainingBudget: Long,
    val durationDays: Int,
    val status: PromotionStatus = PromotionStatus.ACTIVE,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (durationDays * 86400000L)
)

@Entity(tableName = "tasks")
data class EarnTask(
    @PrimaryKey val taskId: String,
    val title: String,
    val description: String,
    val reward: Long,
    val category: String,
    val isDaily: Boolean = false,
    val actionType: String,
    val isCompleted: Boolean = false,
    val cooldownHours: Int = 24
)

@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey val completionId: String,
    val userId: String,
    val taskId: String,
    val reward: Long,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diamond_transactions")
data class DiamondTransaction(
    @PrimaryKey val transactionId: String,
    val userId: String,
    val amount: Long, // Positive for earned/credit, negative for spent/debit
    val type: TransactionType,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedId: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey val notificationId: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "reports")
data class ReportItem(
    @PrimaryKey val reportId: String,
    val reporterUserId: String,
    val targetContentId: String,
    val targetTitle: String,
    val reason: String,
    val details: String,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val auditId: String,
    val adminName: String,
    val action: String,
    val targetUserId: String,
    val diamondDelta: Long,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WalletOverview(
    val balance: Long,
    val totalEarned: Long,
    val totalSpent: Long,
    val pendingBalance: Long,
    val activePromotionsCount: Int
)

data class EconomySettings(
    val dailyLimit: Long = 500L,
    val referralReward: Long = 30L,
    val minimumPromotionBudget: Long = 100L,
    val maximumWalletBalance: Long = 100000L,
    val costPerDayPer100Reach: Long = 100L
)
