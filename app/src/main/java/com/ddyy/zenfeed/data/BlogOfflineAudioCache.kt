package com.ddyy.zenfeed.data

import android.content.Context
import com.ddyy.zenfeed.data.network.ApiClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class BlogOfflineAudioCache(context: Context) {
    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.filesDir, DIR_NAME).apply { mkdirs() }

    fun getCacheSizeBytes(): Long {
        if (!cacheDir.exists()) return 0L
        return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearCache(): Boolean {
        if (!cacheDir.exists()) return true
        return cacheDir.deleteRecursively() && cacheDir.mkdirs()
    }

    fun getLocalFileIfExists(podcastUrl: String, serverId: String?): File? {
        if (podcastUrl.isBlank()) return null
        val file = getLocalFile(podcastUrl, serverId)
        return file.takeIf { it.exists() && it.length() > 0 }
    }

    fun deleteFor(podcastUrl: String, serverId: String?): Boolean {
        if (podcastUrl.isBlank()) return false
        val file = getLocalFile(podcastUrl, serverId)
        return !file.exists() || file.delete()
    }

    suspend fun downloadIfNeeded(podcastUrl: String, serverId: String?): File {
        val existing = getLocalFileIfExists(podcastUrl, serverId)
        if (existing != null) return existing

        val target = getLocalFile(podcastUrl, serverId)
        val temp = File(target.absolutePath + ".tmp")

        val client = ApiClient.getHttpClient(appContext)
        val request = Request.Builder().url(podcastUrl).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("下载失败: ${response.code}")
        }

        response.body.let { responseBody ->
            FileOutputStream(temp).use { output ->
                responseBody.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }

        if (temp.length() <= 0) {
            temp.delete()
            throw Exception("下载失败: 文件为空")
        }

        if (target.exists()) target.delete()
        if (!temp.renameTo(target)) {
            val copied = temp.copyTo(target, overwrite = true)
            temp.delete()
            return copied
        }

        return target
    }

    fun saveFromExistingFile(podcastUrl: String, serverId: String?, sourceFile: File): File {
        val target = getLocalFile(podcastUrl, serverId)
        if (target.exists() && target.length() > 0) return target
        target.parentFile?.let { parent ->
            if (!parent.exists()) parent.mkdirs()
        }
        return sourceFile.copyTo(target, overwrite = true)
    }

    private fun getLocalFile(podcastUrl: String, serverId: String?): File {
        val key = sha256Hex("${serverId.orEmpty()}|$podcastUrl")
        return File(cacheDir, "$key.audio")
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString(separator = "") { b -> "%02x".format(b) }
    }

    companion object {
        private const val DIR_NAME = "blog_offline_audio"
    }
}
