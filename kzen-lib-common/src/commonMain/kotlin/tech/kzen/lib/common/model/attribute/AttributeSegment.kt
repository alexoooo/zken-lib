package tech.kzen.lib.common.model.attribute

import tech.kzen.lib.common.util.Digest
import tech.kzen.lib.common.util.Digestible


@Suppress("DataClassPrivateConstructor")
data class AttributeSegment private constructor(
    private val asKey: String
):
    Digestible
{
    //-----------------------------------------------------------------------------------------------------------------
    companion object {
        fun parse(asString: String): AttributeSegment {
            return ofKey(AttributePath.decodeDelimiter(asString))
        }


        fun ofKey(key: String): AttributeSegment {
            return AttributeSegment(key)
        }


        fun ofIndex(index: Int): AttributeSegment {
            check(index >= 0) { "Must not be negative: $index" }
            return AttributeSegment(index.toString())
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun asKey(): String {
        return asKey
    }


    fun asIndex(): Int? {
        return asKey.toIntOrNull()
    }


    //-----------------------------------------------------------------------------------------------------------------
    override fun digest(builder: Digest.Builder) {
        builder.addUtf8(asKey)
    }


    fun asString(): String {
        return AttributePath.encodeDelimiter(asKey)
    }


    override fun toString(): String {
        return asString()
    }
}