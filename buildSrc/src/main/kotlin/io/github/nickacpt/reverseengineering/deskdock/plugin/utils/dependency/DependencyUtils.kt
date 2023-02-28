package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceType
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.ConfigurationConstants.DESKDOCK_CONFIGURATION_NAME
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.DependencyConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.getDependencyInfo
import org.gradle.api.Project

val Project.deskDockDependency get() = project.getDependencyInfo(DESKDOCK_CONFIGURATION_NAME)

val Project.deskDockWorkspaceType: WorkspaceType
    get() {
        val dependency = deskDockDependency

        check(dependency.group == DependencyConstants.DESKDOCK_GROUP) { "Expected deskdock group to be ${DependencyConstants.DESKDOCK_GROUP}." }
        check(dependency.classifier != DependencyConstants.DESKDOCK_ARTIFACT) { "Expected deskdock group to be ${DependencyConstants.DESKDOCK_ARTIFACT}." }
        check(dependency.version != null) { "Artifact version cannot be missing!" }
        val type = WorkspaceType.values().firstOrNull { it.name.equals(dependency.classifier, true) }

        check(type != null) {
            "Expected artifact classifier to be any of [${WorkspaceType.values().joinToString { it.toString() }}]"
        }

        return type
    }