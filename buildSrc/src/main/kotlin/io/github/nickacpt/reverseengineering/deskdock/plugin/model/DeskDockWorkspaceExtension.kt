package io.github.nickacpt.reverseengineering.deskdock.plugin.model

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfoUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.VirtualMavenRepository
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.getVirtualMavenRepository
import org.gradle.api.Project

open class DeskDockWorkspaceExtension {
    internal lateinit var deskDockDependencyInfo: DependencyInfo
    internal lateinit var repository: VirtualMavenRepository
    internal lateinit var type: WorkspaceType

    var intermediaryObfuscationPattern: String? = null

    internal fun Project.initWorkspace() {
        // Create configuration
        configurations.create(Constants.DESKDOCK_CONFIGURATION_NAME)

        // Create virtual maven repository for storing artifacts
        repository = getVirtualMavenRepository(Constants.DESKDOCK_MAVEN_REPOSITORY)
        repository.addRepository()
    }

    fun Project.initAfterEvaluate() {
        // Create dependency info for the main deskdock dependency
        initDeskDockDependencyInfo()

        // Initialize our workspace type (whether it is the server or the client)
        initWorkspaceType()
    }

    private fun Project.initDeskDockDependencyInfo() {
        deskDockDependencyInfo =
            DependencyInfoUtils.getProjectDependency(this, Constants.DESKDOCK_CONFIGURATION_NAME)

        check(deskDockDependencyInfo.group == Constants.DESKDOCK_GROUP) { "Expected deskdock group to be ${Constants.DESKDOCK_GROUP}." }
        check(deskDockDependencyInfo.classifier != Constants.DESKDOCK_ARTIFACT) { "Expected deskdock group to be ${Constants.DESKDOCK_ARTIFACT}." }
        check(deskDockDependencyInfo.version != null) { "Artifact version cannot be missing!" }
    }

    private fun initWorkspaceType() {
        val type = WorkspaceType.values().firstOrNull { it.name.equals(deskDockDependencyInfo.classifier, true) }

        check(type != null) {
            "Expected artifact classifier to be any of [${WorkspaceType.values().joinToString { it.toString() }}]"
        }

        this.type = type
    }
}