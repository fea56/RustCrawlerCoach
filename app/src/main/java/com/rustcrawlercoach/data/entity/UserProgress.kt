package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户进度实体 - 存储用户的学习进度和打卡信息
 */
@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val userId: Int = 1,
    val currentPhaseId: Int = 1,
    val currentChapterId: Int = 1,
    val completedChapterIds: String = "[]", // JSON 数组
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val lastActiveDate: String = "",
    val streakFreeze: Int = 0 // 补签卡数量
)
