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

    fun toMetadataType(str: String): DataTypes {
        return when(str) {
            integer -> DataTypes.integer
            number -> DataTypes.double
            string -> DataTypes.string
            datetime -> DataTypes.datetime
            date -> DataTypes.date
            obj -> DataTypes.obj
            array -> DataTypes.array
            else -> TODO()
        }
    }

    /** Check if string is primitive type */
    fun isPrimitive(type: String) =
            type == integer
                    || type == number
                    || type == string
}
