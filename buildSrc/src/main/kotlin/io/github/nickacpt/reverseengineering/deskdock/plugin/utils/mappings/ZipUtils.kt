package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

object ZipUtils {
    @OptIn(ExperimentalPathApi::class)
    fun stripPackage(path: Path, pkg: String) {
        FileSystems.newFileSystem(path).use {
            it.getPath("/", pkg).deleteRecursively()
        }
    }
}