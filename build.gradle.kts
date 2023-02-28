import io.github.nickacpt.reverseengineering.deskdock.plugin.model.WorkspaceType

plugins {
    id("desk-dock-reverse-engineering-plugin")
}

group = "io.github.nickacpt.reverseengineering.deskdock"

workspace {
    type = WorkspaceType.SERVER
    version = "1.3.0"
}