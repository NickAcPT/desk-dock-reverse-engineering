package io.github.nickacpt.reverseengineering.deskdock.plugin.providers

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.ConfigurationConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.MavenConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.getDependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.getVirtualMavenRepository
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import java.nio.file.Path

abstract class DeskDockArtifactProvider {
    open val name: String get() = DependencyConstants.DESKDOCK_ARTIFACT
    open val repoName: String get() = MavenConstants.DESKDOCK_MAVEN_REPOSITORY

    abstract val classifier: String?

    fun provide(project: Project) {
        val repo = project.getVirtualMavenRepository(repoName)
        val originalDependency = project.getDependencyInfo(ConfigurationConstants.DESKDOCK_CONFIGURATION_NAME)

        val newDependency = originalDependency.copy(
            artifact = name,
            classifier = classifier
        )

        val finalArtifact = provideArtifact(project)
        repo.publishDependency(newDependency, finalArtifact)

        println(newDependency.notation)
        val config = project.configurations.create("amogus").also {
            project.configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(it)
        }
        val dep = project.dependencies.add("amogus", newDependency.notation)

        config.files(dep!!)


    }

    protected abstract fun provideArtifact(project: Project): Path
}