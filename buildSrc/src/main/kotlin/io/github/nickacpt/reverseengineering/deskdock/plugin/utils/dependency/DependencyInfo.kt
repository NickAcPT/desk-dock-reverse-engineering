package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div

data class DependencyInfo(
    private val project: Project,
    private val configuration: Configuration,
    val group: String?,
    val artifact: String,
    val version: String?,
    val classifier: String? = null
) {
    val notation by lazy {
        listOfNotNull(group, artifact, version, classifier).joinToString(":") { it }
    }

    private var resolvedFiles: Collection<File>? = null

    fun resolve(): Collection<File> {
        return resolvedFiles ?: (configuration.resolve().also { resolvedFiles = it })
    }

    private fun directoryPath() = listOfNotNull(
        *(group?.split('.', '/')?.toTypedArray() ?: emptyArray()),
        artifact,
        version
    ).fold(Path(".")) { acc, it -> acc / it }

    private fun filePath() = listOfNotNull(artifact, version, classifier)
        .joinToString(postfix = ".jar", separator = "-") { it }

    internal fun finalMavenArtifactFilePath() = directoryPath() / filePath()
}