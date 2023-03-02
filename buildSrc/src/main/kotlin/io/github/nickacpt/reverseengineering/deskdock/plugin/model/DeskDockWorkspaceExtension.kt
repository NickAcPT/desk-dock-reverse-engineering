package io.github.nickacpt.reverseengineering.deskdock.plugin.model

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfo
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfoUtils
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.VirtualMavenRepository
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.getVirtualMavenRepository
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

open class DeskDockWorkspaceExtension {
    internal lateinit var deskDockDependencyInfo: DependencyInfo
    internal lateinit var enigmaDependencyInfo: DependencyInfo
    internal lateinit var repository: VirtualMavenRepository
    internal lateinit var type: WorkspaceType

    var intermediaryObfuscationPattern: String? = null
    var strippedPackages: List<String> = emptyList()
    var stringDecryptionStrategy: StringDecryptionStategy = StringDecryptionStategy.None

    internal fun Project.initWorkspace() {
        // Create configuration
        configurations.create(Constants.DESKDOCK_CONFIGURATION_NAME)

        // Setup Enigma and extra dependencies
        val enigmaConfiguration = configurations.create(Constants.ENIGMA_CONFIGURATION_NAME)
        val enigmaDependenciesConfiguration = configurations.create(Constants.ENIGMA_DEPENDENCIES_CONFIGURATION_NAME)

        enigmaConfiguration.extendsFrom(enigmaDependenciesConfiguration)

        configurations.getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME).extendsFrom(
                enigmaConfiguration
        )

        // Create virtual maven repository for storing artifacts
        repository = getVirtualMavenRepository(Constants.DESKDOCK_MAVEN_REPOSITORY)
        repository.addRepository()
    }

    fun Project.initAfterEvaluate() {
        // Create dependency info for the main deskdock dependency
        initDeskDockDependencyInfo()

        // Initialize our workspace type (whether it is the server or the client)
        initWorkspaceType()

        // Create dependency info for the Engima UI
        initEnigmaDependencyInfo()
    }

    private fun Project.initEnigmaDependencyInfo() {
        enigmaDependencyInfo = DependencyInfoUtils.getProjectDependency(this, Constants.ENIGMA_CONFIGURATION_NAME)
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