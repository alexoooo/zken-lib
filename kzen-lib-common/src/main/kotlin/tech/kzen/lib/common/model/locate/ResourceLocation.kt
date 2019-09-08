package tech.kzen.lib.common.model.locate

import tech.kzen.lib.common.model.document.DocumentPath
import tech.kzen.lib.common.model.resource.ResourcePath


data class ResourceLocation(
        val documentPath: DocumentPath,
        val resourcePath: ResourcePath
)