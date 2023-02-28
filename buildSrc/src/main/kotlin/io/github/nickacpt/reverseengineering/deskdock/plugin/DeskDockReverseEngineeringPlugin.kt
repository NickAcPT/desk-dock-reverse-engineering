package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.OriginalDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.ConfigurationConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.MavenConstants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven.getVirtualMavenRepository
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class DeskDockReverseEngineeringPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Apply Java plugin
        target.apply(plugin = "java")

        // Create our configurations
        target.configurations.create(ConfigurationConstants.DESKDOCK_CONFIGURATION_NAME)

        // Create our repositories
        target.getVirtualMavenRepository(MavenConstants.DESKDOCK_MAVEN_REPOSITORY).addRepository()

        target.afterEvaluate {
            OriginalDeskDockProvider.provide(this)
        }
    }
}