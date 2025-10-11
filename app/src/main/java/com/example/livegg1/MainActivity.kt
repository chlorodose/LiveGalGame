package com.example.livegg1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collect
import com.example.livegg1.dialog.KeywordDialog
import com.example.livegg1.speech.KeywordSpeechListener
import com.example.livegg1.ui.CameraScreen
import com.example.livegg1.ui.theme.LiveGG1Theme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var permissionsGranted by mutableStateOf(mapOf<String, Boolean>())

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionsGranted = permissions
            permissions.entries.forEach {
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏沉浸式模式
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            // 隐藏导航栏
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // 隐藏状态栏
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )

        cameraExecutor = Executors.newSingleThreadExecutor()

        val permissionsToRequest = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGrantedPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(notGrantedPermissions.toTypedArray())
        } else {
            permissionsGranted = permissionsToRequest.associateWith { true }
        }

        setContent {
            LiveGG1Theme {
                val allPermissionsGranted = permissionsGranted.all { it.value }
                if (allPermissionsGranted) {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val speechListener = remember { KeywordSpeechListener(keyword = "吗") }
                    var showKeywordDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(speechListener) {
                        speechListener.keywordTriggers.collect {
                            speechListener.stopListening()
                            showKeywordDialog = true
                        }
                    }

                    DisposableEffect(lifecycleOwner, speechListener) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> speechListener.startListening()
                                Lifecycle.Event.ON_STOP -> speechListener.stopListening()
                                else -> Unit
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            speechListener.release()
                        }
                    }

                    CameraScreen(
                        cameraExecutor = cameraExecutor,
                        onRecognizedText = { text, isFinal ->
                            speechListener.onRecognizedText(text, isFinal)
                        },
                        isDialogVisible = showKeywordDialog
                    )

                    if (showKeywordDialog) {
                        KeywordDialog(
                            onAccept = {
                                Log.d("MainActivity", "Keyword accepted")
                                showKeywordDialog = false
                                speechListener.startListening()
                            },
                            onReject = {
                                Log.d("MainActivity", "Keyword rejected")
                                showKeywordDialog = false
                                speechListener.startListening()
                            },
                            onDismiss = {
                                showKeywordDialog = false
                                speechListener.startListening()
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera and/or Audio permission denied. Please grant permissions to use the app.")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
