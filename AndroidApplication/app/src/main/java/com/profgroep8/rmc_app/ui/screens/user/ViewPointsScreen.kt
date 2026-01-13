package com.profgroep8.rmc_app.ui.screens.user

import RmcFilledButton
import RmcScreen
import android.graphics.Paint
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.RmcTextField
import com.profgroep8.rmc_app.viewmodel.user.BonusPointsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BonusPointsScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: BonusPointsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            navigateToScreen(RmcScreen.Login.name)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            RmcAppBar(
                title = stringResource(R.string.home_view_points),
                subtitle = stringResource(R.string.home_view_points),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bonus Points",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    RmcSpacer(8)
                    Text(
                        text = uiState.bonusPoints.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (uiState.simulationStatus.isNotBlank()) {
                        RmcSpacer(8)
                        Text(text = uiState.simulationStatus, textAlign = TextAlign.Center)
                    }

                    uiState.errorMessage?.let { err ->
                        RmcSpacer(8)
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    RmcSpacer(20)

                    RmcTextField(
                        label = "Start address",
                        value = uiState.startAddress,
                        onValueChange = { viewModel.onStartAddressChanged(it) }
                    )
                    RmcSpacer(8)

                    RmcTextField(
                        label = "Destination address",
                        value = uiState.endAddress,
                        onValueChange = { viewModel.onEndAddressChanged(it) }
                    )

                    RmcSpacer(16)

                    RmcFilledButton(
                        value = if (!uiState.isSimulationRunning) "Start simulation" else "Stop simulation",
                        onClick = {
                            if (!uiState.isSimulationRunning) viewModel.startSimulation()
                            else viewModel.stopSimulation()
                        },
                        isEnabled = !uiState.isLoading
                    )

                    RmcSpacer(24)

                    if (uiState.isSimulationRunning) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "🚗 Live Driving Stats",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                RmcSpacer(12)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(text = uiState.speedText, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = uiState.rpmText, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = uiState.gearText, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = uiState.scoreText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Text(text = uiState.simBonusText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                RmcSpacer(8)
                                Text(text = uiState.modeText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }

                        RmcSpacer(20)

                        ProfessionalPerformanceGraph(
                            dataPoints = uiState.performanceGraph,
                            modifier = Modifier.fillMaxWidth()
                        )

                        RmcSpacer(24)
                    }

                    RmcSpacer(24)
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalPerformanceGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Performance Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Last 100 seconds of driving",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                val currentScore = dataPoints.lastOrNull() ?: 50f
                val scoreColor = when {
                    currentScore >= 90f -> Color(0xFF4CAF50)
                    currentScore >= 70f -> Color(0xFF2196F3)
                    currentScore >= 50f -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(scoreColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${currentScore.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            RmcSpacer(16)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A3E))
                    .padding(16.dp)
            ) {
                AnimatedPerformanceGraph(dataPoints = dataPoints)
            }

            RmcSpacer(12)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "100s ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = "Now",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            RmcSpacer(16)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Improving", icon = "↗")
                LegendItem(color = Color(0xFF9E9E9E), label = "Stable", icon = "→")
                LegendItem(color = Color(0xFFF44336), label = "Declining", icon = "↘")
            }
        }
    }
}

@Composable
fun AnimatedPerformanceGraph(dataPoints: List<Float>) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (dataPoints.isEmpty()) return@Canvas

        for (i in 0..4) {
            val y = height * (i / 4f)
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            val label = (100 - (i * 25)).toString()
            drawContext.canvas.nativeCanvas.drawText(
                label,
                -25f,
                y + 5f,
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    alpha = 76
                    textSize = 28f
                }
            )
        }

        for (i in 0..9) {
            val x = width * (i / 9f)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
        }

        if (dataPoints.size >= 2) {
            val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

            val path = Path().apply {
                dataPoints.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = height - (value.coerceIn(0f, 100f) / 100f * height)

                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }

            val avgPerformance = dataPoints.average().toFloat()
            val trend = if (dataPoints.size >= 3) {
                val recent = dataPoints.takeLast(3).average()
                val earlier = dataPoints.take(3).average()
                recent - earlier
            } else 0.0

            val lineColor = when {
                trend > 5 -> Color(0xFF4CAF50)
                trend < -5 -> Color(0xFFF44336)
                else -> Color(0xFF9E9E9E)
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )

            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.3f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            dataPoints.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - (value.coerceIn(0f, 100f) / 100f * height)

                val pointColor = when {
                    value >= 90f -> Color(0xFF4CAF50)
                    value >= 70f -> Color(0xFF2196F3)
                    value >= 50f -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }

                drawCircle(
                    color = pointColor.copy(alpha = pulseAlpha),
                    radius = 12f,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = pointColor,
                    radius = 6f,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }

        val goodThresholdY = height - (70f / 100f * height)
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = 0.3f),
            start = Offset(0f, goodThresholdY),
            end = Offset(width, goodThresholdY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
            color = color
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}