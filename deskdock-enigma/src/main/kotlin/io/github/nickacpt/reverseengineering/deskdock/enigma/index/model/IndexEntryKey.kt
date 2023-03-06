package io.github.nickacpt.reverseengineering.deskdock.enigma.index.model

sealed interface IndexEntryKey {
    val name: String

    data class ClassIndexEntry(override val name: String) : IndexEntryKey
    data class MethodIndexEntry(val owner: ClassIndexEntry, override val name: String, val desc: String) : IndexEntryKey {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MethodIndexEntry

            if (name != other.name) return false
            return desc == other.desc
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + desc.hashCode()
            return result
        }
    }

    data class FieldIndexEntry(val owner: ClassIndexEntry, override val name: String, val desc: String) : IndexEntryKey {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FieldIndexEntry

            if (name != other.name) return false
            return desc == other.desc
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + desc.hashCode()
            return result
        }
    }
    data class MethodParameterIndexEntry(val method: MethodIndexEntry, val index: Int, override val name: String) : IndexEntryKey {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MethodParameterIndexEntry

            if (method != other.method) return false
            return index == other.index
        }

        override fun hashCode(): Int {
            var result = method.hashCode()
            result = 31 * result + index
            return result
        }
    }
}