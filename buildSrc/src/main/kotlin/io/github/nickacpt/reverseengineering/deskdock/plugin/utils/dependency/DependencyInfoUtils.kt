package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency

object DependencyInfoUtils {
    fun getProjectDependencies(project: Project, configuration: String): List<DependencyInfo> {
        val config = project.configurations.getByName(configuration)
        val depends = config.dependencies
        if (depends.isEmpty()) {
            throw Exception("Dependency for \"$configuration\" configuration is missing.")
        }

        return depends.map {
            DependencyInfo(
                project,
                config,
                it.group,
                it.name,
                it.version,
                (it as? ExternalDependency)?.artifacts?.firstOrNull()?.classifier
            )
        }
    }

    fun getProjectDependency(project: Project, configuration: String): DependencyInfo {
        val depends = getProjectDependencies(project, configuration)
        if (depends.size != 1) {
            throw Exception("Configuration \"$configuration\" can only have one (1) dependency")
        }

        return depends.first()
    }

}