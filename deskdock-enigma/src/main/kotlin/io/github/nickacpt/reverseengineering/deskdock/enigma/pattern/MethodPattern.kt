package io.github.nickacpt.reverseengineering.deskdock.enigma.pattern

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

data class ParameterPattern(
    val name: String?,
    val type: String?,
)

data class ParentClassPattern(
    val name: String?,
    val access: List<String>?,
    val `super`: String?
) {
    fun matches(clazz: ClassNode): Boolean {
        if (name != null && clazz.name != name) return false
        if (access != null && !checkAccessFlags(clazz.access, access)) return false
        if (`super` != null && clazz.superName != `super`) return false
        return true
    }
}

data class MethodPattern(
    val name: String?,
    val access: List<String>?,
    val `return`: String?,
    val parameters: List<ParameterPattern?>?,
    val parent: ParentClassPattern?,
) {
    fun matches(parent: ClassNode, method: MethodNode): Boolean {
        if (this.parent != null && !this.parent.matches(parent)) return false
        if (access != null && !checkAccessFlags(method.access, access)) return false
        if (`return` != null && Type.getReturnType(method.desc).internalName != `return`) return false
        if (parameters != null) {
            val params = Type.getArgumentTypes(method.desc)
            if (params.size != parameters.size) return false
            for (i in params.indices) {
                if (parameters[i] != null && params[i].internalName != parameters[i]?.type) return false
            }
        }
        return true
    }
}


private fun checkAccessFlags(accessFlags: Int, access: List<String>): Boolean {
    val flagsList = arrayOf(
        "public" to Opcodes.ACC_PUBLIC,
        "private" to Opcodes.ACC_PRIVATE,
        "protected" to Opcodes.ACC_PROTECTED,
        "static" to Opcodes.ACC_STATIC,
        "final" to Opcodes.ACC_FINAL,
        "synchronized" to Opcodes.ACC_SYNCHRONIZED,
        "bridge" to Opcodes.ACC_BRIDGE,
        "varargs" to Opcodes.ACC_VARARGS,
        "native" to Opcodes.ACC_NATIVE,
        "abstract" to Opcodes.ACC_ABSTRACT,
        "strict" to Opcodes.ACC_STRICT,
        "synthetic" to Opcodes.ACC_SYNTHETIC,
    )

    for ((name, asmFlag) in flagsList) {
        if (accessFlags and asmFlag == 0 && access.contains(name)) return false
    }

    return true
}