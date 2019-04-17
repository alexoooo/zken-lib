package tech.kzen.lib.common.context

import tech.kzen.lib.common.api.ObjectCreator
import tech.kzen.lib.common.api.ObjectDefiner
import tech.kzen.lib.common.definition.GraphDefinition
import tech.kzen.lib.common.model.locate.ObjectLocation
import tech.kzen.lib.common.model.locate.ObjectLocationMap
import tech.kzen.lib.common.model.locate.ObjectReference
import tech.kzen.lib.common.model.obj.ObjectName
import tech.kzen.lib.common.model.obj.ObjectNesting
import tech.kzen.lib.common.model.obj.ObjectPath
import tech.kzen.lib.common.objects.bootstrap.DefaultConstructorObjectCreator
import tech.kzen.lib.common.objects.bootstrap.DefaultConstructorObjectDefiner
import tech.kzen.lib.common.structure.GraphStructure
import tech.kzen.lib.common.structure.notation.NotationConventions
import tech.kzen.lib.common.structure.notation.model.GraphNotation
import tech.kzen.lib.platform.collect.PersistentMap
import tech.kzen.lib.platform.collect.toPersistentMap
import kotlin.reflect.KClass


object GraphDefiner {
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
                ObjectPath(objectName, ObjectNesting.root))
    }


    //-----------------------------------------------------------------------------------------------------------------
    fun define(
            graphStructure: GraphStructure
    ): GraphDefinition {
//        val definerAndRelatedInstances = mutableMapOf<ObjectLocation, Any>()
        var definerAndRelatedInstances = GraphInstance(
                ObjectLocationMap(bootstrapObjects.toPersistentMap()))

//        definerAndRelatedInstances.putAll(bootstrapObjects)

        val openDefinitions: MutableSet<ObjectLocation> = graphStructure
                .graphNotation
                .objectLocations
                .filter {
                    ! bootstrapObjects.containsKey(it) &&
                            ! isAbstract(it, graphStructure.graphNotation)
                }.toMutableSet()

//        val closedDefinitions = mutableMapOf<ObjectLocation, ObjectDefinition>()
        var closedDefinitions = GraphDefinition(ObjectLocationMap(PersistentMap()))

        val missingInstances = mutableSetOf<ObjectLocation>()

        val levelClosed = mutableSetOf<ObjectLocation>()
        val levelCreated = mutableSetOf<ObjectLocation>()
        val missingCreatorInstances = mutableSetOf<ObjectLocation>()

        var levelCount = 0
        while (openDefinitions.isNotEmpty()) {
            levelCount += 1
            check(levelCount < 16) {"too deep"}
//            println("^^^^^ open - $levelCount: $openDefinitions")

            for (objectLocation in openDefinitions) {
//                println("^^^^^ objectName: $objectLocation")

                val definerReference = definerReference(objectLocation, graphStructure.graphNotation)
                val definerLocation = graphStructure.graphNotation.coalesce.locate(objectLocation, definerReference)
                val definer = definerAndRelatedInstances[definerLocation] as? ObjectDefiner

                if (definer == null) {
                    missingInstances.add(definerLocation)
                    continue
                }

                val definition = definer.define(
                        objectLocation,
                        graphStructure,
                        closedDefinitions,
                        definerAndRelatedInstances)
//                println("  >> definition: $definition")

                if (definition.isError()) {
//                    println(" !! definition error: ${definition.errorMessage}")

                    missingInstances.addAll(definition.missingObjects.values)
                    continue
                }

                closedDefinitions = closedDefinitions.put(objectLocation, definition.value!!)

                levelClosed.add(objectLocation)
            }

//            println("--- missingInstances: $missingInstances")
            for (missingLocation in missingInstances) {
                val definition =
                        closedDefinitions[missingLocation]
                        ?: continue

//                println("  $$ got definition for: $missingName")
                val creatorLocation = graphStructure.graphNotation.coalesce.locate(missingLocation, definition.creator)

                var hasMissingCreatorInstances = false
                if (! definerAndRelatedInstances.containsKey(creatorLocation)) {
                    missingCreatorInstances.add(creatorLocation)
                    hasMissingCreatorInstances = true

//                    println("  $$ missing creator ($missingName): $creatorLocation")
                }

                for (creatorRequired in definition.creatorDependencies) {
                    val creatorReferenceLocation =
                            graphStructure.graphNotation.coalesce.locate(missingLocation, creatorRequired)

                    if (! definerAndRelatedInstances.containsKey(creatorReferenceLocation)) {
                        missingCreatorInstances.add(creatorReferenceLocation)
                        hasMissingCreatorInstances = true

//                        println("  $$ missing creator reference ($missingName): " +
//                                "${definition.creator} - $creatorReferenceLocation")
                    }
                }

                if (hasMissingCreatorInstances) {
                    continue
                }

                val creator = definerAndRelatedInstances[creatorLocation] as ObjectCreator

                val instance = creator.create(
                        missingLocation,
                        graphStructure,
                        definition,
                        definerAndRelatedInstances)

//                println("  $$ created: $missingName")

                definerAndRelatedInstances = definerAndRelatedInstances.put(missingLocation, instance)
                levelCreated.add(missingLocation)
            }

            missingInstances.addAll(missingCreatorInstances)
            missingInstances.removeAll(levelCreated)

            check(levelClosed.isNotEmpty() || levelCreated.isNotEmpty()) {
                "Graph cycle? $openDefinitions"
            }

            openDefinitions.removeAll(levelClosed)

            levelCreated.clear()
            levelClosed.clear()
            missingCreatorInstances.clear()
        }
        return closedDefinitions
    }



    private fun definerReference(
            objectLocation: ObjectLocation,
            projectNotation: GraphNotation
    ): ObjectReference {
        return ObjectReference.parse(
                projectNotation.getString(objectLocation, NotationConventions.definerAttributePath))
    }


    private fun isAbstract(
            objectName: ObjectLocation,
            projectNotation: GraphNotation
    ): Boolean {
        return projectNotation.directAttribute(objectName, NotationConventions.abstractAttributePath)
                ?.asBoolean()
                ?: false
    }
}