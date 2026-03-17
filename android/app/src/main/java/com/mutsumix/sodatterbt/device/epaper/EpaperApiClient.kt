package com.mutsumix.sodatterbt.device.epaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
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

data class EpaperTag(
    val mac: String,
    val alias: String,
    val hwType: Int,
    val rssi: Int,
)

@Singleton
class EpaperApiClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * ESP32 APに接続されているタグ一覧を取得する
     */
    suspend fun fetchTags(apIpAddress: String): List<EpaperTag> = withContext(Dispatchers.IO) {
        val allTags = mutableListOf<EpaperTag>()
        var pos: Int? = null
        try {
            do {
                val url = if (pos != null) {
                    "http://$apIpAddress/get_db?pos=$pos"
                } else {
                    "http://$apIpAddress/get_db"
                }
                val request = Request.Builder().url(url).get().build()
                val body = client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList()
                    response.body?.string() ?: return@withContext emptyList()
                }
                val json = JSONObject(body)
                val tagsArray = json.optJSONArray("tags") ?: break
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.getJSONObject(i)
                    allTags.add(
                        EpaperTag(
                            mac = tag.optString("mac", ""),
                            alias = tag.optString("alias", ""),
                            hwType = tag.optInt("hwType", 0),
                            rssi = tag.optInt("RSSI", 0),
                        )
                    )
                }
                pos = if (json.has("continu")) json.getInt("continu") else null
            } while (pos != null)
        } catch (_: IOException) {
            // 接続失敗時は空リスト
        } catch (_: Exception) {
            // JSONパース失敗等
        }
        allTags
    }

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
            .addFormDataPart("mac", tagMacAddress)
            .addFormDataPart("dither", "0")
            .addFormDataPart(
                "file", "image.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaType()),
            )
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
        manufacturer: String,
        seedingDate: String,
        deviceName: String,
    ): EpaperResult {
        val imageBytes = TagImageGenerator.generate(
            cultivationId = cultivationId,
            cropName = cropName,
            manufacturer = manufacturer,
            seedingDate = seedingDate,
            deviceName = deviceName,
        )
        return upload(apIpAddress, tagMacAddress, imageBytes)
    }
}
