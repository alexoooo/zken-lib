package tech.kzen.lib.common.model.obj


data class ObjectName(
        val value: String
) {
    //-----------------------------------------------------------------------------------------------------------------
    override fun toString(): String {
        return value
    }
}