package io.github.nickacpt.reverseengineering.deskdock.plugin.tasks

import io.github.nickacpt.reverseengineering.deskdock.plugin.utils.constants.Constants
import org.gradle.api.DefaultTask

abstract class DeskDockTaskBase : DefaultTask() {
    init {
        group = Constants.DESKDOCK_TASK_GROUP
    }
}