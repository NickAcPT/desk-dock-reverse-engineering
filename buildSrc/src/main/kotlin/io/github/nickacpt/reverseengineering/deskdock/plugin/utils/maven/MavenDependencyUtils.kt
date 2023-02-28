package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven

import org.gradle.api.Project

object MavenDependencyUtils {

    fun getVirtualMavenRepository(project: Project, name: String): VirtualMavenRepository {
        val repo = VirtualMavenRepository(project, name)
        repo.addRepository()

        return repo
    }

}