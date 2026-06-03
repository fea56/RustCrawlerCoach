package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节实体 - 表示一个阶段中的子章节/关卡
 * 例如：阶段一中的 A 变量、类型、输入输出、条件判断
 */
@Entity(
    tableName = "chapter",
    foreignKeys = [
        ForeignKey(
            entity = Phase::class,
            parentColumns = ["id"],
            childColumns = ["phaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("phaseId")]
)
data class Chapter(
    @PrimaryKey
    val id: Int,
    val phaseId: Int,
    val title: String,
    val orderIndex: Int,
    val knowledgePoints: String,
    val requiredProject: Boolean = true,
    val examType: String = "choice", // choice / fill / code / feynman
    val projectRequirement: String? = null // 项目需求文本
)
