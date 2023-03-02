package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.StringDecryptionStategy
import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc
import org.benf.cfr.reader.bytecode.analysis.parse.Expression
import org.benf.cfr.reader.bytecode.analysis.parse.LValue
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer
import org.benf.cfr.reader.bytecode.analysis.parse.expression.*
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance
import org.benf.cfr.reader.entities.ClassFile
import org.benf.cfr.reader.state.DCCommonState

data class DirectInvocationStringDecryptorInputArrayVisitor(val strategy: StringDecryptionStategy.DirectlyInvoke, val decryptorTree: ClassFile, val state: DCCommonState): AbstractExpressionRewriter() {
    private val stringType: JavaRefTypeInstance = state.classCache.getRefClassFor("java/lang/String")
    val materializedInputs = mutableListOf<Any?>()
    private val arrayAssignmentCache = mutableMapOf<LValue, AbstractNewArray>()

    private fun materializeExpression(expr: Expression): Any? {
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
        val stringDecryptorWithArgsMethod = wildcard.getStaticFunction("stringDecryptorWithArgs", decryptorTree.classType, stringType, strategy.realMethodName, arrayExpression)
        val stringDecryptorWithoutArgsMethod = wildcard.getStaticFunction("stringDecryptorWithoutArgsMethod", decryptorTree.classType, stringType, strategy.realMethodName)

        if (wildcard.match(stringDecryptorWithArgsMethod, expression)) {
            val localValue = wildcard.getLValueWildCard("localVar")

            // We have a local variable from the lvar map
            val actualExpression = if (wildcard.match(LValueExpression(localValue), simplifyExpression(arrayExpression.match))) {
                arrayAssignmentCache[localValue.match]
                        ?: return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags)
            } else {
                simplifyExpression(arrayExpression.match)
            }

            materializedInputs.add(materializeExpression(actualExpression))
        } else if (wildcard.match(stringDecryptorWithoutArgsMethod, expression)) {
            materializedInputs.add(emptyArray<Int>())
        }

        return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags)
    }

    private fun simplifyExpression(expr: Expression): Expression {
        return when (expr) {
            is CastExpression -> {
                simplifyExpression(expr.child)
            }

            else -> expr
        }
    }

    override fun handleStatement(statementContainer: StatementContainer<*>) {
        val wildcard = WildcardMatch()
        val variable = wildcard.getLValueWildCard("variable")
        val array = wildcard.getNewArrayWildCard("array", 0, null)

        val assignment = StructuredAssignment(BytecodeLoc.NONE, variable, array)

        if (wildcard.match(assignment, statementContainer.statement)) {
            arrayAssignmentCache[variable.match] = array.match
        }
    }
}