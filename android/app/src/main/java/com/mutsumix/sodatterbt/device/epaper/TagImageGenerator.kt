package com.mutsumix.sodatterbt.device.epaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream

/**
 * Gicisky 2.9インチ電子ペーパー用タグ画像を生成する (296 × 128 px JPEG)
 *
 * 表示内容:
 * - 播種日
 * - 品種名
 * - QRコード領域 (QRコードはSodatterBTの`sodatterbt://cultivation/{id}`スキーム)
 *   → 実際のQRビットマップはzxing等のライブラリで生成するが、
 *     本実装では依存追加を避けてプレースホルダーBoxを描画する
 */
object TagImageGenerator {

    private const val WIDTH = 296
    private const val HEIGHT = 128

    fun generate(
        cultivationId: Long,
        cropName: String,
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
        val topPad = 16f

        // 装置名バッジ
        paintBlack.textSize = 11f
        paintBlack.typeface = Typeface.DEFAULT
        canvas.drawText("Device $deviceName", leftPad, topPad + 11f, paintBlack)

        // 品種名 (大きめ)
        paintBlack.textSize = 22f
        paintBlack.typeface = Typeface.DEFAULT_BOLD
        val cropDisplayName = if (cropName.length > 8) cropName.substring(0, 8) + "…" else cropName
        canvas.drawText(cropDisplayName, leftPad, topPad + 44f, paintBlack)

        // 播種日
        paintBlack.textSize = 12f
        paintBlack.typeface = Typeface.DEFAULT
        canvas.drawText("播種 $seedingDate", leftPad, topPad + 66f, paintBlack)

        // アプリ名ウォーターマーク
        paintBlack.textSize = 9f
        paintBlack.alpha = 100
        canvas.drawText("Sodatter-BT", leftPad, HEIGHT - 8f, paintBlack)
        paintBlack.alpha = 255

        // 右カラム: QRコードプレースホルダー
        // (実際の実装ではzxing-androidで生成したBitmapを描画する)
        val qrSize = 90f
        val qrLeft = WIDTH - qrSize - 12f
        val qrTop = (HEIGHT - qrSize) / 2f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize, borderPaint)
        paintBlack.textSize = 8f
        canvas.drawText("QR", qrLeft + qrSize / 2f - 7f, qrTop + qrSize / 2f + 3f, paintBlack)

        // 区切り線
        val dividerPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(WIDTH - qrSize - 24f, 12f, WIDTH - qrSize - 24f, HEIGHT - 12f, dividerPaint)

        // JPEG出力
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        bitmap.recycle()
        return out.toByteArray()
    }
}
