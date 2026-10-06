package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("date"),
        Index("type"),
        Index("category")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "income", "expense", "saving", "investment"
    val amount: Double,
    val category: String,
    val title: String,
    val description: String = "",
    val date: Long, // timestamp in millis
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "Cash",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
