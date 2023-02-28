package io.github.nickacpt.reverseengineering.deskdock.plugin.utils.maven

import org.gradle.api.Project

fun Project.getVirtualMavenRepository(name: String) = MavenDependencyUtils.getVirtualMavenRepository(this, name)