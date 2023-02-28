package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
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

    private fun directoryPath() = listOfNotNull(
        *(group?.split('.', '/')?.toTypedArray() ?: emptyArray()),
        artifact,
        version
    ).fold(Path(".")) { acc, it -> acc / it }

    private fun filePath() = listOfNotNull(artifact, version, classifier)
        .joinToString(postfix = ".jar", separator = "-") { it }

    internal fun finalMavenArtifactFilePath() = directoryPath() / filePath()
}

object DependencyInfoUtils {
    fun getProjectDependency(project: Project, configuration: String): DependencyInfo {
        val config = project.configurations.getByName(configuration)
        val depends = config.dependencies

        if (depends.isEmpty()) {
            throw Exception("Dependency for \"$configuration\" configuration is missing.")
        } else if (depends.size != 1) {
            throw Exception("Configuration \"$configuration\" can only have one (1) dependency")
        }

        val dependency = depends.first()

        return DependencyInfo(
            project,
            config,
            dependency.group,
            dependency.name,
            dependency.version,
            (dependency as? ExternalDependency)?.artifacts?.firstOrNull()?.classifier
        )
    }

}