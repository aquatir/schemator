package ru.schemator.readers

import ru.schemator.DataTypes
import ru.schemator.GeneratableClass
import ru.schemator.GeneratableProperty
import ru.schemator.JsonSchemaMetadataOutput
import java.lang.StringBuilder

/** Read jsonSchema, parse and return kotlin code */
class MetadataReaderKotlin(private val schema: JsonSchemaMetadataOutput) : LanguageSchemaReader(schema) {
    override fun toClasses(): String {
        return schema.entries.joinToString(separator = "\n") { toKotlinClass(it) }
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
                separator = ",\n",
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
        val strBuilder = StringBuilder()
        if (!property.comment.isNullOrBlank()) {
            strBuilder.append("        /** ${property.comment} */\n")
        }
        strBuilder.append("        val ${property.propertyName.decapitalize()}: ${dataTypeToKotlin(property)}${mbNullable(property.isNullable)}")
        return strBuilder.toString()
    }

    fun dataTypeToKotlin(property: GeneratableProperty): String {
        return when (property.propertyDataType) {
            DataTypes.date -> "LocalDate"
            DataTypes.datetime -> "LocalDateTime"
            DataTypes.double -> "Double"
            DataTypes.integer -> "Int"
            DataTypes.string -> "String"
            DataTypes.obj -> property.objectTypeName!!   // always available for type = objects
            DataTypes.array -> "" // TODO: what to do with arrays?
        }
    }

    fun mbNullable(nullable: Boolean): String {
        return if (nullable) "?" else ""
    }


}
