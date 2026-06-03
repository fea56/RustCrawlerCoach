package com.rustcrawlercoach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustcrawlercoach.ui.components.CurrentChapterCard
import com.rustcrawlercoach.ui.components.ProgressSection
import com.rustcrawlercoach.ui.components.StatsCard
import com.rustcrawlercoach.ui.viewmodel.MainViewModel

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToLearning: (Int) -> Unit,
    onNavigateToFeynman: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RustCrawlerCoach",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 欢迎语
                Text(
                    text = "欢迎回来，编程学习者！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // AI 学习建议卡片
                LearningAdviceCard(
                    advice = uiState.learningAdvice,
                    isLoading = uiState.adviceIsLoading,
                    canRefresh = uiState.canRefreshAdvice,
                    onRefresh = { viewModel.refreshLearningAdvice() }
                )

                // 统计数据卡片
                StatsCard(
                    streakDays = uiState.userProgress?.streakDays ?: 0,
                    totalXp = uiState.userProgress?.totalXp ?: 0,
                    streakFreeze = uiState.userProgress?.streakFreeze ?: 0
                )

                // 进度条
                ProgressSection(
                    completedChapters = uiState.completedChapters,
                    totalChapters = uiState.totalChapters
                )

                // 当前章节卡片
                uiState.currentChapter?.let { chapter ->
                    val currentChapterWithProgress = uiState.chaptersWithProgress.find { it.chapter.id == chapter.id }
                    val isUnlocked = currentChapterWithProgress?.isUnlocked ?: true
                    val isPhase2 = chapter.phaseId == 2
                    
                    if (isUnlocked) {
                        CurrentChapterCard(
                            phaseTitle = uiState.currentPhase?.title ?: "学习阶段",
                            chapterTitle = chapter.title,
                            onStartLearning = {
                                viewModel.checkIn()
                                if (isPhase2) {
                                    onNavigateToFeynman(chapter.id)
                                } else {
                                    onNavigateToLearning(chapter.id)
                                }
                            }
                        )
                    } else {
                        LockedChapterCard(
                            phaseTitle = uiState.currentPhase?.title ?: "学习阶段",
                            chapterTitle = chapter.title,
                            hint = uiState.lockedChapterHint ?: "完成前一章节才能解锁"
                        )
                    }
                }

                // 打卡按钮
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val hasCheckedIn = uiState.userProgress?.lastActiveDate == today

                if (!hasCheckedIn) {
                    ActionButton(
                        text = "今日打卡",
                        onClick = { viewModel.checkIn() },
                        modifier = Modifier.fillMaxSize(),
                        enabled = true
                    )
                } else {
                    Text(
                        text = "✅ 今日已打卡，明天继续加油！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LearningAdviceCard(
    advice: String,
    isLoading: Boolean,
    canRefresh: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖 AI 学习建议",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer
                )
                IconButton(
                    onClick = onRefresh,
                    enabled = canRefresh && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                alpha = if (canRefresh) 1f else 0.5f
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (advice.isNotEmpty()) {
                Text(
                    text = advice,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                Text(
                    text = "点击刷新获取个性化学习建议...",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LockedChapterCard(
    phaseTitle: String,
    chapterTitle: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = phaseTitle,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "🔒 $chapterTitle",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = hint,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
