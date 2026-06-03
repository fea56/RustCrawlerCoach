package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 题库实体 - 每个章节的测验题
 * 支持选择题、填空题、代码题
 */
@Entity(
    tableName = "question_bank",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId")]
)
data class QuestionBank(
    @PrimaryKey
    val id: Int,
    val chapterId: Int,
    val questionText: String,
    val questionType: String, // choice / fill / code
    val options: String? = null, // JSON 数组，仅选择题
    val correctAnswer: String,
    val xpReward: Int = 10
)
