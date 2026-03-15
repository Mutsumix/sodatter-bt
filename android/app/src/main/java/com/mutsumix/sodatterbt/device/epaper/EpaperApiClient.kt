package com.mutsumix.sodatterbt.device.epaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class EpaperResult {
    object Success : EpaperResult()
    data class Failure(val message: String) : EpaperResult()
}

@Singleton
class EpaperApiClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * タグ画像をESP32 (OpenEPaperLink AP) に送信する
     *
     * @param apIpAddress ESP32のIPアドレス (例: "192.168.4.1")
     * @param tagMacAddress タグのMACアドレス (例: "AA:BB:CC:DD:EE:FF")
     * @param imageBytes 296×128 JPEG画像のバイト列
     */
    suspend fun upload(
        apIpAddress: String,
        tagMacAddress: String,
        imageBytes: ByteArray,
    ): EpaperResult = withContext(Dispatchers.IO) {
        val url = "http://$apIpAddress/imgupload"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "image.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaType()),
            )
            .addFormDataPart("mac", tagMacAddress)
            .addFormDataPart("dither", "0")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    EpaperResult.Success
                } else {
                    EpaperResult.Failure("HTTPエラー: ${response.code}")
                }
            }
        } catch (e: IOException) {
            EpaperResult.Failure(e.message ?: "通信エラー")
        }
    }

    /**
     * 播種登録後にタグ表示を更新する便利メソッド
     */
    suspend fun updateTag(
        apIpAddress: String,
        tagMacAddress: String,
        cultivationId: Long,
        cropName: String,
        seedingDate: String,
        deviceName: String,
    ): EpaperResult {
        val imageBytes = TagImageGenerator.generate(
            cultivationId = cultivationId,
            cropName = cropName,
            seedingDate = seedingDate,
            deviceName = deviceName,
        )
        return upload(apIpAddress, tagMacAddress, imageBytes)
    }
}
