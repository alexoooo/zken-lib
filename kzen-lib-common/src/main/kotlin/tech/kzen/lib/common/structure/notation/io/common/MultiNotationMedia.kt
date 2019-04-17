package tech.kzen.lib.common.structure.notation.io.common

import tech.kzen.lib.common.model.document.DocumentPath
import tech.kzen.lib.common.model.document.DocumentPathMap
import tech.kzen.lib.common.structure.notation.io.NotationMedia
import tech.kzen.lib.common.util.Digest
import tech.kzen.lib.platform.collect.toPersistentMap


class MultiNotationMedia(
        private val media: List<NotationMedia>
) : NotationMedia {
    override suspend fun scan(): DocumentPathMap<Digest> {
        val all = mutableMapOf<DocumentPath, Digest>()
        for (delegate in media) {
            all.putAll(delegate.scan().values)
        }
        return DocumentPathMap(all.toPersistentMap())
    }


    override suspend fun read(location: DocumentPath): ByteArray {
        for (source in media) {
            try {
                return source.read(location)
            }
            catch (ignored: Exception) {
                println("MultiNotationMedia - Not found in $source - ${ignored.message}")
            }
        }

        throw IllegalArgumentException("Unable to read: $location")
    }


    override suspend fun write(location: DocumentPath, bytes: ByteArray) {
        for (medium in media) {
            try {
                return medium.write(location, bytes)
            }
            catch (ignored: Exception) {
                println("MultiNotationMedia - Can't write in $medium - ${ignored.message}")
            }
        }

        throw IllegalArgumentException("Unable to write: $location")
    }


    override suspend fun delete(location: DocumentPath) {
        for (medium in media) {
            try {
                return medium.delete(location)
            }
            catch (ignored: Exception) {
                println("MultiNotationMedia - Can't delete in $medium - ${ignored.message}")
            }
        }

        throw IllegalArgumentException("Unable to delete: $location")
    }
}