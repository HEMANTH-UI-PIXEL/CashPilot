package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "type"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "income", "expense", "saving", "investment"
    @ColumnInfo(name = "icon_name")
    val iconName: String = "category",
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false
)
