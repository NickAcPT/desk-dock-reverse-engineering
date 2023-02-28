package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.OriginalDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.ConfigurationConstants
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeskDockReverseEngineeringPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configurations.create(ConfigurationConstants.DESKDOCK_CONFIGURATION_NAME)

        target.afterEvaluate {
            OriginalDeskDockProvider.provide(this)
        }
    }
}