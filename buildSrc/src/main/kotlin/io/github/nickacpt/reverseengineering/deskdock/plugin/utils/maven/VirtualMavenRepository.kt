package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.getCacheFilePath
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.div

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

                this.metadataSources {
                    this.artifact()
                }
            }
        }
    }

    fun publishDependency(dependencyInfo: DependencyInfo, original: Path): Path {
        val finalPath = path / dependencyInfo.finalMavenArtifactFilePath()
        finalPath.parent.createDirectories()

        original.copyTo(finalPath, true)

        return finalPath
    }

}