package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DiamondTransaction
import com.example.data.model.EarnTask
import com.example.data.model.PromotionItem
import com.example.data.model.ReportItem
import com.example.data.model.TaskCompletion
import com.example.data.model.UserAccount

@Database(
    entities = [
        UserAccount::class,
        PromotionItem::class,
        EarnTask::class,
        TaskCompletion::class,
        DiamondTransaction::class,
        AppNotification::class,
        ReportItem::class,
        AuditLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun promotionDao(): PromotionDao
    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reportDao(): ReportDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "creator_diamond_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
