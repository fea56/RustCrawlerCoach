package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 阶段实体 - 表示课程的一个大阶段
 * 例如：阶段一 Python 爬虫实战、阶段二 迷你C底层追问 等
 */
@Entity(tableName = "phase")
data class Phase(
    @PrimaryKey
    val id: Int,
    val title: String,
    val orderIndex: Int,
    val estimatedWeeks: Int,
    val coreGoal: String
)
