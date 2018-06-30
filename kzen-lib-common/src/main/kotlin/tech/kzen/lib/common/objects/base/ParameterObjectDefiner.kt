package tech.kzen.lib.common.objects.base

import tech.kzen.lib.common.api.ObjectDefiner
import tech.kzen.lib.common.api.ParameterDefiner
import tech.kzen.lib.common.context.ObjectGraph
import tech.kzen.lib.common.definition.GraphDefinition
import tech.kzen.lib.common.definition.ObjectDefinition
import tech.kzen.lib.common.definition.ObjectDefinitionAttempt
import tech.kzen.lib.common.definition.ParameterDefinition
import tech.kzen.lib.common.metadata.model.GraphMetadata
import tech.kzen.lib.common.notation.model.ProjectNotation
import tech.kzen.lib.common.notation.model.ScalarParameterNotation
import tech.kzen.lib.platform.Mirror


class ParameterObjectDefiner : ObjectDefiner {
    companion object {
        private const val classParameter = "class"
        private const val creatorParameter = "creator"

        private val defaultParameterDefiner =
                NotationParameterDefiner::class.simpleName!!

        private val defaultParameterCreator =
                StructuralParameterCreator::class.simpleName!!
    }

    override fun define(
            objectName: String,
            projectNotation: ProjectNotation,
            projectMetadata: GraphMetadata,
            projectDefinition: GraphDefinition,
            objectGraph: ObjectGraph
    ): ObjectDefinitionAttempt {
        val objectMetadata = projectMetadata.objectMetadata[objectName]
                ?: throw IllegalArgumentException("Metadata not found: $objectName")

        val className = projectNotation.getString(objectName, classParameter)

        val constructorArguments = mutableMapOf<String, ParameterDefinition>()
        val parameterCreators = mutableSetOf<String>()

        val argumentNames = Mirror.constructorArgumentNames(className)
        println("&&& class arguments ($className): $argumentNames")

        for (argumentName in argumentNames) {
            val parameterMetadata = objectMetadata.parameters[argumentName]
                    ?: throw IllegalArgumentException(
                            "Argument not found in metadata ($argumentName): $objectMetadata")

            val parameterCreatorName = parameterMetadata.creator ?: defaultParameterCreator
            parameterCreators.add(parameterCreatorName)

            val parameterDefinerName = parameterMetadata.definer ?: defaultParameterDefiner

            val definerInstance = objectGraph
                    .find(parameterDefinerName)
                    ?: return ObjectDefinitionAttempt.missingObjectsFailure(
                            setOf(parameterDefinerName))

            val parameterDefiner = definerInstance
                    as? ParameterDefiner
                    ?: return ObjectDefinitionAttempt.failure(
                            "Parameter Definer expected: $parameterDefinerName")

            val parameterDefinition = parameterDefiner.define(
                    objectName,
                    argumentName,
                    projectNotation,
                    projectMetadata,
                    projectDefinition,
                    objectGraph)

            constructorArguments[argumentName] = parameterDefinition
        }

        val creatorName = projectNotation.getString(objectName, creatorParameter)

        val objectDefinition = ObjectDefinition(
                className,
                constructorArguments,
                creatorName,
                parameterCreators)

        return ObjectDefinitionAttempt.success(objectDefinition)
    }
}