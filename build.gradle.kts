plugins {
    kotlin("multiplatform") version kotlinVersion apply false
}


subprojects {
    group = "tech.kzen.lib"
    version = "0.13.2"

    repositories {
        mavenLocal()
        jcenter()
    }
}