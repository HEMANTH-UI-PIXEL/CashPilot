package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY date DESC, id DESC")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: InvestmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(investments: List<InvestmentEntity>)

    @Update
    suspend fun update(investment: InvestmentEntity)

    @Delete
    suspend fun delete(investment: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM investments")
    suspend fun getAllInvestmentsSnapshot(): List<InvestmentEntity>

    @Query("DELETE FROM investments")
    suspend fun deleteAll()
}
