package tech.kzen.lib.server.objects

import tech.kzen.lib.common.model.locate.ObjectLocation
import tech.kzen.lib.common.model.structure.notation.DocumentNotation
import tech.kzen.lib.common.model.structure.notation.ObjectNotation


class SelfAware(
        val objectLocation: ObjectLocation,
        val objectNotation: ObjectNotation,
        val documentNotation: DocumentNotation)