package com.vektorsoft.xapps.kickstart.model

open class JvmDependency(fileName: String,
						 hash: String,
						 size: Long,
						 val dependencyScope: JvmDependencyScope) : BinaryData(fileName, hash, size) {
}