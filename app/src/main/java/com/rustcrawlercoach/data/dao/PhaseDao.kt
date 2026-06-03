package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustcrawlercoach.data.entity.Phase
import kotlinx.coroutines.flow.Flow

@Dao
interface PhaseDao {
    @Query("SELECT * FROM phase ORDER BY orderIndex ASC")
    fun getAllPhases(): Flow<List<Phase>>

    @Query("SELECT * FROM phase WHERE id = :phaseId")
    suspend fun getPhaseById(phaseId: Int): Phase?

    @Query("SELECT * FROM phase WHERE orderIndex = :orderIndex")
    suspend fun getPhaseByOrder(orderIndex: Int): Phase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<Phase>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phase: Phase)

    @Query("SELECT COUNT(*) FROM phase")
    suspend fun getCount(): Int
}
