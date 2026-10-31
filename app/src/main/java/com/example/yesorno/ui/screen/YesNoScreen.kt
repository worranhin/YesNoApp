package com.example.yesorno.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yesorno.util.hapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class YesNoResult {
    IDLE, YES, NO
}

@Composable
fun YesNoScreen(
    modifier: Modifier = Modifier
) {
    var result by remember { mutableStateOf(YesNoResult.IDLE) }
    val scale = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(1f) }
    
    var showRing by remember { mutableStateOf(false) }
    val ringScale = remember { Animatable(0.2f) }
    val ringAlpha = remember { Animatable(0f) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var animationJob by remember { mutableStateOf<Job?>(null) }

    // 马卡龙色系 - 柔和但更深一点
    val yesColor = Color(0xFF7FCDC0)
    val noColor = Color(0xFFFF9AAE)
    val idleColor = MaterialTheme.colorScheme.background

    val targetBackgroundColor = when (result) {
        YesNoResult.IDLE -> idleColor
        YesNoResult.YES -> yesColor
        YesNoResult.NO -> noColor
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(800),
        label = "backgroundColor"
    )

    val textColor = when (result) {
        YesNoResult.IDLE -> MaterialTheme.colorScheme.onBackground
        YesNoResult.YES, YesNoResult.NO -> Color.White
    }

    val ringColor = when (result) {
        YesNoResult.IDLE -> Color.Transparent
        YesNoResult.YES -> Color.White.copy(alpha = 0.6f)
        YesNoResult.NO -> Color.White.copy(alpha = 0.6f)
    }

    val displayText = when (result) {
        YesNoResult.IDLE -> "?"
        YesNoResult.YES -> "YES"
        YesNoResult.NO -> "NO"
    }

    fun triggerAnimation() {
        // 取消上一次动画
        animationJob?.cancel()
        
        animationJob = coroutineScope.launch {
            // 立即隐藏文字
            scale.snapTo(0.2f)
            textAlpha.snapTo(0f)
            
            // 初始化光环
            showRing = true
            ringScale.snapTo(0.2f)
            ringAlpha.snapTo(0.8f)

            // 并行执行所有动画
            launch {
                scale.animateTo(
                    targetValue = 1.12f,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = 400f
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 500f
                    )
                )
            }
            
            launch {
                textAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(350)
                )
            }
            
            launch {
                ringScale.animateTo(
                    targetValue = 3.5f,
                    animationSpec = tween(700)
                )
            }
            
            launch {
                ringAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300)
                )
            }
            
            // 等待光环动画完成
            kotlinx.coroutines.delay(700)
            showRing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    val newResult = if (Random.nextBoolean()) YesNoResult.YES else YesNoResult.NO
                    result = newResult
                    context.hapticFeedback()
                    triggerAnimation()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 光环效果
        if (showRing) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(ringScale.value)
                    .alpha(ringAlpha.value)
                    .border(6.dp, ringColor, CircleShape)
            )
        }

        // 文字
        Text(
            text = displayText,
            fontSize = when (result) {
                YesNoResult.IDLE -> 140.sp
                else -> 100.sp
            },
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier
                .scale(scale.value)
                .alpha(textAlpha.value)
        )
    }
}
