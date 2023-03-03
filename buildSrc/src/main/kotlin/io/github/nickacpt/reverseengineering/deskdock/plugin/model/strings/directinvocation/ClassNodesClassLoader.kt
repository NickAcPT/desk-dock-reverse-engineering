package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.asm.AsmMaterializationUtils
import org.objectweb.asm.tree.ClassNode
import java.net.URLClassLoader

data class ClassNodesClassLoader(val nodes: Map<String, ClassNode>) : URLClassLoader(emptyArray(), getSystemClassLoader()) {

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return nodes["/$name.class"]?.let { defineNode(it) } ?: super.loadClass(name, resolve)
    }

    fun defineNode(node: ClassNode): Class<*> {
        val name = node.name.replace('/', '.')

        return defineClass(name, AsmMaterializationUtils.toBytes(node), 0, AsmMaterializationUtils.toBytes(node).size)
    }

}