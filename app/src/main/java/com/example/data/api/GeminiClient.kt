package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        model: String = "gemini-3.5-flash",
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API key is missing or is placeholder.")
            return@withContext "Please configure your Gemini API Key in the Secrets panel in AI Studio."
        }

        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestJson.put("contents", contentsArray)

        if (systemInstruction != null) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysParts.put(sysPartObj)
            sysObj.put("parts", sysParts)
            requestJson.put("systemInstruction", sysObj)
        }

        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed: Code ${response.code}, Body: $errorBody")
                    return@withContext "Error: Failed with code ${response.code}. Please verify your API Key."
                }
                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body"
                
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                        }
                    }
                }
                "No output generated from the AI model."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call", e)
            "Network Exception: ${e.message}. Please check your connection."
        }
    }
}
