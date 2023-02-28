package io.github.nickacpt.reverseengineering.deskdock.plugin.model

enum class WorkspaceType {
    SERVER {
        override fun getDownloadUrl(version: String): String {
            return "https://files.fdmobileinventions.com/DeskDockServer/${version}/DeskDockServer_${version}.zip"
        }
    };

    abstract fun getDownloadUrl(version: String): String

    override fun toString(): String {
        return name.lowercase()
    }
}