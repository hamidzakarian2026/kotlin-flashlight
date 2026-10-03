package com.example.flashlight

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashlightApp()
        }
    }
}

@Composable
fun FlashlightApp() {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember { 
        try { cameraManager.cameraIdList[0] } catch (e: Exception) { null } 
    }

    var isOn by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(1.0f) }

    fun updateFlashlight(enabled: Boolean, level: Float) {
        if (cameraId == null) return
        try {
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val maxLevel = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    if (maxLevel > 1) {
                        val targetLevel = (level * maxLevel).toInt().coerceIn(1, maxLevel)
                        cameraManager.turnOnTorchWithStrengthLevel(cameraId, targetLevel)
                    } else {
                        cameraManager.setTorchMode(cameraId, true)
                    }
                } else {
                    cameraManager.setTorchMode(cameraId, true)
                }
            } else {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFFFFD700) else Color(0xFF2C2C2E),
        label = "color"
    )

    val glowScale by animateFloatAsState(
        targetValue = if (isOn) 1.2f else 1.0f,
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121214)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "FLASHLIGHT",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )

            // دکمه دایره‌ای با افکت هاله نور
            Box(contentAlignment = Alignment.Center) {
                if (isOn) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(glowScale)
                            .background(Color(0xFFFFD700).copy(alpha = 0.3f), CircleShape)
                            .blur(30.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isOn = !isOn
                        updateFlashlight(isOn, brightness)
                    },
                    modifier = Modifier
                        .size(140.dp)
                        .background(buttonColor, CircleShape)
                ) {
                    Text(
                        text = if (isOn) "ON" else "OFF",
                        color = if (isOn) Color.Black else Color.White,
                        fontSize = 28.sp
                    )
                }
            }

            // اسلایدر تنظیم نور
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "شدت نور: ${(brightness * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = brightness,
                    onValueChange = {
                        brightness = it
                        if (isOn) updateFlashlight(true, brightness)
                    },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD700),
                        activeTrackColor = Color(0xFFFFD700),
                        inactiveTrackColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
