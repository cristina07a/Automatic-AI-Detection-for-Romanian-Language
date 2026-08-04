package com.example.android_app

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

fun saveFullPredictionResult(userId: String, record: FullPredictionRecord) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .collection("predictions")
        .add(record)
        .addOnSuccessListener {
            println("Full prediction saved successfully for user $userId")
        }
        .addOnFailureListener { e ->
            println("Error saving full prediction: ${e.message}")
        }
}

fun loadPredictionHistory(userId: String, onLoaded: (List<FullPredictionRecord>, String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .collection("predictions")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { documents ->
            val predictionRecords = documents.map { document ->
                document.toObject(FullPredictionRecord::class.java)
            }
            onLoaded(predictionRecords, null)
        }
        .addOnFailureListener { e ->
            Log.e("PredictionHistory", "Error loading prediction history: ${e.message}")
            onLoaded(emptyList(), e.message)
        }
}


