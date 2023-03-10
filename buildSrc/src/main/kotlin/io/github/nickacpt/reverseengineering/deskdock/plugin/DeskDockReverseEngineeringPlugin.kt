package io.github.nickacpt.reverseengineering.deskdock.plugin

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.DeskDockWorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.NamedDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.mappingsPath
import io.github.nickacpt.reverseengineering.deskdock.plugin.tasks.AutomateMappingsTask
import io.github.nickacpt.reverseengineering.deskdock.plugin.tasks.DumpNamesFromIntermediaryMappingsTask
import io.github.nickacpt.reverseengineering.deskdock.plugin.tasks.InsertProposedMappingsTask
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

        val enigmaTasks = listOf(
            target.tasks.create<LaunchEnigmaTask>("launchEnigma"),
            target.tasks.create<InsertProposedMappingsTask>("insertProposedMappings")
        )
        target.tasks.create<DumpNamesFromIntermediaryMappingsTask>("dumpIntermediaryNames")
        target.tasks.create<AutomateMappingsTask>("automateMappings")

        target.afterEvaluate {
            with(extension) {
                target.initAfterEvaluate()
            }

            // First, provide the original deskdock jar
            val intermediaryJar = IntermediaryDeskDockProvider.provide(this)

            enigmaTasks.forEach {
                it.workDirPath = project.mappingsPath
                it.inputJarPath = intermediaryJar
                it.mappingsPath = target.workspace.type.getMappingsDirectory(target)
                it.initClassPath()
            }

            // Now, provide the named deskdock jar
            NamedDeskDockProvider.provide(this)

            println(project.configurations.getByName("deskDockProvided").resolve())
        }
    }
}