package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

object CachedFilesUtils {

    private fun getCachePath(project: Project): Path {
        return (project.gradle.gradleUserHomeDir.toPath() / "caches" / "deskdock-reversing").createDirectories()
    }

    fun getCacheFilePath(project: Project, vararg names: String): Path {
        return names.fold(getCachePath(project)) { acc, value -> acc / value }
    }

    fun fetchOrGetCachedFile(project: Project, vararg names: String, fileFetcher: (Path) -> Unit): Path {
        val file = getCacheFilePath(project, *names)
        file.parent.createDirectories()

        if (!file.exists()) {
            fileFetcher(file)
        }

        return file
    }

}