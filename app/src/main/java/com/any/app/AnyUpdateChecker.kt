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
    val assetUrls: List<String>
)

object AnyUpdateConfig {
    // Set this to "owner/repository" when the Any GitHub repository exists.
    const val githubRepository = ""
}

object AnyUpdateChecker {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun checkLatestRelease(
        repository: String = AnyUpdateConfig.githubRepository,
        onSuccess: (GithubRelease) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!repository.matches(Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$"))) {
            mainHandler.post { onError("尚未配置有效的 GitHub 仓库") }
            return
        }

        executor.execute {
            try {
                val connection = (URL(
                    "https://api.github.com/repos/$repository/releases/latest"
                ).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "Any-Android-App")
                }

                try {
                    if (connection.responseCode !in 200..299) {
                        throw IllegalStateException("GitHub 返回 HTTP ${connection.responseCode}")
                    }

                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val assets = buildList {
                        val items = json.optJSONArray("assets") ?: return@buildList
                        for (index in 0 until items.length()) {
                            items.optJSONObject(index)
                                ?.optString("browser_download_url")
                                ?.takeIf { it.isNotBlank() }
                                ?.let(::add)
                        }
                    }
                    mainHandler.post {
                        onSuccess(
                            GithubRelease(
                                tagName = json.optString("tag_name", "未知版本"),
                                title = json.optString("name", "Any 最新版本"),
                                releaseUrl = json.optString("html_url", ""),
                                assetUrls = assets
                            )
                        )
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (error: Exception) {
                mainHandler.post { onError(error.message ?: "检查更新失败") }
            }
        }
    }
}
