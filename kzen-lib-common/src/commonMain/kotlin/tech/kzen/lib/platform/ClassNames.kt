package tech.kzen.lib.platform


object ClassNames {
    val kotlinAny = ClassName("kotlin.Any")
    val kotlinString = ClassName("kotlin.String")
    val kotlinBoolean = ClassName("kotlin.Boolean")
    val kotlinInt = ClassName("kotlin.Int")
    val kotlinDouble = ClassName("kotlin.Double")

    val kotlinList = ClassName("kotlin.collections.List")


    fun isPrimitive(className: ClassName): Boolean {
        return className == kotlinString ||
                className == kotlinBoolean ||
                className == kotlinInt ||
                className == kotlinDouble;
    }
}