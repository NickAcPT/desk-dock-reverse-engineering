package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.exists

abstract class DeskDockArtifactProvider {
    open val name: String get() = Constants.DESKDOCK_ARTIFACT

    open val addToProject: Boolean get() = false

    abstract val classifier: String?

    fun provide(project: Project): Path {
        val repo = project.workspace.repository
        val originalDependency = project.workspace.deskDockDependencyInfo

        val newDependency = originalDependency.copy(
            artifact = name,
            classifier = classifier
        )

        val artifactPath = repo.getPath(newDependency)
        if (artifactPath.exists()) {
            return artifactPath
        }

        if (addToProject) {
            val config = project.configurations.maybeCreate("amogus")
            project.configurations.findByName("implementation")!!.extendsFrom(config)

            project.dependencies.add("amogus", newDependency.notation)
        }

        return repo.publishDependency(newDependency, provideArtifact(project, newDependency))
    }

    protected abstract fun provideArtifact(project: Project, newDependency: DependencyInfo): Path
}