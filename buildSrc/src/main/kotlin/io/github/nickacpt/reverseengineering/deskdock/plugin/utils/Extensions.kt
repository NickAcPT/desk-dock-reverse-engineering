package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.DeskDockWorkspaceExtension
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.dependency.DependencyInfoUtils
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path

val Project.workspace get() = extensions.getByType<DeskDockWorkspaceExtension>()

fun Project.getDependencyInfo(configuration: String) = DependencyInfoUtils.getProjectDependency(this, configuration)

fun Project.getCacheFilePath(vararg names: String): Path = CachedFilesUtils.getCacheFilePath(project, *names)

fun Project.fetchOrGetCachedFile(vararg names: String, fetcher: (Path) -> Unit): Path =
    CachedFilesUtils.fetchOrGetCachedFile(project, *names, fileFetcher = fetcher)