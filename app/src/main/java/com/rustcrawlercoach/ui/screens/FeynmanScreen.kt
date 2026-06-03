package com.rustcrawlercoach.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustcrawlercoach.ui.theme.ErrorRed
import com.rustcrawlercoach.ui.theme.SuccessGreen
import com.rustcrawlercoach.ui.theme.XpGold
import com.rustcrawlercoach.ui.viewmodel.FeynmanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeynmanScreen(
    chapterId: Int,
    onNavigateBack: () -> Unit,
    onChapterCompleted: () -> Unit,
    viewModel: FeynmanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                viewModel.setMicRecording(false)
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { text ->
                    viewModel.appendToAnswer(text)
                }
                viewModel.setMicRecording(false)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    DisposableEffect(Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "费曼测验 - ${uiState.chapter?.title ?: ""}",
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
                onClick = {
                    if (uiState.isMicRecording) {
                        speechRecognizer?.stopListening()
                        viewModel.setMicRecording(false)
                    } else {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        }
                        speechRecognizer?.startListening(intent)
                        viewModel.setMicRecording(true)
                    }
                },
                containerColor = if (uiState.isMicRecording) ErrorRed else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (uiState.isMicRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (uiState.isMicRecording) "停止录音" else "开始录音"
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 主题说明
                TopicCard(
                    topic = uiState.chapter?.knowledgePoints ?: ""
                )

                // 输入区域
                InputSection(
                    answer = uiState.currentAnswer,
                    onAnswerChange = { viewModel.updateAnswer(it) },
                    onSubmit = { viewModel.submitAnswer() },
                    isSubmitting = uiState.isSubmitting,
                    isMicRecording = uiState.isMicRecording
                )

                // 历史记录
                if (uiState.answers.isNotEmpty()) {
                    HistorySection(
                        answers = uiState.answers
                    )
                }
            }
        }

        // 结果对话框
        if (uiState.showResultDialog) {
            ResultDialog(
                score = uiState.aiScore,
                feedback = uiState.aiFeedback,
                chapterCompleted = uiState.chapterCompleted,
                onDismiss = { viewModel.dismissResultDialog() },
                onChapterCompleted = {
                    onChapterCompleted()
                }
            )
        }
    }
}

@Composable
private fun TopicCard(
    topic: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 请用你自己的话解释：",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = topic,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun InputSection(
    answer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    isMicRecording: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("用你自己的话解释") },
                placeholder = { Text("请输入你的解释...（支持语音输入）") },
                minLines = 5,
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = answer.isNotBlank() && !isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 评分中...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("提交并获取 AI 评分", fontWeight = FontWeight.Bold)
                }
            }

            if (isMicRecording) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(ErrorRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在录音...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(
    answers: List<com.rustcrawlercoach.data.entity.FeynmanAnswer>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📝 历史记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            answers.take(5).forEach { answer ->
                HistoryItem(
                    answer = answer.answerText,
                    score = answer.aiScore,
                    date = com.rustcrawlercoach.util.DateUtils.formatTimestamp(answer.createdAt)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryItem(
    answer: String,
    score: Int,
    date: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = answer.take(100) + if (answer.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 80 -> SuccessGreen
                    score >= 60 -> XpGold
                    else -> ErrorRed
                }
            )
            Text(
                text = "分",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ResultDialog(
    score: Int?,
    feedback: String?,
    chapterCompleted: Boolean,
    onDismiss: () -> Unit,
    onChapterCompleted: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎉 评分结果")
                if (score != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$score 分",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            score >= 80 -> SuccessGreen
                            score >= 60 -> XpGold
                            else -> ErrorRed
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                feedback?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            if (chapterCompleted) {
                TextButton(onClick = {
                    onDismiss()
                    onChapterCompleted()
                }) {
                    Text("继续下一章节")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("好的")
                }
            }
        }
    )
}
