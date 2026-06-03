package com.rustcrawlercoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustcrawlercoach.ui.components.ActionButton
import com.rustcrawlercoach.ui.components.QuestionCard
import com.rustcrawlercoach.ui.components.XpRewardBadge
import com.rustcrawlercoach.ui.theme.SuccessGreen
import com.rustcrawlercoach.ui.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    chapterId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (Int) -> Unit,
    onNavigateToFeynman: (Int) -> Unit,
    viewModel: LearningViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAiDialog by remember { mutableStateOf(false) }
    var aiQuestion by remember { mutableStateOf("") }

    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.chapter?.title ?: "学习界面",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAiDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "问 AI"
                )
            }
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
        } else if (uiState.isQuizComplete) {
            QuizCompleteContent(
                correctCount = uiState.correctCount,
                totalQuestions = uiState.questions.size,
                requiresProject = uiState.chapter?.requiredProject == true,
                projectRequirement = uiState.projectRequirement,
                onGenerateProject = { viewModel.generateProject() },
                onContinue = { viewModel.completeChapterAndNext() },
                onOpenEditor = {
                    viewModel.dismissProjectDialog()
                    onNavigateToEditor(chapterId)
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 知识点卡片
                KnowledgeCard(
                    knowledgePoints = uiState.chapter?.knowledgePoints ?: ""
                )

                // 测验区
                if (uiState.questions.isNotEmpty()) {
                    val currentIndex = uiState.currentQuestionIndex
                    val questionState = uiState.questionStates.getOrNull(currentIndex)
                    val question = uiState.questions.getOrNull(currentIndex)

                    if (question != null && questionState != null) {
                        QuestionCard(
                            question = question,
                            selectedAnswer = questionState.selectedAnswer,
                            isCorrect = questionState.isCorrect,
                            showResult = questionState.showResult,
                            questionNumber = currentIndex + 1,
                            totalQuestions = uiState.questions.size,
                            onAnswerSelected = { viewModel.selectAnswer(it) }
                        )

                        if (questionState.showResult) {
                            ActionButton(
                                text = if (currentIndex < uiState.questions.size - 1) "下一题" else "完成测验",
                                onClick = { viewModel.nextQuestion() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // AI 生成新题目按钮
                        if (!uiState.isQuizComplete) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.generateNewQuestion() },
                                enabled = !uiState.isGeneratingQuestion,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isGeneratingQuestion) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI 生成中...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("生成新题目（AI）")
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI 问答对话框
        if (showAiDialog) {
            AiChatDialog(
                question = aiQuestion,
                onQuestionChange = { aiQuestion = it },
                onDismiss = { showAiDialog = false },
                onSend = {
                    viewModel.askAi(aiQuestion)
                },
                isLoading = uiState.isAiLoading
            )
        }

        // AI 回复对话框
        if (uiState.showAiDialog) {
            AiResponseDialog(
                response = uiState.aiResponse ?: "",
                onDismiss = { viewModel.dismissAiDialog() }
            )
        }

        // 项目需求对话框
        if (uiState.showProjectDialog) {
            ProjectDialog(
                projectDescription = uiState.projectDescription ?: "",
                onDismiss = { viewModel.dismissProjectDialog() },
                onOpenEditor = {
                    viewModel.dismissProjectDialog()
                    onNavigateToEditor(chapterId)
                }
            )
        }

        // 生成题目错误对话框
        if (uiState.showGenerateError && uiState.generateErrorMessage != null) {
            GenerateErrorDialog(
                errorMessage = uiState.generateErrorMessage!!,
                onRetry = { viewModel.retryGenerateQuestion() },
                onDismiss = { viewModel.dismissGenerateError() }
            )
        }
    }
}

@Composable
private fun KnowledgeCard(
    knowledgePoints: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "知识点",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = knowledgePoints,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun QuizCompleteContent(
    correctCount: Int,
    totalQuestions: Int,
    requiresProject: Boolean,
    projectRequirement: String?,
    onGenerateProject: () -> Unit,
    onContinue: () -> Unit,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "完成",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "测验完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "正确率：$correctCount / $totalQuestions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        XpRewardBadge(xp = correctCount * 10)

        Spacer(modifier = Modifier.height(32.dp))

        if (requiresProject) {
            if (projectRequirement != null) {
                // 已生成项目需求，显示需求卡片
                ProjectRequirementCard(
                    projectRequirement = projectRequirement,
                    onOpenEditor = onOpenEditor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Text("继续下一章", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = null
                    )
                }
            } else {
                // 尚未生成项目需求
                Button(
                    onClick = onGenerateProject,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成项目需求", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 可以先跳过，但后续无法解锁下一章
                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("跳过项目（后续需完成项目才能解锁下一章）", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("继续下一章", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ProjectRequirementCard(
    projectRequirement: String,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "项目需求",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = projectRequirement,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 10,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onOpenEditor,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("打开编辑器实现", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AiChatDialog(
    question: String,
    onQuestionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("问 DeepSeek") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入你的问题") },
                    minLines = 3,
                    enabled = !isLoading
                )
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 思考中...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSend,
                enabled = question.isNotBlank() && !isLoading
            ) {
                Text("发送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AiResponseDialog(
    response: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DeepSeek 回复") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = response,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun ProjectDialog(
    projectDescription: String,
    onDismiss: () -> Unit,
    onOpenEditor: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("项目需求") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = projectDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenEditor) {
                Text("打开编辑器")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun GenerateErrorDialog(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("生成题目失败") },
        text = { Text(errorMessage) },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("重试")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
