package ru.schemator

/** Json Schema keywords */
object Schema {
    const val id = "\$id"
    const val schema = "\$schema"

    const val title = "title"
    const val type = "type"
    const val properties = "properties"
    const val required = "required"
    const val description = "description"

    const val minimum = "minimum"
    const val maximum = "maximum"
    const val minLength = "minLength"
    const val maxLength = "maxLength"
    const val items = "items"
    const val ref = "\$ref"
}

object SchemaTypes {
    const val integer = "integer"
    const val number = "number"
    const val string = "string"
    const val datetime = "datetime"
    const val date = "date"

    const val obj = "object"
    const val array = "array"

    fun toMetadataType(str: String): PrimitiveDataTypes {
        return when(str) {
            integer -> PrimitiveDataTypes.integer
            number -> PrimitiveDataTypes.double
            string -> PrimitiveDataTypes.string
            datetime -> PrimitiveDataTypes.datetime
            date -> PrimitiveDataTypes.date
            else -> TODO()
        }
    }

    /** Check if string is primitive type */
    fun isPrimitive(type: String) =
            type == integer
                    || type == number
                    || type == string
}
