package io.github.nickacpt.reverseengineering.deskdock.enigma.index.model

sealed interface IndexEntryKey {
    val name: String

    data class ClassIndexEntry(override val name: String) : IndexEntryKey
    data class MethodIndexEntry(val owner: ClassIndexEntry, override val name: String, val desc: String) : IndexEntryKey
    data class FieldIndexEntry(val owner: ClassIndexEntry, override val name: String, val desc: String) : IndexEntryKey
}