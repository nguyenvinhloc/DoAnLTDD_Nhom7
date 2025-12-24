package com.example.doannhom7.data.entity

import androidx.room.*

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
