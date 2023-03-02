package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation.ClassNodesClassLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
object AsmUtils {

    fun viewJarAsNodes(path: Path): Map<String, ClassNode> {
        return FileSystems.newFileSystem(path).use { fs ->
            fs.getPath("/").walk().mapNotNull { path ->
                if (path.extension != "class") return@mapNotNull null

                path.toString() to classNodeFromPath(path).second
            }.toMap()
        }
    }

    fun updateJarClasses(path: Path, updater: (ClassNode) -> Boolean) {
        val writerClassLoader = ClassNodesClassLoader(viewJarAsNodes(path))

        FileSystems.newFileSystem(path).use { fs ->
            fs.getPath("/").walk().forEach { path ->
                if (path.extension != "class") return@forEach

                val (reader, node) = classNodeFromPath(path)

                if (updater(node)) {
                    val writer = object : ClassWriter(reader, COMPUTE_MAXS or COMPUTE_FRAMES) {
                        override fun getClassLoader(): ClassLoader {
                            return writerClassLoader
                        }
                    }
                    node.accept(writer)

                    path.writeBytes(writer.toByteArray())
                }
            }
        }
    }

    private fun classNodeFromPath(path: Path): Pair<ClassReader, ClassNode> {
        val reader = ClassReader(path.readBytes())
        val node = ClassNode()
        reader.accept(node, 0)
        return Pair(reader, node)
    }
}