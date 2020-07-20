package ru.schemator.readers

import ru.schemator.DataTypes
import ru.schemator.GeneratableClass
import ru.schemator.GeneratableProperty
import ru.schemator.JsonSchemaMetadataOutput

/** Read jsonSchema, parse and return kotlin code */
class MetadataReaderKotlin(private val schema: JsonSchemaMetadataOutput) : LanguageSchemaReader(schema) {
    override fun toClasses(): String {
        return schema.entries.joinToString(separator = "\n\n") { toKotlinClass(it) }
    }

    //
    //
    // CLASS/SCHEMAS PRINTERS
    //   TODO: GENERIFY?
    //
    //

    fun toKotlinClass(generatableClass: GeneratableClass): String {
        val commentOnTop = if (generatableClass.description != null) """
/**
 * ${generatableClass.description}
 */""" + "\n" else ""
        return generatableClass.properties.joinToString(
                prefix = "${commentOnTop}data class ${generatableClass.className.capitalize()}(\n",

                transform = { toKotlinProperty(it) },
                separator = ",\n\n",
                postfix = "\n)")
    }

    //
    //
    //  PROPERTY PRINTERS
    //   TODO: GENERIFY?
    //
    //

    // TODO: Fix indent in some other way instead of hardcoding it
    fun toKotlinProperty(property: GeneratableProperty): String {
        if (property.comment.isNullOrBlank()) {
            return "    val ${property.propertyName.decapitalize()}: ${dataTypeToKotlin(property.propertyDataType)}${mbNullable(property.isNullable)}"
        } else {
            return """
            /** ${property.comment} */
            val ${property.propertyName.decapitalize()}: ${dataTypeToKotlin(property.propertyDataType)}${mbNullable(property.isNullable)}
    """.replaceIndent("    ")

        }
    }

    fun dataTypeToKotlin(dataTypes: DataTypes): String {
        return when (dataTypes) {
            DataTypes.date -> "LocalDate"
            DataTypes.datetime -> "LocalDateTime"
            DataTypes.double -> "Double"
            DataTypes.integer -> "Int"
            DataTypes.string -> "String"
            DataTypes.obj -> ""   // TODO: what to do with objects?
            DataTypes.array -> "" // TODO: what to do with arrays?
        }
    }

    fun mbNullable(nullable: Boolean): String {
        return if (nullable) "?" else ""
    }


}
