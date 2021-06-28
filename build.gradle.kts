// https://youtrack.jetbrains.com/issue/KT-46006

plugins {
    kotlin("multiplatform") version kotlinVersion apply false
}


subprojects {
    group = "tech.kzen.lib"
    version = "0.23.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}
