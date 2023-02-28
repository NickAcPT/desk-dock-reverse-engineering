package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.getCacheFilePath
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories
import java.nio.file.Path
import kotlin.io.path.*

data class VirtualMavenRepository(
    val project: Project,
    val name: String
) {
    private val repoName get() = "deskdock-${name}"

    private val path = project.getCacheFilePath("repos", name).createDirectories()

    fun addRepository() {
        if (!project.repositories.names.none { it == repoName }) return

        project.repositories {
            maven {
                name = repoName
                url = path.toUri()
            }
        }
    }

    fun publishDependency(dependencyInfo: DependencyInfo, original: Path) {
        val path = path / dependencyInfo.finalMavenArtifactFilePath()
        path.parent.createDirectories()

        original.copyTo(path, true)

    }

    fun publishDependency(dependencyInfo: DependencyInfo, file: ByteArray) {
        val path = path / dependencyInfo.finalMavenArtifactFilePath()
        path.parent.createDirectories()
        path.deleteIfExists()

        path.writeBytes(file)
    }

}