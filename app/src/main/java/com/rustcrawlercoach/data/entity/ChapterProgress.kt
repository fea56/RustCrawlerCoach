package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节详细进度实体 - 记录每个章节的完成状态
 */
@Entity(
    tableName = "chapter_progress",
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
data class ChapterProgress(
    @PrimaryKey
    val chapterId: Int,
    val isQuizCompleted: Boolean = false,
    val quizScore: Int = 0, // 0-100 百分比
    val isProjectCompleted: Boolean = false,
    val isCompleted: Boolean = false, // 整体完成状态
    val completedAt: Long? = null
)
