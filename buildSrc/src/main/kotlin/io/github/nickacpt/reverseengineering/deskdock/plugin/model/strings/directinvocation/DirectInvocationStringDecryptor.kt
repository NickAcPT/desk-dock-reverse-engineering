package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy.DirectlyInvoke
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptor
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation.AsmMaterializationUtils.toBytes
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.state.DCCommonState
import org.benf.cfr.reader.util.getopt.OptionsImpl
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.net.URLClassLoader

class DirectInvocationStringDecryptor : StringDecryptor<DirectlyInvoke> {

    private val cfrOptions = OptionsImpl(emptyMap())
    private var cfrState: DCCommonState? = null
    private var cfrTree: ClassFile? = null
    private var decryptorTree: ClassFile? = null
    private var decryptorMethodHandle: MethodHandle? = null

    override fun prepareNodes(context: Map<String, ClassNode>, strategy: DirectlyInvoke) {
        val cfrSource = ClassNodeViewCfrClassSource(context)
        cfrState = DCCommonState(cfrOptions, cfrSource)

        val state = cfrState!!
        decryptorTree = state.getClassFileMaybePath(strategy.realClassName)

        val decryptorClassNode = context["/${strategy.realClassName}.class"] ?: throw Exception("Invalid decryptor class")
        val decryptorMethodNode = decryptorClassNode.methods.firstOrNull { it.name == strategy.realMethodName && it.desc == strategy.realMethodDesc }
                ?: throw Exception("Invalid decryptor method")

        decryptorMethodHandle = AsmMaterializationUtils.materializeMethodNode(decryptorClassNode, decryptorMethodNode, context)
    }

    override fun prepareClass(nodes: Map<String, ClassNode>, clazz: ClassNode, strategy: DirectlyInvoke) {
        cfrTree = cfrState!!.getClassFileMaybePath(clazz.name)
    }

    override fun decryptStrings(clazz: ClassNode, method: MethodNode, strategy: DirectlyInvoke): Boolean {
        val decryptorMethodHandle = decryptorMethodHandle ?: return false
        val tree = cfrTree ?: return false
        val state = cfrState ?: return false
        val decryptorTree = decryptorTree ?: return false

        val cfrMethod = tree.methods.firstOrNull { it.name == method.name && it.methodPrototype.originalDescriptor == method.desc }
                ?: throw Exception("Missing method?! $method")

        val analysis = runCatching { cfrMethod.analysis }.getOrNull() ?: return false

        val visitor = DirectInvocationStringDecryptorInputArrayVisitor(strategy, decryptorTree, state)

        analysis.transform(object : ExpressionRewriterTransformer(visitor) {}, StructuredScope())

        val methodCalls = method.instructions.filter { it is MethodInsnNode && it.owner == strategy.realClassName && it.name == strategy.realMethodName && it.desc == strategy.realMethodDesc }
        if (methodCalls.isEmpty()) return false

        val materializedOutputs = visitor.materializedInputs.map { input ->
            decryptorMethodHandle.invoke((input as Array<*>).map { it as Int }.toIntArray())
        }

        val replacements = methodCalls.mapIndexed { i, insn ->
            val output = materializedOutputs[i] as? String ?: return@mapIndexed false
            println("Decrypted string for method ${method.name + method.desc} at ${clazz.name} - [$i] = $output")
            val previous = insn.previous

            // First, insert a Load Constant instruction with this output
            method.instructions.insertBefore(insn, LdcInsnNode(output))

            // Then we remove the method call
            method.instructions.remove(insn)

            // And if we're doing a load on a local variable to load an array, pop the loaded variable
            if (previous is VarInsnNode) {
                method.instructions.insert(previous, InsnNode(POP))
            }

            return@mapIndexed true
        }

        return replacements.any { it }
    }
}

data class ClassNodesClassLoader(val nodes: Map<String, ClassNode>) : URLClassLoader(emptyArray(), getSystemClassLoader()) {

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return nodes["/$name.class"]?.let { defineNode(it) } ?: super.loadClass(name, resolve)
    }

    fun defineNode(node: ClassNode): Class<*> {
        val name = node.name.replace('/', '.')

        return defineClass(name, toBytes(node), 0, toBytes(node).size)
    }

}

object AsmMaterializationUtils {

    fun toBytes(node: ClassNode): ByteArray = ClassWriter(ClassWriter.COMPUTE_MAXS).also { node.accept(it) }.toByteArray()

    fun materializeMethodNode(owner: ClassNode, method: MethodNode, context: Map<String, ClassNode>): MethodHandle {
        val classLoader = ClassNodesClassLoader(context)

        // Create class node with the methods
        val cn = ClassNode()
        cn.version = V1_8
        cn.access = ACC_PUBLIC
        cn.superName = "java/lang/Object"
        cn.name = "Generated${System.currentTimeMillis()}"

        val invokerNode = MethodNode().also(cn.methods::add).apply {
            access = ACC_PUBLIC or ACC_STATIC
            name = method.name
            desc = method.desc
        }

        val invoker = GeneratorAdapter(invokerNode, ACC_PUBLIC or ACC_STATIC, method.name, method.desc)

        invoker.loadArgs()
        invoker.invokeStatic(Type.getObjectType(owner.name), Method(method.name, method.desc))
        invoker.returnValue()

        val clazz = classLoader.defineNode(cn)
        val materializedMethod = clazz.methods.first { it.name == method.name && Type.getMethodDescriptor(it) == method.desc }

        return MethodHandles.lookup().unreflect(materializedMethod)
    }
}