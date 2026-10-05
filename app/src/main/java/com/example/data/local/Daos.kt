package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DiamondTransaction
import com.example.data.model.EarnTask
import com.example.data.model.PromotionItem
import com.example.data.model.PromotionStatus
import com.example.data.model.ReportItem
import com.example.data.model.TaskCompletion
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): Flow<UserAccount?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserSync(userId: String): UserAccount?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE users SET diamonds = :newBalance, updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateDiamonds(userId: String, newBalance: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserAccount>>
}

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions ORDER BY createdAt DESC")
    fun getAllPromotions(): Flow<List<PromotionItem>>

    @Query("SELECT * FROM promotions WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getPromotionsByCreator(creatorId: String): Flow<List<PromotionItem>>

    @Query("SELECT * FROM promotions WHERE promotionId = :promotionId LIMIT 1")
    fun getPromotionById(promotionId: String): Flow<PromotionItem?>

    @Query("SELECT * FROM promotions WHERE promotionId = :promotionId LIMIT 1")
    suspend fun getPromotionByIdSync(promotionId: String): PromotionItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionItem)

    @Query("UPDATE promotions SET status = :status WHERE promotionId = :promotionId")
    suspend fun updatePromotionStatus(promotionId: String, status: PromotionStatus)

    @Query("UPDATE promotions SET topListedUntil = :until WHERE promotionId = :promotionId")
    suspend fun updateTopListedUntil(promotionId: String, until: Long)

    @Query("UPDATE promotions SET impressions = impressions + 1 WHERE promotionId = :promotionId")
    suspend fun incrementImpression(promotionId: String)

    @Query("UPDATE promotions SET clicks = clicks + 1 WHERE promotionId = :promotionId")
    suspend fun incrementClick(promotionId: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<EarnTask>>

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): EarnTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<EarnTask>)

    @Update
    suspend fun updateTask(task: EarnTask)

    @Query("SELECT * FROM task_completions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getTaskCompletions(userId: String): Flow<List<TaskCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(completion: TaskCompletion)

    @Query("SELECT COUNT(*) FROM task_completions WHERE userId = :userId AND taskId = :taskId")
    suspend fun getCompletionCount(userId: String, taskId: String): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM diamond_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactions(userId: String): Flow<List<DiamondTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DiamondTransaction)

    @Query("SELECT * FROM diamond_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<DiamondTransaction>>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportItem)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportItem>>
}

@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<AuditLog>>
}
