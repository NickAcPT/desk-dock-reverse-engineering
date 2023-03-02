package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

abstract class EnigmaCommandTask : EnigmaTask() {
    init {
        mainClass.set("cuchaz.enigma.command.Main")
    }
}