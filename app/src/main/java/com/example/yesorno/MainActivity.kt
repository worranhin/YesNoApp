package com.example.yesorno

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yesorno.ui.theme.YesOrNoTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YesOrNoTheme {
                YesNoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoApp(modifier: Modifier = Modifier) {
    var result by remember { mutableStateOf("Yes or No?") }
    var count by remember { mutableIntStateOf(0) }

    val animatedColor by  animateColorAsState(
        targetValue = when (count) {
            0 -> MaterialTheme.colorScheme.background
            1 -> MaterialTheme.colorScheme.primaryContainer
            2 -> MaterialTheme.colorScheme.secondaryContainer
            3 -> MaterialTheme.colorScheme.tertiaryContainer
            4 -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.background
        },
        label = "color"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = when (count) {
            0 -> MaterialTheme.colorScheme.onBackground
            1 -> MaterialTheme.colorScheme.onPrimaryContainer
            2 -> MaterialTheme.colorScheme.onSecondaryContainer
            3 -> MaterialTheme.colorScheme.onTertiaryContainer
            4 -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onBackground
        },
        label = "text color"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    result = generateYesNo()

                    if (++count > 4)
                        count = 0
                }
                ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh random result",
                    modifier = Modifier.size(32.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Yes or No")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = animatedColor,
                    titleContentColor = animatedTextColor
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = animatedColor,
                contentColor = animatedTextColor
            ) { }
        }

    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind { drawRect(animatedColor) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = result
            ) { targetResult ->
                Text(
                    targetResult,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = animatedTextColor
                    )
                )
            }
            BasicText(
                text = "Click the button to know.",
                color = { animatedTextColor }
            )
        }
    }
}

fun generateYesNo(): String {
    val randomNumber = Random.nextInt(2)
    return if (randomNumber == 0) "No" else "Yes"
}

@Preview(showBackground = true)
@Composable
fun YesNoAppPreview() {
    YesOrNoTheme {
            YesNoApp()
    }
}


//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    YesOrNoTheme {
//        Greeting("Android")
//    }
//}