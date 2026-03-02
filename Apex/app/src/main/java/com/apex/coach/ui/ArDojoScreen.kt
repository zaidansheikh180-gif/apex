package com.apex.coach.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ArDojoScreen() {
    var repCount by remember { mutableIntStateOf(0) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview (Placeholder for MediaPipe integration)
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    // Logic to start camera and bind to MediaPipe will go here
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Rep Counter Overlay (PRD EXE-02)
        Text(
            text = "REPS: $repCount",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        )
        
        // Skeleton Overlay would be drawn here using Canvas (Sprint 2)
    }
}
