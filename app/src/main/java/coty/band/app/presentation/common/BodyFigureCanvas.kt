package coty.band.app.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coty.band.app.domain.BodyArea
import coty.band.app.domain.BodySegment
import java.nio.file.Files.size

@Composable
fun BodyFigureCanvas(
    segments: List<BodySegment>,
    modifier: Modifier = Modifier,
    figureColor: Color = Color(0xFF424242),
    lineColor: Color = Color(0xFFE53935),
    width: Dp = 260.dp,
    height: Dp = 440.dp
) {
    Canvas(modifier = modifier.size(width, height)) {
        val w = size.width
        val h = size.height

        // Центр и масштаб фигуры
        val cx = w * 0.45f
        val headR = h * 0.06f
        val headTop = h * 0.03f
        val neckTop = headTop + headR * 2
        val shoulderY = neckTop + h * 0.04f
        val chestY    = shoulderY + h * 0.12f
        val waistY    = chestY + h * 0.10f
        val hipY      = waistY + h * 0.10f
        val kneeY     = hipY + h * 0.16f
        val footY     = kneeY + h * 0.16f
        val shoulderW = w * 0.28f
        val hipW      = w * 0.22f
        val waistW    = w * 0.16f
        val stroke    = 5f

        // Head
        drawCircle(figureColor, radius = headR, center = Offset(cx, headTop + headR), style = Stroke(stroke))

        // Neck
        drawLine(figureColor, Offset(cx - 8f, neckTop), Offset(cx - 8f, shoulderY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + 8f, neckTop), Offset(cx + 8f, shoulderY), strokeWidth = stroke)

        // Shoulders
        drawLine(figureColor, Offset(cx - shoulderW, shoulderY), Offset(cx + shoulderW, shoulderY), strokeWidth = stroke)

        // Torso
        drawLine(figureColor, Offset(cx - shoulderW, shoulderY), Offset(cx - waistW, waistY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + shoulderW, shoulderY), Offset(cx + waistW, waistY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx - waistW, waistY), Offset(cx - hipW, hipY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + waistW, waistY), Offset(cx + hipW, hipY), strokeWidth = stroke)

        // Hip line
        drawLine(figureColor, Offset(cx - hipW, hipY), Offset(cx + hipW, hipY), strokeWidth = stroke)

        // Legs
        drawLine(figureColor, Offset(cx - hipW * 0.7f, hipY), Offset(cx - hipW * 0.5f, kneeY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + hipW * 0.7f, hipY), Offset(cx + hipW * 0.5f, kneeY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx - hipW * 0.5f, kneeY), Offset(cx - hipW * 0.4f, footY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + hipW * 0.5f, kneeY), Offset(cx + hipW * 0.4f, footY), strokeWidth = stroke)

        // Arms
        val elbowY = chestY + h * 0.06f
        val wristY = waistY + h * 0.06f
        drawLine(figureColor, Offset(cx - shoulderW, shoulderY), Offset(cx - shoulderW - 12f, elbowY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx - shoulderW - 12f, elbowY), Offset(cx - shoulderW - 6f, wristY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + shoulderW, shoulderY), Offset(cx + shoulderW + 12f, elbowY), strokeWidth = stroke)
        drawLine(figureColor, Offset(cx + shoulderW + 12f, elbowY), Offset(cx + shoulderW + 6f, wristY), strokeWidth = stroke)

        // Segment indicators
        val segmentYMap = mapOf(
            BodyArea.CHEST    to (shoulderY + waistY) / 2,
            BodyArea.WAIST    to waistY,
            BodyArea.HIP      to hipY,
        )

        segments.forEach { seg ->
            val y = segmentYMap[seg.area] ?: return@forEach
            val isLeft = seg.area == BodyArea.WAIST
            val startX = if (isLeft) cx - shoulderW - 20f else cx + shoulderW + 20f
            val endX   = if (isLeft) startX - 16f else startX + 16f

            drawLine(lineColor, Offset(startX, y), Offset(endX, y), strokeWidth = 2f)
            // dot
            drawCircle(lineColor, radius = 3f, center = Offset(startX, y))
        }
    }
}
