package com.example.yesorno.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.yesorno.R
import com.example.yesorno.ui.theme.YesOrNoTheme
import com.example.yesorno.util.hapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

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

    // 退出时取消尚未结束的动画，避免协程泄漏
    DisposableEffect(Unit) {
        onDispose { animationJob?.cancel() }
    }

    val triggerAnimation = remember(coroutineScope) {
        {
            // 取消上一次动画
            animationJob?.cancel()

            animationJob = coroutineScope.launch {
                try {
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
                    kotlinx.coroutines.delay(700.milliseconds)
                } finally {
                    showRing = false
                }
            }
        }
    }

    val onTap = remember(context, triggerAnimation) {
        {
            result = if (Random.nextBoolean()) YesNoResult.YES else YesNoResult.NO
            context.hapticFeedback()
            triggerAnimation()
        }
    }

    YesNoContent(
        modifier = modifier,
        result = result,
        scale = scale.value,
        textAlpha = textAlpha.value,
        showRing = showRing,
        ringScale = ringScale.value,
        ringAlpha = ringAlpha.value,
        onTap = onTap
    )
}

/**
 * 无状态展示层：三种状态共用同一套配色、排版与光环语言。
 * 状态与动画由 [YesNoScreen] 提升管理，方便预览与复用。
 */
@Composable
private fun YesNoContent(
    result: YesNoResult,
    scale: Float,
    textAlpha: Float,
    showRing: Boolean,
    ringScale: Float,
    ringAlpha: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val targetBackgroundColor = when (result) {
        YesNoResult.IDLE -> colorScheme.tertiary
        YesNoResult.YES -> colorScheme.primary
        YesNoResult.NO -> colorScheme.secondary
    }
    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(800),
        label = "backgroundColor"
    )

    val contentColor = when (result) {
        YesNoResult.IDLE -> colorScheme.onTertiary
        YesNoResult.YES -> colorScheme.onPrimary
        YesNoResult.NO -> colorScheme.onSecondary
    }

    val displayText = when (result) {
        YesNoResult.IDLE -> "?"
        YesNoResult.YES -> "YES"
        YesNoResult.NO -> "NO"
    }

    val tapHint = stringResource(R.string.tap_hint)
    val hintAlpha by animateFloatAsState(
        targetValue = if (result == YesNoResult.IDLE) 0.75f else 0f,
        animationSpec = tween(300),
        label = "hintAlpha"
    )

    // 初始态的呼吸光环，与结果态的扩散光环共用 CircleShape 描边语言
    val breatheTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by breatheTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    val breatheAlpha by breatheTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = tapHint
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 光环与主文字共用的居中容器：宽度撑满避免长文字换行被裁，高度固定保证圆形区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // 初始态：柔和呼吸光环，暗示可点击
                if (result == YesNoResult.IDLE) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(breatheScale)
                            .alpha(breatheAlpha)
                            .border(4.dp, Color.White, CircleShape)
                    )
                }

                // 结果态：扩散光环（动效保持不变）
                if (showRing) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(ringScale)
                            .alpha(ringAlpha)
                            .border(6.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.displayLarge,
                    color = contentColor,
                    modifier = Modifier
                        .scale(scale)
                        .alpha(textAlpha)
                )
            }

            // 提示文字放在圆形外面（下方一点的位置）
            Text(
                text = tapHint,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = hintAlpha),
                modifier = Modifier
                    .alpha(hintAlpha)
                    .padding(top = 24.dp)
            )
        }
    }
}

@Preview(name = "IDLE")
@Composable
private fun IdlePreview() {
    YesOrNoTheme {
        YesNoContent(
            result = YesNoResult.IDLE,
            scale = 1f,
            textAlpha = 1f,
            showRing = false,
            ringScale = 1f,
            ringAlpha = 0f,
            onTap = {}
        )
    }
}

@Preview(name = "YES")
@Composable
private fun YesPreview() {
    YesOrNoTheme {
        YesNoContent(
            result = YesNoResult.YES,
            scale = 1f,
            textAlpha = 1f,
            showRing = false,
            ringScale = 1f,
            ringAlpha = 0f,
            onTap = {}
        )
    }
}

@Preview(name = "NO")
@Composable
private fun NoPreview() {
    YesOrNoTheme {
        YesNoContent(
            result = YesNoResult.NO,
            scale = 1f,
            textAlpha = 1f,
            showRing = false,
            ringScale = 1f,
            ringAlpha = 0f,
            onTap = {}
        )
    }
}

@Preview(name = "IDLE (Dark)")
@Composable
private fun IdleDarkPreview() {
    YesOrNoTheme(darkTheme = true) {
        YesNoContent(
            result = YesNoResult.IDLE,
            scale = 1f,
            textAlpha = 1f,
            showRing = false,
            ringScale = 1f,
            ringAlpha = 0f,
            onTap = {}
        )
    }
}
