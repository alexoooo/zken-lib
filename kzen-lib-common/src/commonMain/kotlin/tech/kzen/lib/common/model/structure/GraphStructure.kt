package tech.kzen.lib.common.model.structure

import tech.kzen.lib.common.model.document.DocumentNesting
import tech.kzen.lib.common.model.structure.metadata.GraphMetadata
import tech.kzen.lib.common.model.structure.notation.GraphNotation
import tech.kzen.lib.common.util.Digest
import tech.kzen.lib.common.util.Digestible


data class GraphStructure(
    val graphNotation: GraphNotation,
    val graphMetadata: GraphMetadata
):
    Digestible
{
    //-----------------------------------------------------------------------------------------------------------------
    companion object {
        val empty = GraphStructure(
                GraphNotation.empty,
                GraphMetadata.empty)
    }


    //-----------------------------------------------------------------------------------------------------------------
    private var digest: Digest? = null


    //-----------------------------------------------------------------------------------------------------------------
    fun filter(allowed: Set<DocumentNesting>): GraphStructure {
        return GraphStructure(
                graphNotation.filter(allowed),
                graphMetadata.filter(allowed))
    }


    //-----------------------------------------------------------------------------------------------------------------
    override fun digest(builder: Digest.Builder) {
        builder.addDigest(digest())
    }


    override fun digest(): Digest {
        if (digest == null) {
            val builder = Digest.Builder()

            builder.addDigestible(graphNotation)
            builder.addDigestible(graphMetadata)

            digest = builder.digest()
        }
        return digest!!
    }
}