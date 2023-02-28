package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.MavenConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.deskDockDependency
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.getVirtualMavenRepository
import org.gradle.api.Project
import java.nio.file.Path

abstract class DeskDockArtifactProvider {
    open val name: String get() = DependencyConstants.DESKDOCK_ARTIFACT
    open val repoName: String get() = MavenConstants.DESKDOCK_MAVEN_REPOSITORY

    abstract val classifier: String?

    fun provide(project: Project) {
        val repo = project.getVirtualMavenRepository(repoName)
        val originalDependency = project.deskDockDependency

        val newDependency = originalDependency.copy(
            artifact = name,
            classifier = classifier
        )

        val finalArtifact = provideArtifact(project)
        repo.publishDependency(newDependency, finalArtifact)
    }

    protected abstract fun provideArtifact(project: Project): Path
}