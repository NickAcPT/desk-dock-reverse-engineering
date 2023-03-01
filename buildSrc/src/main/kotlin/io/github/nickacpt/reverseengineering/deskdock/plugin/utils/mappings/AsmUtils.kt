package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

object AsmUtils {
    @OptIn(ExperimentalPathApi::class)
    fun updateJarClasses(path: Path, updater: (ClassNode) -> Boolean) {
        FileSystems.newFileSystem(path).use { fs ->
            fs.getPath("/").walk().forEach { path ->
                if (path.extension != "class") return@forEach

                val reader = ClassReader(path.readBytes())
                val node = ClassNode()
                reader.accept(node, 0)

                if (updater(node)) {
                    val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                    node.accept(writer)

                    path.writeBytes(writer.toByteArray())
                }
            }
        }
    }
}