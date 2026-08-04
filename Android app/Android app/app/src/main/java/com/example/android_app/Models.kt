package com.example.android_app

import android.health.connect.datatypes.units.Percentage
import com.google.firebase.Timestamp

data class BatchPrediction(
    val prediction: String = "",
    val tokensText: String = "",
    val aiPercentage: Float = 0.0f
)

data class FullPredictionRecord(
    val userInput: String = "",
    val batchPredictions: Map<String, BatchPrediction> = emptyMap(),
    val finalPrediction: String = "",
    val timestamp: Timestamp = Timestamp.now()
)


