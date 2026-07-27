package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database singleton cho ứng dụng quản lý kho hàng.
 * Chứa 2 bảng: products (sản phẩm) và warehouse_notifications (thông báo).
 * Version 4 - có migration từ version 3.
 * Sử dụng Singleton pattern để đảm bảo chỉ có 1 instance database.
 */
@Database(entities = [Product::class, WarehouseNotification::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WarehouseDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: WarehouseDatabase? = null

        /** Migration từ version 3 sang 4: tạo bảng notification_dao */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notification_dao` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`message` TEXT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`type` TEXT NOT NULL DEFAULT 'info', " +
                    "`isRead` INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `notification_dao` " +
                    "SELECT * FROM `warehouse_notifications`"
                )
            }
        }

        /** Tạo hoặc trả về instance database singleton */
        fun getDatabase(context: Context): WarehouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarehouseDatabase::class.java,
                    "warehouse_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
