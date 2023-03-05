package io.github.nickacpt.reverseengineering.deskdock.enigma.utils.cfr

import org.benf.cfr.reader.apiunreleased.ClassFileSource2
import org.benf.cfr.reader.apiunreleased.JarContent
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair
import org.benf.cfr.reader.util.AnalysisType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

data class ClassNodeViewCfrClassSource(val nodes: Map<String, ClassNode>) : ClassFileSource2 {
    override fun informAnalysisRelativePathDetail(usePath: String?, classFilePath: String?) {
    }

    override fun addJar(jarPath: String?): Collection<String> {
        return emptyList()
    }

    override fun getPossiblyRenamedPath(path: String): String = path

    override fun getClassFileContent(path: String): Pair<ByteArray, String>? {
        val clazz = nodes[path.removeSuffix(".class")] ?: return null

        return Pair(ClassWriter(0).let { clazz.accept(it); it.toByteArray() }, path)
    }

    override fun addJarContent(jarPath: String?, analysisType: AnalysisType?): JarContent? {
        return null
    }

}