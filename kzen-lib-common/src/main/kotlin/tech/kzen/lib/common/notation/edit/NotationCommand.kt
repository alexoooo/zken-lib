package tech.kzen.lib.common.notation.edit

import tech.kzen.lib.common.api.model.*
import tech.kzen.lib.common.notation.model.*


//---------------------------------------------------------------------------------------------------------------------
sealed class NotationCommand


sealed class StructuralNotationCommand: NotationCommand()
sealed class SemanticNotationCommand: NotationCommand()


//---------------------------------------------------------------------------------------------------------------------
data class CreateBundleCommand(
        val bundlePath: BundlePath
): StructuralNotationCommand()



data class DeletePackageCommand(
        val filePath: BundlePath
): StructuralNotationCommand()


//---------------------------------------------------------------------------------------------------------------------
data class AddObjectCommand(
        val location: PositionedObjectLocation,
        val body: ObjectNotation
): StructuralNotationCommand() {
    companion object {
        fun ofParent(
                location: PositionedObjectLocation,
                parentName: ObjectName
        ): AddObjectCommand {
            return ofParent(location, ObjectReference(parentName, null, null))
        }

        fun ofParent(
                location: PositionedObjectLocation,
                parentReference: ObjectReference
        ): AddObjectCommand {
            val parentBody = ObjectNotation.ofParent(parentReference.asString())
            return AddObjectCommand(location, parentBody)
        }
    }
}


data class RemoveObjectCommand(
        val location: ObjectLocation
): StructuralNotationCommand()


data class ShiftObjectCommand(
        val location: ObjectLocation,
        val newPositionInBundle: PositionIndex
): StructuralNotationCommand()


data class RenameObjectCommand(
        val location: ObjectLocation,
        val newName: ObjectName
): StructuralNotationCommand()


data class RelocateObjectCommand(
        val location: ObjectLocation,
        val newObjectPath: PositionedObjectPath
): StructuralNotationCommand()


//---------------------------------------------------------------------------------------------------------------------
data class UpsertAttributeCommand(
        val objectLocation: ObjectLocation,
        val attributeName: AttributeName,
        val attributeNotation: AttributeNotation
): StructuralNotationCommand()


data class ClearAttributeCommand(
        val objectLocation: ObjectLocation,
        val attributeName: AttributeName
): StructuralNotationCommand()


data class UpdateInAttributeCommand(
        val objectLocation: ObjectLocation,
        val attributeNesting: AttributePath,
        val attributeNotation: AttributeNotation
): StructuralNotationCommand()


data class InsertListItemInAttributeCommand(
        val objectLocation: ObjectLocation,
        val containingList: PositionedAttributeNesting,
        val item: AttributeNotation
): StructuralNotationCommand()


data class InsertMapEntryInAttributeCommand(
        val objectLocation: ObjectLocation,
        val containingMap: PositionedAttributeNesting,
        val key: AttributeSegment,
        val value: AttributeNotation
): StructuralNotationCommand()


data class ShiftInAttributeCommand(
        val objectLocation: ObjectLocation,
        val containingStructure: PositionedAttributeNesting
): StructuralNotationCommand()



//---------------------------------------------------------------------------------------------------------------------
data class InsertObjectInListAttributeCommand(
        val containingObjectLocation: ObjectLocation,
        val containingListPosition: PositionedAttributeNesting,
//        val objectLocation: PositionedObjectLocation,
        val objectName: ObjectName,
        val positionInBundle: PositionIndex,
        val body: ObjectNotation
): StructuralNotationCommand()


// TODO: could use __REF__ or inline object definition?
//data class InsertObjectInMapAttributeCommand(
//        val objectLocation: PositionedObjectLocation,
//        val containingMapPosition: PositionedAttributeNesting,
//        val key: MapKeyAttributeSegment,
//        val body: ObjectNotation
//): ProjectCommand()


data class RenameRefactorCommand(
        val objectLocation: ObjectLocation,
        val newName: ObjectName
): SemanticNotationCommand()


//data class MoveRefactorCommand(
//        val objectLocation: ObjectLocation,
//        val newName: ObjectName
//): ProjectCommand()