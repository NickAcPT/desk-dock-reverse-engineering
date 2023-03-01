package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.DeskDockWorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.tasks.LaunchEnigmaTask
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.workspace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

class DeskDockReverseEngineeringPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Apply Java plugin
        target.apply(plugin = "java")

        target.repositories {
            mavenCentral()
            maven("https://maven.fabricmc.net/")
        }

        // Initialize our workspace
        val extension = target.extensions.create<DeskDockWorkspaceExtension>(Constants.DESKDOCK_EXTENSION_NAME)
        with(extension) {
            target.initWorkspace()
        }

        val engimaTask = target.tasks.create<LaunchEnigmaTask>("launchEnigma")

        target.afterEvaluate {
            with(extension) {
                target.initAfterEvaluate()
            }

            // First, provide the original deskdock jar
            val intermediaryJar = IntermediaryDeskDockProvider.provide(this)

            engimaTask.apply {
                inputJarPath = intermediaryJar
                mappingsPath = target.workspace.type.getMappingsDirectory(target)
                initClassPath()
            }
        }
    }
}