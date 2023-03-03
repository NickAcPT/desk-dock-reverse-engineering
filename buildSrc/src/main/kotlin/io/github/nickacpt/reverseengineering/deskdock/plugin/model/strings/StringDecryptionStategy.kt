package io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings

import io.github.nickacpt.reverseengineering.deskdock.plugin.model.strings.directinvocation.DirectInvocationStringDecryptor
import io.github.nickacpt.reverseengineering.deskdock.plugin.providers.IntermediaryMappingProvider
import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.mappings.MappingUtils
import org.gradle.api.Project
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

sealed class StringDecryptionStategy {
    abstract val decryptor: StringDecryptor<*>

    object None : StringDecryptionStategy(), StringDecryptor<None> {
        override val decryptor: StringDecryptor<*> get() = this

        override fun prepareClass(nodes: Map<String, ClassNode>, clazz: ClassNode, strategy: None) {
            // NO-OP
        }

        override fun decryptStrings(clazz: ClassNode, method: MethodNode, strategy: None): Boolean {
            // NO-OP
            return false
        }

        override fun prepareNodes(context: Map<String, ClassNode>, strategy: None) {
            // NO-OP
        }

    }

    data class DirectlyInvoke(val project: Project, val decryptorClass: String, val decryptorMethod: String, val decryptorDesc: String) : StringDecryptionStategy() {
        private val intermediaryNs = 0

        private val intermediaryMappings by lazy {
            MappingUtils.loadMappings(IntermediaryMappingProvider.provide(project))
        }

        private val mappingMethod by lazy {
            intermediaryMappings.getMethod(decryptorClass, decryptorMethod, decryptorDesc, intermediaryNs)
        }

        internal val realClassName by lazy {
            mappingMethod.owner.srcName
        }

        internal val realMethodName by lazy {
            mappingMethod.srcName
        }

        internal val realMethodDesc by lazy {
            mappingMethod.srcDesc
        }

        override val decryptor: StringDecryptor<*> = DirectInvocationStringDecryptor()
    }

    @Suppress("UNCHECKED_CAST")
    data class MultipleInvoke(val invocations: List<DirectlyInvoke>) : StringDecryptionStategy(), StringDecryptor<MultipleInvoke> {
        override val decryptor: StringDecryptor<*>
            get() = this

        override fun prepareNodes(context: Map<String, ClassNode>, strategy: MultipleInvoke) {
            invocations.forEach {
                (it.decryptor as StringDecryptor<StringDecryptionStategy>).prepareNodes(context, it)
            }
        }

        override fun prepareClass(nodes: Map<String, ClassNode>, clazz: ClassNode, strategy: MultipleInvoke) {
            invocations.forEach {
                (it.decryptor as StringDecryptor<StringDecryptionStategy>).prepareClass(nodes, clazz, it)
            }
        }

        override fun decryptStrings(clazz: ClassNode, method: MethodNode, strategy: MultipleInvoke): Boolean {
            return invocations.map {
                (it.decryptor as StringDecryptor<StringDecryptionStategy>).decryptStrings(clazz, method, it)
            }.any { it }
        }
    }

}
