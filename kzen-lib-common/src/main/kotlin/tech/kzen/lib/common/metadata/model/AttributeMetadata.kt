package tech.kzen.lib.common.metadata.model

import tech.kzen.lib.common.api.model.ObjectReference


data class AttributeMetadata(
        val type: TypeMetadata?,
        val definerReference: ObjectReference?,
        val creatorReference: ObjectReference?)
