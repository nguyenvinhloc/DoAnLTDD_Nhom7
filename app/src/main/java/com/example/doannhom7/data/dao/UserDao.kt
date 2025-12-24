package com.example.doannhom7.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doannhom7.data.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(u: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :u AND password = :p LIMIT 1")
    suspend fun login(u: String, p: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
