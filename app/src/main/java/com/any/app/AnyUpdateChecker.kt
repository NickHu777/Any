package com.any.app

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class GithubRelease(
    val tagName: String,
    val title: String,
    val releaseUrl: String,
    val assetUrls: List<String>,
    val downloadUrl: String,
    val sha256: String,
    val source: String
)

object AnyUpdateConfig {
    const val giteeRepository = "doggy-original-licensing/Any"
    const val githubRepository = "NickHu777/Any"
}

object AnyUpdateChecker {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun checkLatestRelease(
        onSuccess: (GithubRelease) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            val sources = listOf(
                "Gitee" to "https://gitee.com/api/v5/repos/${AnyUpdateConfig.giteeRepository}/releases/latest",
                "GitHub" to "https://api.github.com/repos/${AnyUpdateConfig.githubRepository}/releases/latest"
            )
            var lastError = "检查更新失败"
            for ((source, apiUrl) in sources) {
                try {
                    val release = fetchRelease(apiUrl, source)
                    if (release.downloadUrl.isNotBlank()) {
                        mainHandler.post { onSuccess(release) }
                        return@execute
                    }
                    lastError = "$source 没有可用 APK"
                } catch (error: Exception) {
                    lastError = "$source：${error.message ?: "请求失败"}"
                }
            }
            mainHandler.post { onError(lastError) }
        }
    }

    private fun fetchRelease(apiUrl: String, source: String): GithubRelease {
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Any-Android-App")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val assets = buildList {
                val items = json.optJSONArray("assets") ?: return@buildList
                for (index in 0 until items.length()) {
                    items.optJSONObject(index)
                        ?.optString("browser_download_url")
                        ?.takeIf { it.isNotBlank() }
                        ?.let(::add)
                }
            }
            val apkAsset = json.optJSONArray("assets")?.let { items ->
                (0 until items.length())
                    .mapNotNull { items.optJSONObject(it) }
                    .firstOrNull { it.optString("name").endsWith(".apk", ignoreCase = true) }
            }
            return GithubRelease(
                tagName = json.optString("tag_name", "未知版本"),
                title = json.optString("name", "Any 最新版本"),
                releaseUrl = json.optString("html_url", ""),
                assetUrls = assets,
                downloadUrl = apkAsset?.optString("browser_download_url", "").orEmpty(),
                sha256 = apkAsset?.optString("digest", "")?.removePrefix("sha256:").orEmpty(),
                source = source
            )
        } finally {
            connection.disconnect()
        }
    }
}
