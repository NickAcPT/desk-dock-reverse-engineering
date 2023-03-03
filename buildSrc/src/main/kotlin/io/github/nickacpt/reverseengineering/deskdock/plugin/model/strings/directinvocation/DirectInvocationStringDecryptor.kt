package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy.DirectlyInvoke
import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptor
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.asm.AsmMaterializationUtils
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.state.DCCommonState
import org.benf.cfr.reader.util.getopt.OptionsImpl
import org.codehaus.janino.ExpressionEvaluator
import org.objectweb.asm.Opcodes.POP
import org.objectweb.asm.tree.*
import java.lang.invoke.MethodHandle

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

        val decryptorClassNode = context["/${strategy.realClassName}.class"]
                ?: throw Exception("Invalid decryptor class")
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

        val evaluator = ExpressionEvaluator()
        val expressions = visitor.matchedExpressions.map { it?.toString() ?: "new int[0]" }.toTypedArray()

        evaluator.cook(expressions)

        val materializedOutputs = expressions.mapIndexed { i, _ ->
            decryptorMethodHandle.invoke(evaluator.evaluate(i))
        }

        val replacements = methodCalls.mapIndexed { i, insn ->
            val output = materializedOutputs[i] as? String ?: return@mapIndexed false

            // Pop the decrypt method call result
            // And lastly insert a Load Constant instruction with this output
            val pop = InsnNode(POP)
            val ldc = LdcInsnNode(output)
            method.instructions.insert(insn, pop)
            method.instructions.insert(pop, ldc)

            return@mapIndexed true
        }

        return replacements.any { it }
    }
}
