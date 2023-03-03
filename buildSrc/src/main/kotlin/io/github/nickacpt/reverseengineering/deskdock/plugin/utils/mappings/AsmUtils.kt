package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation.ClassNodesClassLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
object AsmUtils {

    fun viewJarAsNodes(path: Path, everythingPublic: Boolean = false): Map<String, ClassNode> {
        return FileSystems.newFileSystem(path).use { fs ->
            fs.getPath("/").walk().mapNotNull { path ->
                if (path.extension != "class") return@mapNotNull null

                path.toString() to classNodeFromPath(path, everythingPublic).second
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
                    val writer = object : ClassWriter(reader, COMPUTE_MAXS) {
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

    private fun classNodeFromPath(path: Path, everythingPublic: Boolean = false): Pair<ClassReader, ClassNode> {
        val reader = ClassReader(path.readBytes())
        val node = ClassNode()
        reader.accept(node, 0)

        if (everythingPublic) {
            node.access = makePublic(node.access)
            node.methods.forEach {
                it.access = makePublic(it.access)
            }
            node.fields.forEach {
                it.access = makePublic(it.access)
            }
        }

        return Pair(reader, node)
    }

    private fun makePublic(access: Int): Int {
        return access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).inv() or Opcodes.ACC_PUBLIC
    }
}