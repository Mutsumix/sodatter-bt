package com.mutsumix.sodatterbt.device.epaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream

/**
 * Gicisky 2.9インチ電子ペーパー用タグ画像を生成する (296 × 128 px JPEG)
 *
 * 表示内容:
 * - 播種日
 * - 品種名
 * - QRコード (sodatterbt://cultivation/{id})
 */
object TagImageGenerator {

    private const val WIDTH = 296
    private const val HEIGHT = 128

    fun generate(
        cultivationId: Long,
        cropName: String,
        manufacturer: String,
        seedingDate: String,
        deviceName: String,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        // 背景: 白
        canvas.drawColor(Color.WHITE)

        val paintBlack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
        }

        // 左カラム: テキスト情報
        val leftPad = 12f
        val topPad = 10f

        // 装置名
        paintBlack.textSize = 20f
        paintBlack.typeface = Typeface.DEFAULT
        canvas.drawText("Device $deviceName", leftPad, topPad + 18f, paintBlack)

        // 品種名 (大きめ)
        paintBlack.textSize = 22f
        paintBlack.typeface = Typeface.DEFAULT_BOLD
        val cropDisplayName = if (cropName.length > 8) cropName.substring(0, 8) + "…" else cropName
        canvas.drawText(cropDisplayName, leftPad, topPad + 46f, paintBlack)

        // 種苗メーカー
        paintBlack.textSize = 14f
        paintBlack.typeface = Typeface.DEFAULT
        if (manufacturer.isNotBlank()) {
            val mfgDisplay = if (manufacturer.length > 12) manufacturer.substring(0, 12) + "…" else manufacturer
            canvas.drawText(mfgDisplay, leftPad, topPad + 64f, paintBlack)
        }

        // 播種日
        paintBlack.textSize = 20f
        paintBlack.typeface = Typeface.DEFAULT
        canvas.drawText("播種日 $seedingDate", leftPad, topPad + 90f, paintBlack)

        // アプリ名ウォーターマーク
        paintBlack.textSize = 9f
        paintBlack.alpha = 100
        canvas.drawText("Sodatter-BT", leftPad, HEIGHT - 8f, paintBlack)
        paintBlack.alpha = 255

        // 右カラム: QRコード (ZXing)
        val qrSizeInt = 90
        val qrLeft = WIDTH - qrSizeInt - 12
        val qrTop = (HEIGHT - qrSizeInt) / 2
        val qrContent = "sodatterbt://cultivation/$cultivationId"
        val qrBitmap = generateQrBitmap(qrContent, qrSizeInt)
        canvas.drawBitmap(qrBitmap, qrLeft.toFloat(), qrTop.toFloat(), null)
        qrBitmap.recycle()

        // 区切り線
        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(WIDTH - qrSizeInt - 24f, 12f, WIDTH - qrSizeInt - 24f, HEIGHT - 12f, dividerPaint)

        // JPEG出力
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
