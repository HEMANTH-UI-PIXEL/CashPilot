package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // Mutual Funds, Stocks, Fixed Deposit, Gold, Bonds, Other Investment
    @ColumnInfo(name = "amount_invested")
    val amountInvested: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
