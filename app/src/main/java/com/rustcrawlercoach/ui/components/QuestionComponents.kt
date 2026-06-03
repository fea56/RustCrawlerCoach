package com.rustcrawlercoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rustcrawlercoach.data.entity.QuestionBank
import com.rustcrawlercoach.ui.theme.ErrorRed
import com.rustcrawlercoach.ui.theme.SuccessGreen

@Composable
fun QuestionCard(
    question: QuestionBank,
    selectedAnswer: String?,
    isCorrect: Boolean?,
    showResult: Boolean,
    questionNumber: Int,
    totalQuestions: Int,
    onAnswerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 题号
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExamTypeChip(examType = question.questionType)
                Text(
                    text = "第 $questionNumber / $totalQuestions 题",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 题目
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 选项
            if (question.questionType == "choice" && question.options != null) {
                val options = parseOptions(question.options)
                options.forEach { option ->
                    OptionItem(
                        option = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = if (showResult) option == question.correctAnswer else null,
                        isUserChoice = selectedAnswer == option,
                        showResult = showResult,
                        onClick = { if (!showResult) onAnswerSelected(option) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (question.questionType == "fill" || question.questionType == "code") {
                // 填空/代码题
                OutlinedTextField(
                    value = selectedAnswer ?: "",
                    onValueChange = { if (!showResult) onAnswerSelected(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("请输入答案") },
                    enabled = !showResult,
                    minLines = if (question.questionType == "code") 5 else 2
                )

                if (showResult) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isCorrect == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "正确",
                                tint = SuccessGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "正确答案：${question.correctAnswer}",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "错误",
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "正确答案：${question.correctAnswer}",
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionItem(
    option: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isUserChoice: Boolean,
    showResult: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showResult && isCorrect == true -> SuccessGreen.copy(alpha = 0.2f)
        showResult && isUserChoice && isCorrect == false -> ErrorRed.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        showResult && isCorrect == true -> SuccessGreen
        showResult && isUserChoice && isCorrect == false -> ErrorRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !showResult, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
                .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else borderColor,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = option,
            style = MaterialTheme.typography.bodyMedium
        )

        if (showResult && isCorrect == true) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "正确",
                tint = SuccessGreen
            )
        } else if (showResult && isUserChoice && isCorrect == false) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "错误",
                tint = ErrorRed
            )
        }
    }
}

@Composable
fun ExamTypeChip(examType: String) {
    val (icon, label, color) = when (examType) {
        "choice" -> Triple(Icons.Default.Quiz, "选择题", Color(0xFF4CAF50))
        "fill" -> Triple(Icons.Default.Edit, "填空题", Color(0xFF2196F3))
        "code" -> Triple(Icons.Default.Code, "代码题", Color(0xFFFF9800))
        "feynman" -> Triple(Icons.Default.RecordVoiceOver, "费曼测验", Color(0xFF9C27B0))
        else -> Triple(Icons.Default.Quiz, "测验", Color(0xFF757575))
    }

    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            Text("处理中...")
        } else {
            Text(text = text, fontWeight = FontWeight.Bold)
        }
    }
}

private fun parseOptions(optionsJson: String): List<String> {
    return try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson(optionsJson, type)
    } catch (e: Exception) {
        emptyList()
    }
}
