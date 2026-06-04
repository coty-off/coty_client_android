package coty.band.app.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Схема тела с пунктирными линиями обхватов и выносками,
 * на которых подписан сегмент и его обхват в сантиметрах.
 *
 * Силуэт рисуется слева, выноски и подписи — справа.
 */
@Composable
fun BodyMeasurementDiagram(
    chestCm: Float,
    waistCm: Float,
    hipCm: Float,
    modifier: Modifier = Modifier,
    figureFill: Color = Color(0xFFE3DCD5),
    figureStroke: Color = Color(0xFF8D8378),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Центр фигуры в левой части, базовая полуширина
        val cx = w * 0.26f
        val bw = h * 0.12f

        // Вертикальные уровни
        val headCy   = h * 0.085f
        val headR    = h * 0.055f
        val neckY    = headCy + headR + h * 0.012f
        val shoulderY = h * 0.17f
        val chestY   = h * 0.30f
        val waistY   = h * 0.45f
        val hipY     = h * 0.60f
        val bottomY  = h * 0.74f

        // Полуширины на каждом уровне
        val neckHW     = bw * 0.20f
        val shoulderHW = bw * 1.05f
        val chestHW    = bw * 0.92f
        val waistHW    = bw * 0.60f
        val hipHW      = bw * 1.10f
        val bottomHW   = bw * 0.80f

        // Силуэт торса (плечи -> талия -> бёдра -> верх бедра)
        val body = Path().apply {
            moveTo(cx - neckHW, neckY)
            lineTo(cx - shoulderHW, shoulderY)
            quadraticBezierTo(cx - chestHW, chestY, cx - waistHW, waistY)
            quadraticBezierTo(cx - waistHW, (waistY + hipY) / 2f, cx - hipHW, hipY)
            quadraticBezierTo(cx - hipHW, (hipY + bottomY) / 2f, cx - bottomHW, bottomY)
            lineTo(cx + bottomHW, bottomY)
            quadraticBezierTo(cx + hipHW, (hipY + bottomY) / 2f, cx + hipHW, hipY)
            quadraticBezierTo(cx + waistHW, (waistY + hipY) / 2f, cx + waistHW, waistY)
            quadraticBezierTo(cx + chestHW, chestY, cx + shoulderHW, shoulderY)
            lineTo(cx + neckHW, neckY)
            close()
        }
        drawPath(body, color = figureFill)
        drawPath(body, color = figureStroke, style = Stroke(width = 3f))

        // Шея
        drawLine(figureStroke, Offset(cx - neckHW, neckY), Offset(cx - neckHW, headCy + headR * 0.6f), 3f)
        drawLine(figureStroke, Offset(cx + neckHW, neckY), Offset(cx + neckHW, headCy + headR * 0.6f), 3f)

        // Голова
        drawCircle(figureFill, headR, Offset(cx, headCy))
        drawCircle(figureStroke, headR, Offset(cx, headCy), style = Stroke(3f))

        // Линии обхватов + выноски
        val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        val pad = bw * 0.45f
        val labelX = w * 0.50f

        data class Seg(val title: String, val value: Float, val y: Float, val hw: Float)
        val segments = listOf(
            Seg("Обхват груди", chestCm, chestY, chestHW),
            Seg("Обхват талии", waistCm, waistY, waistHW),
            Seg("Обхват бёдер", hipCm,   hipY,   hipHW)
        )

        segments.forEach { s ->
            val left  = cx - s.hw - pad
            val right = cx + s.hw + pad

            // Пунктирная линия поперёк фигуры
            drawLine(lineColor, Offset(left, s.y), Offset(right, s.y), strokeWidth = 4f, pathEffect = dash)
            // Точка на правом крае и выноска к подписи
            drawCircle(lineColor, 6f, Offset(right, s.y))
            drawLine(lineColor, Offset(right, s.y), Offset(labelX - 10f, s.y), strokeWidth = 3f)

            // Подписи: название сегмента + значение обхвата
            val titleLayout = textMeasurer.measure(
                s.title,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
            )
            val valueLayout = textMeasurer.measure(
                String.format(Locale.getDefault(), "%.2f см", s.value),
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = valueColor)
            )
            drawText(titleLayout, topLeft = Offset(labelX, s.y - titleLayout.size.height - 1f))
            drawText(valueLayout, topLeft = Offset(labelX, s.y + 2f))
        }
    }
}