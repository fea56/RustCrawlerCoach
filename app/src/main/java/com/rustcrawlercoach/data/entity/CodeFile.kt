package com.rustcrawlercoach.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 代码文件实体 - 存储用户在各章节编写的代码文件
 */
@Entity(
    tableName = "code_files",
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
data class CodeFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val chapterId: Int,
    val fileName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
