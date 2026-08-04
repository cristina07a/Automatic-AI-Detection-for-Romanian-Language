package com.example.android_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Typeface.ITALIC
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

object PdfHelper {

    fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Float,
        lineHeight: Float
    ): List<String> {
        val words = text.split(" ")
        var line = ""
        val lines = mutableListOf<String>()

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val textWidth = paint.measureText(testLine)
            if (textWidth > maxWidth) {
                lines.add(line)
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) lines.add(line)
        return lines
    }


    fun generatePredictionPdf(
        context: Context,
        batchPredictionsMap: Map<String, BatchPrediction>,
        fileName: String = "prediction_result.pdf"
    ): File? {
        val pageWidth = 595
        val pageHeight = 842f
        val marginTop = 40f
        val marginBottom = 40f
        val maxTextWidth = pageWidth - 20f
        val lineHeight = 20f

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight.toInt(), 1).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        val robotoFlex = Typeface.createFromAsset(context.assets, "font/RobotoFlex.ttf")

        val textPaint = Paint().apply {
            typeface = robotoFlex
            color = Color.BLACK
            textSize = 12f
        }

        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_round)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 75, 75, true)
        canvas.drawBitmap(scaledLogo, 30f, 20f, null)

        textPaint.textSize = 15f
        textPaint.typeface = Typeface.create(robotoFlex, Typeface.BOLD)
        canvas.drawText("ARTIFICIAL INTELLIGENCE DETECTION", 110f, 50f, textPaint)

        var yPosition = 140f

        val title = "CONTENT ANALYSIS"
        val textWidth = textPaint.measureText(title)
        val xPos = (pageWidth - textWidth) / 2f - 25f
        textPaint.textSize = 18f
        canvas.drawText(title, xPos, yPosition, textPaint)
        yPosition += 30f

        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(robotoFlex, Typeface.BOLD)
        canvas.drawText("OVERVIEW:", 10f, yPosition, textPaint)
        yPosition += lineHeight + 5f

        textPaint.textSize = 12f
        textPaint.typeface = robotoFlex

        val totalBatches = batchPredictionsMap.size
        val aiBatches = batchPredictionsMap.values.count { it.prediction == "AI" }
        val notAiBatches = totalBatches - aiBatches
        val averageAiPercentage = if (totalBatches > 0) {
            batchPredictionsMap.values.map { it.aiPercentage }.average()
        } else 0.0

        canvas.drawText("Total parts analyzed: $totalBatches", 10f, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("AI detected parts: $aiBatches", 10f, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("NOT AI parts: $notAiBatches", 10f, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Average AI confidence: ${String.format("%.1f", averageAiPercentage)}%", 10f, yPosition, textPaint)
        yPosition += 30f

        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(robotoFlex, Typeface.BOLD)

        batchPredictionsMap.forEach { (indexStr, batchPrediction) ->
            val index = indexStr.toIntOrNull() ?: 0

            val titleText = "PART ${index + 1}\n"
            val subtitleText = "AI confidence: ${String.format("%.1f", batchPrediction.aiPercentage)}%"

            textPaint.textSize = 14f
            textPaint.typeface = Typeface.create(robotoFlex, Typeface.BOLD)
            canvas.drawText(titleText, 10f, yPosition, textPaint)
            yPosition += lineHeight

            textPaint.textSize = 12f
            textPaint.typeface = robotoFlex
            canvas.drawText(subtitleText, 10f, yPosition, textPaint)
            yPosition += lineHeight

            val firstChar = batchPrediction.prediction.firstOrNull() ?: ' '
            textPaint.color = when (firstChar) {
                'A' -> Color.RED
                'N' -> Color.GREEN
                else -> Color.BLACK
            }

            val lines = drawMultilineText(canvas, batchPrediction.tokensText, 10f, yPosition, textPaint, maxTextWidth, lineHeight)
            for (line in lines) {
                if (yPosition + lineHeight > pageHeight - marginBottom) {
                    pdfDocument.finishPage(currentPage)
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    yPosition = marginTop
                }
                canvas.drawText(line, 10f, yPosition, textPaint)
                yPosition += lineHeight
            }

            textPaint.color = Color.BLACK
            textPaint.typeface = Typeface.create(robotoFlex, Typeface.BOLD)
            yPosition += 10f
        }

        pdfDocument.finishPage(currentPage)

        val path = context.getExternalFilesDir(null)
        if (path != null && !path.exists()) path.mkdirs()
        val file = File(path, fileName)
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Log.d("PDF", "PDF saved successfully at: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("PDF", "Error saving PDF: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun extractTextFromPdf(context: Context, uri: Uri): String {
        return try {
            PDFBoxResourceLoader.init(context)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            "Eroare la extragerea textului din PDF"
        }
    }
}

