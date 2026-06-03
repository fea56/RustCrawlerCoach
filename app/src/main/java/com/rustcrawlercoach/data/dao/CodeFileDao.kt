package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rustcrawlercoach.data.entity.CodeFile
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeFileDao {
    @Query("SELECT * FROM code_files WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getCodeFilesByChapter(chapterId: Int): Flow<List<CodeFile>>

    @Query("SELECT * FROM code_files WHERE id = :fileId")
    suspend fun getCodeFileById(fileId: Int): CodeFile?

    @Query("SELECT * FROM code_files WHERE chapterId = :chapterId AND fileName = :fileName")
    suspend fun getCodeFileByName(chapterId: Int, fileName: String): CodeFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(codeFile: CodeFile): Long

    @Update
    suspend fun update(codeFile: CodeFile)

    @Delete
    suspend fun delete(codeFile: CodeFile)

    @Query("DELETE FROM code_files WHERE id = :fileId")
    suspend fun deleteById(fileId: Int)

    @Query("SELECT COUNT(*) FROM code_files WHERE chapterId = :chapterId")
    suspend fun getFileCountByChapter(chapterId: Int): Int
}
