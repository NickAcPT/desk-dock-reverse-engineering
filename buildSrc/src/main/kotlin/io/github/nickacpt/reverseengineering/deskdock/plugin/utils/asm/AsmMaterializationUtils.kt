package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.asm

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation.ClassNodesClassLoader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

object AsmMaterializationUtils {

    fun toBytes(node: ClassNode): ByteArray =
        ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()

    fun materializeMethodNode(owner: ClassNode, method: MethodNode, context: Map<String, ClassNode>): MethodHandle {
        val classLoader = ClassNodesClassLoader(context)

        // Create class node with the methods
        val cn = ClassNode()
        cn.version = Opcodes.V1_8
        cn.access = Opcodes.ACC_PUBLIC
        cn.superName = "java/lang/Object"
        cn.name = "Generated${System.currentTimeMillis()}"

        val invokerNode = MethodNode().also(cn.methods::add).apply {
            access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
            name = method.name
            desc = method.desc
        }

        val invoker = GeneratorAdapter(invokerNode, Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, method.name, method.desc)

        invoker.loadArgs()
        invoker.invokeStatic(Type.getObjectType(owner.name), Method(method.name, method.desc))
        invoker.returnValue()

        val clazz = classLoader.defineNode(cn)
        val materializedMethod =
            clazz.methods.first { it.name == method.name && Type.getMethodDescriptor(it) == method.desc }

        return MethodHandles.lookup().unreflect(materializedMethod)
    }
}