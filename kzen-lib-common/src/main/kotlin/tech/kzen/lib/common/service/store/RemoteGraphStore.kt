package tech.kzen.lib.common.service.store

import tech.kzen.lib.common.model.structure.notation.cqrs.NotationCommand
import tech.kzen.lib.common.util.Digest


interface RemoteGraphStore {
    fun apply(command: NotationCommand): Digest
}