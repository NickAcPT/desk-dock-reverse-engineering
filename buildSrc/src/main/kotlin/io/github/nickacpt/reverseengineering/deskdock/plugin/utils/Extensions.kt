package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.nio.file.Path

val Project.workspace: WorkspaceExtension get() = extensions.getByType()

fun Project.getCacheFile(vararg names: String): Path = CachedFilesUtils.getCacheFilePath(project, *names)

fun Project.fetchOrGetCachedFile(vararg names: String, fetcher: (Path) -> Unit): Path =
    CachedFilesUtils.fetchOrGetCachedFile(project, *names, fileFetcher = fetcher)