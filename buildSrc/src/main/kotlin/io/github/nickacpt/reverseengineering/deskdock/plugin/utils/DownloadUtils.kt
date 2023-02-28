package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import org.gradle.api.Project
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.writeBytes

object DownloadUtils {
    private const val DOWNLOAD_CACHE_PATH_NAME = "downloads"

    fun downloadFile(project: Project, url: URL, name: String): Path {
        return project.fetchOrGetCachedFile(DOWNLOAD_CACHE_PATH_NAME, name) {
            it.writeBytes(url.readBytes())
        }
    }
}