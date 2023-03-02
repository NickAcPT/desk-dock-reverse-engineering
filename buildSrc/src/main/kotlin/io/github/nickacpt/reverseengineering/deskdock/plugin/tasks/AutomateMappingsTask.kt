package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryDeskDockProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.AsmUtils
import org.benf.cfr.reader.apiunreleased.ClassFileSource2
import org.benf.cfr.reader.apiunreleased.JarContent
import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer
import org.benf.cfr.reader.bytecode.analysis.parse.Expression
import org.benf.cfr.reader.bytecode.analysis.parse.LValue
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.state.DCCommonState
import org.benf.cfr.reader.util.AnalysisType
import org.benf.cfr.reader.util.getopt.OptionsImpl
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassWriter

abstract class AutomateMappingsTask : DeskDockTaskBase() {
    @TaskAction
    fun automate() {
        val intermediaryJar = IntermediaryDeskDockProvider.provide(project)
        val nodes = AsmUtils.viewJarAsNodes(intermediaryJar)

        val cfrOptions = OptionsImpl(emptyMap())

        val source = object : ClassFileSource2 {
            override fun informAnalysisRelativePathDetail(usePath: String?, classFilePath: String?) {
            }

            override fun addJar(jarPath: String?): Collection<String> {
                return emptyList()
            }

            override fun getPossiblyRenamedPath(path: String): String = path

            override fun getClassFileContent(path: String): Pair<ByteArray, String> {
                val classNode = nodes["/${path}"]!!
                return Pair(ClassWriter(0).let { classNode.accept(it); it.toByteArray() }, path)
            }

            override fun addJarContent(jarPath: String?, analysisType: AnalysisType?): JarContent? {
                return null
            }

        }

        val commonState = DCCommonState(cfrOptions, source)
        val tree = commonState.getClassFileMaybePath("com/floriandraschbacher/deskdock/server/class_9")

        val analysis = tree.methods[10].analysis

        val rewriter = object : AbstractExpressionRewriter() {
            val stringType = commonState.classCache.getRefClassFor("java/lang/String")

            fun materializeExpression(expr: Expression): Any? {
                if (expr is Literal) {
                    return expr.value.value
                } else if (expr is NewAnonymousArray) {
                    return expr.values.map(this::materializeExpression).toTypedArray()
                }
                return null
            }


            override fun rewriteExpression(expression: Expression, ssaIdentifiers: SSAIdentifiers<*>?, statementContainer: StatementContainer<*>?, flags: ExpressionRewriterFlags?): Expression {
                val wildcard = WildcardMatch()
                val arrayExpression = wildcard.getExpressionWildCard("arrayValues")

                val stringDecryptorMethod = wildcard.getStaticFunction("decryptString", tree.classType, stringType, "method_26", arrayExpression)

                if (wildcard.match(stringDecryptorMethod, expression)) {
                    val localValue = wildcard.getLValueWildCard("localVar")

                    // We have a local variable from the lvar map
                    val matchedArrayExpr = arrayExpression.match
                    val actualExpression = if (wildcard.match(LValueExpression(localValue), matchedArrayExpr)) {
                        arrayAssignmentCache[localValue.match]
                                ?: return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags)
                    } else {
                        matchedArrayExpr
                    }

                    val materialized = materializeExpression(actualExpression)

                    println("Materialized")
                }

                return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags)
            }

            override fun rewriteExpression(lValue: LValue?, ssaIdentifiers: SSAIdentifiers<*>?, statementContainer: StatementContainer<*>?, flags: ExpressionRewriterFlags?): LValue {
                return super.rewriteExpression(lValue, ssaIdentifiers, statementContainer, flags)
            }

            val arrayAssignmentCache = mutableMapOf<LValue, AbstractNewArray>()

            override fun handleStatement(statementContainer: StatementContainer<*>) {
                val wildcard = WildcardMatch()
                val variable = wildcard.getLValueWildCard("variable")
                val array = wildcard.getNewArrayWildCard("array", 0, null)

                val assignment = StructuredAssignment(BytecodeLoc.NONE, variable, array)

                if (wildcard.match(assignment, statementContainer.statement)) {
                    arrayAssignmentCache[variable.match] = array.match
                }

                super.handleStatement(statementContainer)
            }

        }

        analysis.transform(object : ExpressionRewriterTransformer(rewriter) {}, StructuredScope())
    }

}