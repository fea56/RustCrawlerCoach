package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 费曼测验答案实体 - 存储用户费曼测验的答案和 AI 评分反馈
 */
@Entity(
    tableName = "feynman_answers",
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
data class FeynmanAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val chapterId: Int,
    val answerText: String,
    val aiScore: Int = 0, // 0-100
    val aiFeedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
