package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,
    @ColumnInfo(name = "current_amount")
    val currentAmount: Double = 0.0,
    @ColumnInfo(name = "target_date")
    val targetDate: Long = 0L,
    val description: String = "",
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0.0) ((currentAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0).toFloat() else 0f
}
