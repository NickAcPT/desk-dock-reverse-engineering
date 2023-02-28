package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.nio.file.Path

abstract class DeskDockArtifactProvider {
    open val name: String get() = Constants.DESKDOCK_ARTIFACT

    abstract val classifier: String?

    fun provide(project: Project): Path {
        val repo = project.workspace.repository
        val originalDependency = project.workspace.deskDockDependencyInfo

        val newDependency = originalDependency.copy(
            artifact = name,
            classifier = classifier
        )

        return repo.publishDependency(newDependency, provideArtifact(project))
    }

    protected abstract fun provideArtifact(project: Project): Path
}