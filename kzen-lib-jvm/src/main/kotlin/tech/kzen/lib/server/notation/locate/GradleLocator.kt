package tech.kzen.lib.server.notation.locate

import tech.kzen.lib.common.api.model.BundlePath
import tech.kzen.lib.common.structure.notation.NotationConventions
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors


class GradleLocator(
        private var includeTest: Boolean = false
): FileNotationLocator {
    //-----------------------------------------------------------------------------------------------------------------
    companion object {
        private const val mainResources = "/src/main/resources/" + NotationConventions.prefix
        private const val testResources = "/src/test/resources/" + NotationConventions.prefix
    }


    //-----------------------------------------------------------------------------------------------------------------
    private val moduleRoot: String by lazy { moduleRootImpl() }

    private val scanRoots: List<Path> by lazy { scanRootsImpl() }


    //-----------------------------------------------------------------------------------------------------------------
    override fun scanRoots(): List<Path> {
        return scanRoots
    }


    //-----------------------------------------------------------------------------------------------------------------
    override fun locateExisting(location: BundlePath): Path? {
        for (root in scanRoots) {
            val candidate = root.resolve(location.asRelativeFile())

//            println("GradleLocator - candidate: ${candidate.toAbsolutePath()}")

            if (Files.exists(candidate)) {
                return candidate
            }
        }

        return null
    }


    //-----------------------------------------------------------------------------------------------------------------
    override fun resolveNew(location: BundlePath): Path? {
        return Paths.get("${mainResources()}/${location.asRelativeFile()}")
                .normalize()
                .toAbsolutePath()
    }


    //-----------------------------------------------------------------------------------------------------------------
    private fun mainResources(): String {
        return "$moduleRoot$mainResources"
    }


    private fun scanRootsImpl(): List<Path> {
        val buffer = mutableListOf<String>()

        buffer.add(mainResources())

        if (includeTest) {
            buffer.add("$moduleRoot$testResources")
        }

        return buffer.map { Paths.get(it).normalize().toAbsolutePath() }
    }


    private fun moduleRootImpl(): String =
        if (Files.exists(Paths.get("src"))) {
            "."
        }
        else {
            Files.list(Paths.get(".")).use { files ->
                val list = files.collect(Collectors.toList())


                val jvmModule = list.firstOrNull { it.fileName.toString().endsWith("-jvm")}

                // TODO: expose static/
                jvmModule?.toString()
                        ?: "."
            }
        }
}