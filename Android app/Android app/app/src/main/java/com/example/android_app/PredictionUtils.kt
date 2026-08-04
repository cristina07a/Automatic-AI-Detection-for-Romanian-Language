package com.example.android_app

import android.content.Context
import android.util.Log
import com.example.android_app.tokenizer.FullTokenizer
import com.example.android_app.tokenizer.UnknownToken
import com.google.firebase.Timestamp
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

data class PredictionRecord(
    val userInput: String = "",
    val prediction: String = "",
    val timestamp: Timestamp? = null
)

data class TokenizeResult(
    val batchInputs: List<Map<String, Array<IntArray>>>,
    val batchTexts: List<List<String>>
)

data class TokenizeInferenceResult(
    val predictions: List<String>,
    val textsPerBatch: List<List<String>>,
    val finalPrediction: String = "",
    val aiPercentages: List<Float> = emptyList()
)

private fun softmax(logits: FloatArray): List<Double> {
    val expValues = logits.map { Math.exp(it.toDouble()) }
    val sumExp = expValues.sum()
    return expValues.map { it / sumExp * 100 }
}

fun runModelInferenceBatched(context: Context, userInput: String, selectedLanguage: String): TokenizeInferenceResult {
    val predictions = mutableListOf<String>()
    val aiPercentages = mutableListOf<Float>()
    var predictedClassAI = 0f
    var predictedClassNOTAI = 0f

    val textsPerBatch = mutableListOf<List<String>>()
    var finalPredictions = ""

    //-------------------ALEGEREA MODELULUI FOLOSIT---------------------
    val modelFileName = when(selectedLanguage) {
        "ro" -> "bert_model-ro.tflite"
        else    -> "bert_model-en.tflite"
    }

    try {
        val tfliteModel = loadModelFile(context, modelFileName)
        val interpreter = Interpreter(tfliteModel)

        val tokenizeResult = tokenizeInputBatches(context, userInput, selectedLanguage)
        val batches = tokenizeResult.batchInputs  // lista cu batchInputs
        textsPerBatch.addAll(tokenizeResult.batchTexts)  // lista cu textele pentru fiecare batch

        var batchNumber = 0

        for ((index, batch) in batches.withIndex()) {
            batchNumber++

            val inputs = arrayOf(
                batch["attention_mask"]!!,
                batch["input_ids"]!!,
                batch["token_type_ids"]!!
            )

            val output = Array(1) { FloatArray(2) }

            interpreter.runForMultipleInputsOutputs(inputs, mapOf(0 to output))

            val percentages = softmax(output[0])
            val predictedClass = if (percentages[0] > percentages[1]) "NOT AI" else "AI"
            val aiPercentage = percentages[1].toFloat()

            Log.d("InferenceBatch", "Batch $index - Class: $predictedClass | Prob: ${percentages.joinToString(", ") { "%.2f".format(it) }}")

            predictedClassAI += percentages[1].toFloat()
            predictedClassNOTAI += percentages[0].toFloat()

            predictions.add(predictedClass)
            aiPercentages.add(aiPercentage)
        }

        finalPredictions = if (predictedClassNOTAI > predictedClassAI) "NOT AI" else "AI"

        Log.d("Final Prediction", "Result: $finalPredictions")

        interpreter.close()

    } catch (e: Exception) {
        Log.e("ModelInference", "Error during batched inference: ${e.message}")
        return TokenizeInferenceResult(
            predictions = emptyList(),
            textsPerBatch = emptyList(),
            finalPrediction = "Eroare: ${e.message}",
            aiPercentages = emptyList()
        )
    }

    return TokenizeInferenceResult(
        predictions = predictions,
        textsPerBatch = textsPerBatch,
        finalPrediction = finalPredictions,
        aiPercentages = aiPercentages
    )
}

fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
    try {
        Log.d("LoadModel", "Loading model from $modelPath")
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        Log.d("LoadModel", "Model file loaded from assets")
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    } catch (e: Exception) {
        Log.e("LoadModel", "Error loading model file: ${e.message}")
        throw e
    }
}

fun loadVocab(context: Context, selectedLanguage: String): Map<String, Int> {
    //--------------VOCABULARUL FOLOSIT--------------//
    val vocabPath = when (selectedLanguage) {
        "ro" -> "bert-romanian-base-cased-v1/vocab.txt"
        else     -> "bert-base-uncased/vocab.txt"
    }
    try {
        Log.d("LoadVocab", "Loading vocabulary")
        val vocab = mutableMapOf<String, Int>()
        context.assets.open(vocabPath).bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                vocab[line.trim()] = index
            }
        }
        Log.d("LoadVocab", "Vocabulary loaded successfully")
        return vocab
    } catch (e: Exception) {
        Log.e("LoadVocab", "Error loading vocabulary: ${e.message}")
        throw e
    }
}

fun tokenizeInputBatches(context: Context, input: String, selectedLanguage: String): TokenizeResult {

    val MAX_LEN = 256
    val vocab = loadVocab(context, selectedLanguage)
    val tokenizer = FullTokenizer(vocab, false)

    val normalizedInput = input.replace("ţ", "ț").replace("ş", "ș").replace("Ţ", "Ț").replace("Ş", "Ș")

    val result = tokenizer.NewTokenize(normalizedInput) // TokenizationResult
    val tokens = result.tokens
    val unknowns = result.unknownTokens
    val chunks = tokens.chunked(MAX_LEN - 2)

    val batchInputs = mutableListOf<Map<String, Array<IntArray>>>()
    val batchTexts = mutableListOf<List<String>>()
    var indexUnk = 0

    for (chunk in chunks) {
        val tokensWithSpecials = mutableListOf<String>()
        tokensWithSpecials.add("[CLS]")
        tokensWithSpecials.addAll(chunk)
        tokensWithSpecials.add("[SEP]")

        while (tokensWithSpecials.size < MAX_LEN) {
            tokensWithSpecials.add("[PAD]")
        }

        val inputIds = tokenizer.convertTokensToIds(tokensWithSpecials).toIntArray()
        val attentionMask = IntArray(MAX_LEN) { if (tokensWithSpecials[it] == "[PAD]") 0 else 1 }
        val tokenTypeIds = IntArray(MAX_LEN) { 0 }

        val (text, unkCounter) = reconstructFromTokens(chunk, unknowns, indexUnk)
        indexUnk = unkCounter

        batchTexts.add(text)

        batchInputs.add(
            mapOf(
                "input_ids" to arrayOf(inputIds),
                "attention_mask" to arrayOf(attentionMask),
                "token_type_ids" to arrayOf(tokenTypeIds)
            )
        )
    }

    return TokenizeResult(batchInputs, batchTexts)
}

fun reconstructFromTokens(tokens: List<String>, unknowns: List<UnknownToken>, startUnkCounter: Int): Pair<List<String>, Int> {
    val reconstructedWords = mutableListOf<String>()
    var i = 0
    var unkCounter = startUnkCounter

    while (i < tokens.size) {
        val token = tokens[i]

        if (token == "[UNK]") {
            reconstructedWords.add(unknowns[unkCounter].word)
            Log.d("UNK", "unkCounter = $unkCounter")
            unkCounter++ // se mareste indexul lui unkCounter
            i++
            continue
        }

        if (token.startsWith("##")) {
            if (reconstructedWords.isEmpty()) {
                reconstructedWords.add(token.removePrefix("##"))
            } else {
                val lastWord = reconstructedWords.removeAt(reconstructedWords.size - 1)
                reconstructedWords.add(lastWord + token.removePrefix("##"))
            }
            i++
            continue
        } else {
            reconstructedWords.add(token)
            i++
        }
    }

    return Pair(reconstructedWords, unkCounter)
}