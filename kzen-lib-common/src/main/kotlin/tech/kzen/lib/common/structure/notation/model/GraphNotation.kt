package tech.kzen.lib.common.structure.notation.model

import tech.kzen.lib.common.model.attribute.AttributePath
import tech.kzen.lib.common.model.document.DocumentPath
import tech.kzen.lib.common.model.document.DocumentPathMap
import tech.kzen.lib.common.model.locate.AttributeLocation
import tech.kzen.lib.common.model.locate.ObjectLocation
import tech.kzen.lib.common.model.locate.ObjectLocationMap
import tech.kzen.lib.common.model.locate.ObjectReference
import tech.kzen.lib.common.objects.bootstrap.BootstrapConventions
import tech.kzen.lib.common.structure.notation.NotationConventions


data class GraphNotation(
        val documents: DocumentPathMap<DocumentNotation>)
{
    //-----------------------------------------------------------------------------------------------------------------
    companion object {
        val empty = GraphNotation(DocumentPathMap(mapOf()))
    }


    //-----------------------------------------------------------------------------------------------------------------
    val objectLocations: Set<ObjectLocation> by lazy {
        coalesce.values.keys
    }

    val coalesce: ObjectLocationMap<ObjectNotation> by lazy {
        val buffer = mutableMapOf<ObjectLocation, ObjectNotation>()
        documents.values.entries
                .flatMap { it.value.expand(it.key).values.entries }
                .forEach { buffer[it.key] = it.value }
        ObjectLocationMap(buffer)
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun inheritanceChain(objectLocation: ObjectLocation): List<ObjectLocation> {
        val builder = mutableListOf<ObjectLocation>()
        inheritanceChain(objectLocation) {
            builder.add(it)
        }
        return builder
    }


    fun inheritanceChain(
            objectLocation: ObjectLocation,
            consumer: (ObjectLocation) -> Unit
    ) {
        var cursor = objectLocation
        while (true) {
            val notation = coalesce.values[cursor]
                    ?: throw IllegalStateException("Missing: $cursor")

            consumer.invoke(cursor)

            if (cursor == BootstrapConventions.rootObjectLocation ||
                    cursor == BootstrapConventions.bootstrapObjectLocation) {
                break
            }

            val isAttribute = notation.get(NotationConventions.isAttributePath)

            val superReference =
                    when (isAttribute) {
                        null ->
                            BootstrapConventions.rootObjectReference

                        !is ScalarAttributeNotation ->
                            TODO()

                        else -> {
                            val isValue = isAttribute.value
                            ObjectReference.parse(isValue)
                        }
                    }

            cursor = coalesce.locate(objectLocation, superReference)
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun directAttribute(
            objectLocation: ObjectLocation,
            attributePath: AttributePath
    ): AttributeNotation? =
            coalesce.values[objectLocation]?.get(attributePath)


    fun transitiveAttribute(
            objectLocation: ObjectLocation,
            attributePath: AttributePath
    ): AttributeNotation? {
        val notation = coalesce.values[objectLocation]
                ?: return null

        val attributeNotation = notation.get(attributePath)
        if (attributeNotation != null) {
            return attributeNotation
        }

        val isAttribute = notation.get(NotationConventions.isAttributePath)

        val superReference =
                when (isAttribute) {
                    null ->
                        BootstrapConventions.rootObjectReference

                    !is ScalarAttributeNotation ->
                        TODO()

                    else -> {
                        val isValue = isAttribute.value
                        ObjectReference.parse(isValue)
                    }
                }

//        println("coalesce keys ($objectLocation - $attributePath - $superReference): " + coalesce.values.keys)
        val superLocation = coalesce.locate(objectLocation, superReference)

        if (objectLocation == BootstrapConventions.rootObjectLocation ||
                objectLocation == BootstrapConventions.bootstrapObjectLocation) {
            return null
        }

        return transitiveAttribute(superLocation, attributePath)
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun getString(attributeLocation: AttributeLocation): String {
        return getString(attributeLocation.objectLocation, attributeLocation.attributePath)
    }


    fun getString(objectLocation: ObjectLocation, attributePath: AttributePath): String {
        val scalarParameter = transitiveAttribute(objectLocation, attributePath)
                ?: throw IllegalArgumentException("Not found: $objectLocation.$attributePath")

        return scalarParameter.asString()
            ?: throw IllegalArgumentException("Expected string ($objectLocation.$attributePath): $scalarParameter")
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun withNewDocument(
            documentPath: DocumentPath,
            documentNotation: DocumentNotation
    ): GraphNotation {
        check(! documents.values.containsKey(documentPath)) {"Already exists: $documentPath"}

        val buffer = mutableMapOf<DocumentPath, DocumentNotation>()

        buffer.putAll(documents.values)

        buffer[documentPath] = documentNotation

        return GraphNotation(DocumentPathMap(buffer))
    }


    fun withModifiedDocument(
            documentPath: DocumentPath,
            documentNotation: DocumentNotation
    ): GraphNotation {
        check(documents.values.containsKey(documentPath)) {"Not found: $documentPath"}

        val buffer = mutableMapOf<DocumentPath, DocumentNotation>()

        for (e in documents.values) {
            buffer[e.key] =
                    if (e.key == documentPath) {
                        documentNotation
                    }
                    else {
                        e.value
                    }
        }

        return GraphNotation(DocumentPathMap(buffer))
    }


    fun withoutDocument(
            documentPath: DocumentPath
    ): GraphNotation {
        check(documents.values.containsKey(documentPath)) {"Already absent: $documentPath"}

        val buffer = mutableMapOf<DocumentPath, DocumentNotation>()

        for (e in documents.values) {
            if (e.key == documentPath) {
                continue
            }

            buffer[e.key] = e.value
        }

        return GraphNotation(DocumentPathMap(buffer))
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun filterPaths(predicate: (DocumentPath) -> Boolean): GraphNotation {
        val filteredDocuments = mutableMapOf<DocumentPath, DocumentNotation>()

        for (e in documents.values) {
            if (! predicate.invoke(e.key)) {
                continue
            }

            filteredDocuments[e.key] = e.value
        }

        return GraphNotation(DocumentPathMap(filteredDocuments))
    }
}