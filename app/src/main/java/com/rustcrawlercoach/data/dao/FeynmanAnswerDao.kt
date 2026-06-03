package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rustcrawlercoach.data.entity.FeynmanAnswer
import kotlinx.coroutines.flow.Flow

@Dao
interface FeynmanAnswerDao {
    @Query("SELECT * FROM feynman_answers WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getAnswersByChapter(chapterId: Int): Flow<List<FeynmanAnswer>>

    @Query("SELECT * FROM feynman_answers WHERE id = :answerId")
    suspend fun getAnswerById(answerId: Int): FeynmanAnswer?

    @Query("SELECT * FROM feynman_answers WHERE chapterId = :chapterId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAnswerByChapter(chapterId: Int): FeynmanAnswer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(answer: FeynmanAnswer): Long

    @Update
    suspend fun update(answer: FeynmanAnswer)

    @Query("SELECT COUNT(*) FROM feynman_answers WHERE chapterId = :chapterId")
    suspend fun getAnswerCountByChapter(chapterId: Int): Int
}
