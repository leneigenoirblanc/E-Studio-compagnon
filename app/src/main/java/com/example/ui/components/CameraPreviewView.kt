package com.example.ui.components

import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraScannerView(
    modifier: Modifier = Modifier,
    isAutoScan: Boolean = false,
    autoScanDelayMs: Long = 800L,
    manualScanTrigger: Long = 0L,
    isTorchOn: Boolean = false,
    onBarcodeDetected: (String, Int) -> Unit,
    onToggleTorch: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var analyzerRef by remember { mutableStateOf<BarcodeAnalyzer?>(null) }

    LaunchedEffect(isTorchOn, camera) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }

    LaunchedEffect(isAutoScan, autoScanDelayMs) {
        analyzerRef?.isAutoScanEnabled = isAutoScan
        analyzerRef?.autoScanDelayMs = autoScanDelayMs
    }

    LaunchedEffect(manualScanTrigger) {
        if (manualScanTrigger > 0L) {
            analyzerRef?.triggerManualScan()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .testTag("camera_scanner_container")
    ) {
        // Vue native CameraX
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    runCatching {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val analyzer = BarcodeAnalyzer(
                            isAutoScanEnabled = isAutoScan,
                            autoScanDelayMs = autoScanDelayMs
                        ) { code, format ->
                            onBarcodeDetected(code, format)
                        }
                        analyzerRef = analyzer

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor, analyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Overlay de visée avec coins et laser animé
        ScannerLaserOverlay(
            modifier = Modifier.fillMaxSize(),
            isActive = isAutoScan
        )

        // Bouton Flash / Torche
        IconButton(
            onClick = onToggleTorch,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(40.dp)
                .testTag("torch_toggle_button"),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isTorchOn) Color(0xFFFBBF24) else Color(0x66000000),
                contentColor = if (isTorchOn) Color.Black else Color.White
            )
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = if (isTorchOn) "Éteindre la torche" else "Allumer la torche"
            )
        }

        // Badge Statut Mode
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isAutoScan) "AUTO SCAN ACTIF (${autoScanDelayMs}ms)" else "MODE MANUEL • APPUYER SUR SCAN",
                color = if (isAutoScan) Color(0xFF34D399) else Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScannerLaserOverlay(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_position"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Cadre central de visée
        val boxWidth = width * 0.82f
        val boxHeight = height * 0.72f
        val left = (width - boxWidth) / 2f
        val top = (height - boxHeight) / 2f
        val right = left + boxWidth
        val bottom = top + boxHeight

        // Ligne laser animée
        if (isActive) {
            val laserY = top + (boxHeight * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF22D3EE),
                        Color(0xFF10B981),
                        Color(0xFF22D3EE),
                        Color.Transparent
                    )
                ),
                start = Offset(left + 14f, laserY),
                end = Offset(right - 14f, laserY),
                strokeWidth = 4f
            )
        }

        // Coins du réticule de visée
        val cornerLength = 32f
        val cornerStroke = 6f
        val cornerColor = if (isActive) Color(0xFF10B981) else Color(0xFF38BDF8)

        // Haut-Gauche
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), cornerStroke)

        // Haut-Droite
        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), cornerStroke)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), cornerStroke)

        // Bas-Gauche
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), cornerStroke)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLength), cornerStroke)

        // Bas-Droite
        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), cornerStroke)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), cornerStroke)
    }
}
