package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

interface StringDecryptor<in S: StringDecryptionStategy> {
    fun prepareNodes(context: Map<String, ClassNode>, strategy: S)
    fun prepareClass(nodes: Map<String, ClassNode>, clazz: ClassNode, strategy: S)
    fun decryptStrings(clazz: ClassNode, method: MethodNode, strategy: S): Boolean
}