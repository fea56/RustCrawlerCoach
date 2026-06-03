package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustcrawlercoach.data.entity.QuestionBank
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionBankDao {
    @Query("SELECT * FROM question_bank WHERE chapterId = :chapterId")
    fun getQuestionsByChapter(chapterId: Int): Flow<List<QuestionBank>>

    @Query("SELECT * FROM question_bank WHERE chapterId = :chapterId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByChapter(chapterId: Int, limit: Int = 5): List<QuestionBank>

    @Query("SELECT * FROM question_bank WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Int): QuestionBank?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionBank>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionBank)

    @Query("SELECT COUNT(*) FROM question_bank WHERE chapterId = :chapterId")
    suspend fun getQuestionCountByChapter(chapterId: Int): Int
}
