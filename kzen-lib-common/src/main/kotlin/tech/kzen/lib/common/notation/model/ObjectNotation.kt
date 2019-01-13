package tech.kzen.lib.common.notation.model

import tech.kzen.lib.common.api.model.*


data class ObjectNotation(
        val attributes: Map<AttributeName, AttributeNotation>
) {
    //-----------------------------------------------------------------------------------------------------------------
    fun get(notationPath: AttributeNesting): AttributeNotation? {
//        if (parameters.containsKey(notationPath)) {
//            return parameters[notationPath]!!
//        }
//
//        val segments = notationPath.split(".")

//        println("attributes: $attributes - $notationPath")

        val firstSegment = notationPath.attribute
        if (! attributes.containsKey(firstSegment)) {
//            println("first segment missing ($firstSegment): $attributes - $notationPath")
            return null
        }
//        println("first segment: $firstSegment")

        val root = attributes[firstSegment]!!
        if (notationPath.segments.isEmpty()) {
            return root
        }

        var next: StructuredAttributeNotation = root
                as? StructuredAttributeNotation
                ?: return null

        for (segment in notationPath.segments.dropLast(1)) {
            println("get - next: $next - $segment")

            val sub = next.get(segment.asString())
                    as? StructuredAttributeNotation
                    ?: return null

            next = sub
        }

        val lastPathSegment = notationPath.segments.last()

        return next.get(lastPathSegment.asString())
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun upsertAttribute(
            notationPath: AttributeNesting,
            parameterNotation: AttributeNotation
    ): ObjectNotation {
        val rootParameterName = notationPath.attribute

        if (notationPath.segments.isEmpty()) {
            return upsertRootParameter(rootParameterName, parameterNotation)
        }

//        if (! parameters.containsKey(rootParameterName)) {
//            throw IllegalArgumentException("Parameter not found: $notationPath")
//        }

        val root: StructuredAttributeNotation =
                attributes[rootParameterName]
                as? StructuredAttributeNotation
                ?: MapAttributeNotation(mapOf())
//                ?: throw IllegalArgumentException(
//                        "Structured parameter expected (${notationPathSegments[0]}): $notationPath")

        val newRoot = upsertSubParameter(
                root,
                notationPath.segments.subList(1, notationPath.segments.size),
                parameterNotation)

        return upsertRootParameter(rootParameterName, newRoot)
    }


    private fun upsertRootParameter(
            parameterName: AttributeName,
            value: AttributeNotation
    ): ObjectNotation {
        var replaced = false

        val buffer = mutableMapOf<AttributeName, AttributeNotation>()
        for (parameter in attributes) {
            buffer[parameter.key] =
                    if (parameter.key == parameterName) {
                        replaced = true
                        value
                    }
                    else {
                        parameter.value
                    }
        }

        if (! replaced) {
            buffer[parameterName] = value
        }

        return ObjectNotation(buffer)
    }


    private fun upsertSubParameter(
            next: StructuredAttributeNotation,
            remainingPathSegments: List<AttributeSegment>,
            value: AttributeNotation
    ): StructuredAttributeNotation {
        val nextPathSegment = remainingPathSegments[0]

        val nextValue =
                if (remainingPathSegments.size == 1) {
                    value
                }
                else {
                    upsertSubParameter(
                            next.get(nextPathSegment.asString()) as StructuredAttributeNotation,
                            remainingPathSegments.subList(1, remainingPathSegments.size),
                            value)
                }

        return when (next) {
            is ListAttributeNotation -> {
                val index = nextPathSegment.asIndex()!!

                val buffer = mutableListOf<AttributeNotation>()
                buffer.addAll(next.values)

                buffer[index] = nextValue

                ListAttributeNotation(buffer)
            }

            is MapAttributeNotation -> {
                val buffer = mutableMapOf<AttributeSegment, AttributeNotation>()

                for (e in next.values) {
                    buffer[e.key] =
                            if (e.key == nextPathSegment) {
                                nextValue
                            }
                            else {
                                e.value
                            }
                }

                MapAttributeNotation(buffer)
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    override fun toString(): String {
        return attributes.toString()
    }
}
