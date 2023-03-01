package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings

import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.tinyremapper.IMappingProvider

object TinyRemapperHelper {
    private fun memberOf(className: String, memberName: String, descriptor: String): IMappingProvider.Member {
        return IMappingProvider.Member(className, memberName, descriptor)
    }

    fun create(mappings: MappingTree, from: String?, to: String?, remapLocalVariables: Boolean): IMappingProvider {
        return IMappingProvider { acceptor: IMappingProvider.MappingAcceptor ->
            val fromId = mappings.getNamespaceId(from)
            val toId = mappings.getNamespaceId(to)
            for (classDef in mappings.classes) {
                val className = classDef.getName(fromId)
                var dstName = classDef.getName(toId)
                if (dstName == null) {
                    // Unsure if this is correct, it should be better than crashing tho.
                    dstName = className
                }
                acceptor.acceptClass(className, dstName)
                for (field in classDef.fields) {
                    acceptor.acceptField(
                        memberOf(className, field.getName(fromId), field.getDesc(fromId)),
                        field.getName(toId)
                    )
                }
                for (method in classDef.methods) {
                    val methodIdentifier = memberOf(className, method.getName(fromId), method.getDesc(fromId))
                    acceptor.acceptMethod(methodIdentifier, method.getName(toId))
                    if (remapLocalVariables) {
                        for (parameter in method.args) {
                            val name = parameter.getName(toId) ?: continue
                            acceptor.acceptMethodArg(methodIdentifier, parameter.lvIndex, name)
                        }
                        for (localVariable in method.vars) {
                            acceptor.acceptMethodVar(
                                methodIdentifier, localVariable.lvIndex,
                                localVariable.startOpIdx, localVariable.lvtRowIndex,
                                localVariable.getName(toId)
                            )
                        }
                    }
                }
            }
        }
    }
}