package com.example.data.remote

import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DiamondTransaction
import com.example.data.model.PromotionItem
import com.example.data.model.ReportItem
import com.example.data.model.TaskCompletion
import com.example.data.model.UserAccount
import com.google.firebase.database.FirebaseDatabase

object FirebaseCloudSync {
    private val db = FirebaseDatabase.getInstance(
        "https://moodhub-8c69e-default-rtdb.firebaseio.com"
    ).reference

    private fun clean(value: String): String = value.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")

    fun saveUser(user: UserAccount) {
        db.child("users").child(clean(user.userId)).setValue(mapOf(
            "userId" to user.userId, "username" to user.username, "email" to user.email,
            "profileImage" to user.profileImage, "bio" to user.bio, "diamonds" to user.diamonds,
            "youtubeChannelId" to user.youtubeChannelId, "youtubeChannelUrl" to user.youtubeChannelUrl,
            "creatorEnabled" to user.creatorEnabled, "followersCount" to user.followersCount,
            "followingCount" to user.followingCount, "createdAt" to user.createdAt, "updatedAt" to user.updatedAt
        ))
    }

    fun savePromotion(promo: PromotionItem) {
        db.child("promotions").child(clean(promo.promotionId)).setValue(mapOf(
            "promotionId" to promo.promotionId, "creatorId" to promo.creatorId,
            "creatorName" to promo.creatorName, "creatorAvatar" to promo.creatorAvatar,
            "type" to promo.type.name, "targetUrl" to promo.targetUrl, "targetId" to promo.targetId,
            "title" to promo.title, "description" to promo.description, "thumbnailUrl" to promo.thumbnailUrl,
            "budget" to promo.budget, "remainingBudget" to promo.remainingBudget,
            "durationDays" to promo.durationDays, "status" to promo.status.name,
            "impressions" to promo.impressions, "clicks" to promo.clicks,
            "createdAt" to promo.createdAt, "expiresAt" to promo.expiresAt
        ))
    }

    fun saveTransaction(tx: DiamondTransaction) {
        db.child("transactions").child(clean(tx.transactionId)).setValue(mapOf(
            "transactionId" to tx.transactionId, "userId" to tx.userId, "amount" to tx.amount,
            "type" to tx.type.name, "reason" to tx.reason, "timestamp" to tx.timestamp,
            "relatedId" to tx.relatedId, "status" to tx.status.name
        ))
    }

    fun saveNotification(n: AppNotification) {
        db.child("notifications").child(clean(n.notificationId)).setValue(mapOf(
            "notificationId" to n.notificationId, "userId" to n.userId, "title" to n.title,
            "message" to n.message, "type" to n.type.name, "timestamp" to n.timestamp, "isRead" to n.isRead
        ))
    }

    fun saveTaskCompletion(completion: TaskCompletion) {
        db.child("taskCompletions").child(clean(completion.completionId)).setValue(mapOf(
            "completionId" to completion.completionId, "userId" to completion.userId,
            "taskId" to completion.taskId, "reward" to completion.reward, "completedAt" to completion.completedAt
        ))
    }

    fun saveReport(report: ReportItem) {
        db.child("reports").child(clean(report.reportId)).setValue(mapOf(
            "reportId" to report.reportId, "reporterUserId" to report.reporterUserId,
            "targetContentId" to report.targetContentId, "targetTitle" to report.targetTitle,
            "reason" to report.reason, "details" to report.details, "status" to report.status, "timestamp" to report.timestamp
        ))
    }

    fun saveAudit(audit: AuditLog) {
        db.child("auditLogs").child(clean(audit.auditId)).setValue(mapOf(
            "auditId" to audit.auditId, "adminName" to audit.adminName, "action" to audit.action,
            "targetUserId" to audit.targetUserId, "diamondDelta" to audit.diamondDelta,
            "reason" to audit.reason, "timestamp" to audit.timestamp
        ))
    }

    fun recordVideoView(viewer: UserAccount, promo: PromotionItem) {
        val id = viewer.userId + "_" + promo.promotionId
        db.child("contentViews").child(clean(promo.creatorId)).child(clean(promo.promotionId)).child(clean(id))
            .setValue(mapOf(
                "viewerId" to viewer.userId, "viewerName" to viewer.username, "viewerEmail" to viewer.email,
                "viewerAvatar" to viewer.profileImage, "promotionId" to promo.promotionId,
                "creatorId" to promo.creatorId, "title" to promo.title, "viewedAt" to System.currentTimeMillis()
            ))
    }

    fun recordSubscription(viewer: UserAccount, promo: PromotionItem) {
        val id = viewer.userId + "_" + promo.promotionId
        val payload = mapOf(
            "subscriberId" to viewer.userId, "subscriberName" to viewer.username,
            "subscriberEmail" to viewer.email, "subscriberAvatar" to viewer.profileImage,
            "promotionId" to promo.promotionId, "creatorId" to promo.creatorId,
            "targetId" to promo.targetId, "verifiedAt" to System.currentTimeMillis()
        )
        val updates = hashMapOf<String, Any>(
            "subscriptions/" + clean(promo.creatorId) + "/" + clean(id) to payload,
            "userSubscriptions/" + clean(viewer.userId) + "/" + clean(id) to payload
        )
        db.updateChildren(updates)
    }
}
