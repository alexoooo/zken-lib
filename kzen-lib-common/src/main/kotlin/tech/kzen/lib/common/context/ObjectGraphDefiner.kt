package tech.kzen.lib.common.context

import tech.kzen.lib.common.api.ObjectCreator
import tech.kzen.lib.common.api.ObjectDefiner
import tech.kzen.lib.common.definition.GraphDefinition
import tech.kzen.lib.common.definition.ObjectDefinition
import tech.kzen.lib.common.metadata.model.GraphMetadata
import tech.kzen.lib.common.notation.NotationConventions
import tech.kzen.lib.common.notation.model.NotationTree
import tech.kzen.lib.common.objects.bootstrap.DefaultConstructorObjectCreator
import tech.kzen.lib.common.objects.bootstrap.DefaultConstructorObjectDefiner
import tech.kzen.lib.common.api.model.*
import kotlin.reflect.KClass


object ObjectGraphDefiner {
    //-----------------------------------------------------------------------------------------------------------------
    val bootstrapObjects = mapOf(
            bootstrapEntry(DefaultConstructorObjectCreator),
            bootstrapEntry(DefaultConstructorObjectDefiner)
    )


    private fun bootstrapEntry(bootstrapObject: Any): Pair<ObjectLocation, Any> {
        val objectPath = bootstrapPath(bootstrapObject::class)
        return objectPath to bootstrapObject
    }


    private fun bootstrapPath(type: KClass<*>): ObjectLocation {
        return bootstrapPath(ObjectName(type.simpleName!!))
    }


    private fun bootstrapPath(objectName: ObjectName): ObjectLocation {
        return ObjectLocation(
                NotationConventions.kzenBasePath,
                ObjectPath(objectName, BundleNesting.root))
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun define(
            projectNotation: NotationTree,
            projectMetadata: GraphMetadata
    ): GraphDefinition {
        val definerAndRelatedInstances = mutableMapOf<ObjectLocation, Any>()

        definerAndRelatedInstances.putAll(bootstrapObjects)

        val openDefinitions = projectNotation
                .objectLocations
                .filter {
                    ! bootstrapObjects.containsKey(it) &&
                            ! isAbstract(it, projectNotation)
                }.toMutableSet()

        val closedDefinitions = mutableMapOf<ObjectLocation, ObjectDefinition>()
        val missingInstances = mutableSetOf<ObjectLocation>()

        val levelClosed = mutableSetOf<ObjectLocation>()
        val levelCreated = mutableSetOf<ObjectLocation>()
        val missingCreatorInstances = mutableSetOf<ObjectLocation>()

        var levelCount = 0
        while (! openDefinitions.isEmpty()) {
            levelCount += 1
            check(levelCount < 10) {"too deep"}
            println("^^^^^ open - $levelCount: $openDefinitions")

            for (objectLocation in openDefinitions) {
                println("^^^^^ objectName: $objectLocation")

                val definerReference = ObjectReference.parse(definerName(objectLocation, projectNotation))
                val definerLocation = projectNotation.coalesce.locate(objectLocation, definerReference)
                val definer = definerAndRelatedInstances[definerLocation] as? ObjectDefiner

                if (definer == null) {
                    missingInstances.add(definerLocation)
                    continue
                }

                val definition = definer.define(
                        objectLocation,
                        projectNotation,
                        projectMetadata,
                        GraphDefinition(ObjectMap(closedDefinitions)),
                        ObjectGraph(ObjectMap(definerAndRelatedInstances)))
                println("  >> definition: $definition")

                if (definition.isError()) {
                    println(" !! definition error: ${definition.errorMessage}")

                    missingInstances.addAll(definition.missingObjects)
                    continue
                }

                closedDefinitions[objectLocation] = definition.value!!
                levelClosed.add(objectLocation)
            }

            println("--- missingInstances: $missingInstances")
            for (missingName in missingInstances) {
                val definition =
                        closedDefinitions[missingName]
                        ?: continue

                println("  $$ got definition for: $missingName")
                val creatorLocation = projectNotation.coalesce.locate(missingName, definition.creator)

                var hasMissingCreatorInstances = false
                if (! definerAndRelatedInstances.containsKey(creatorLocation)) {
                    missingCreatorInstances.add(creatorLocation)
                    hasMissingCreatorInstances = true

                    println("  $$ missing creator ($missingName): $creatorLocation")
                }

                for (creatorReference in definition.creatorReferences) {
                    val creatorReferenceLocation =
                            projectNotation.coalesce.locate(missingName, creatorReference)

                    if (! definerAndRelatedInstances.containsKey(creatorReferenceLocation)) {
                        missingCreatorInstances.add(creatorReferenceLocation)
                        hasMissingCreatorInstances = true

                        println("  $$ missing creator reference ($missingName): " +
                                "${definition.creator} - $creatorReferenceLocation")
                    }
                }

                if (hasMissingCreatorInstances) {
                    continue
                }

                val creator = definerAndRelatedInstances[creatorLocation] as ObjectCreator

                val newInstance = creator.create(
                        missingName,
                        definition,
                        projectMetadata.objectMetadata.get(missingName),
                        ObjectGraph(ObjectMap(definerAndRelatedInstances)))

                println("  $$ created: $missingName")

                definerAndRelatedInstances[missingName] = newInstance
                levelCreated.add(missingName)
            }

            missingInstances.addAll(missingCreatorInstances)
            missingInstances.removeAll(levelCreated)

            check(levelClosed.isNotEmpty() || levelCreated.isNotEmpty()) { "Graph cycle?" }

            openDefinitions.removeAll(levelClosed)

            levelCreated.clear()
            levelClosed.clear()
            missingCreatorInstances.clear()
        }
        return GraphDefinition(ObjectMap(closedDefinitions))
    }



    private fun definerName(
            objectName: ObjectLocation,
            projectNotation: NotationTree
    ): String {
        return projectNotation.getString(objectName, NotationConventions.definerPath)
    }


    private fun isAbstract(
            objectName: ObjectLocation,
            projectNotation: NotationTree
    ): Boolean {
        return projectNotation.directParameter(objectName, NotationConventions.abstractPath)
                ?.asBoolean()
                ?: false
    }
}