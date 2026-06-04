package coty.band.app.presentation.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    fun compress(
        source: File,
        maxSizeBytes: Int = 2 * 1024 * 1024,
        maxDimensionPx: Int = 1600
    ): File {
        // 1. Читаем только размеры, не загружая всю картинку в память
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "Не удалось прочитать изображение"
        }

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calcInSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
        }
        var bitmap = BitmapFactory.decodeFile(source.absolutePath, decodeOpts)
            ?: throw IllegalStateException("Не удалось декодировать изображение")

        bitmap = applyExifRotation(source, bitmap)
        bitmap = scaleToMaxDimension(bitmap, maxDimensionPx)

        val out = File(source.parentFile, "cmp_${source.name}")
        var quality = 90
        do {
            FileOutputStream(out).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            }
            quality -= 10
        } while (out.length() > maxSizeBytes && quality >= 40)

        if (out.length() > maxSizeBytes) {
            val smaller = scaleToMaxDimension(bitmap, (maxDimensionPx * 0.75).toInt())
            FileOutputStream(out).use { fos ->
                smaller.compress(Bitmap.CompressFormat.JPEG, 75, fos)
            }
            if (smaller != bitmap) smaller.recycle()
        }

        bitmap.recycle()
        return out
    }

    private fun calcInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= maxDim || h / 2 >= maxDim) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun scaleToMaxDimension(src: Bitmap, maxDim: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxDim) return src
        val ratio = maxDim.toFloat() / longest
        val newW = (src.width * ratio).toInt()
        val newH = (src.height * ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(src, newW, newH, true)
        if (scaled != src) src.recycle()
        return scaled
    }

    private fun applyExifRotation(source: File, bitmap: Bitmap): Bitmap {
        val orientation = ExifInterface(source.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }
}