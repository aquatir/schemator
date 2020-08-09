package ru.schemator.readers

import ru.schemator.ArrayGenericParameter
import ru.schemator.ArrayPropertyMetadata
import ru.schemator.PrimitiveDataTypes
import ru.schemator.GeneratableClassMetadata
import ru.schemator.GeneratablePropertyMetadata
import ru.schemator.JsonSchemaMetadataOutput
import ru.schemator.ObjectPropertyMetadata
import ru.schemator.PrimitivePropertyMetadata
import java.lang.StringBuilder

/** Read metadata, parse and return kotlin code */
class MetadataPrinterKotlin(private val schema: JsonSchemaMetadataOutput) : LanguageSchemaPrinter(schema) {
    override fun toClasses(): List<String> {
        return schema.entries.map { toKotlinClass(it) }
    }

    //
    //
    // CLASS/SCHEMAS PRINTERS
    //   TODO: GENERIFY?
    //
    //

    fun toKotlinClass(generatableClassMetadata: GeneratableClassMetadata): String {
        val commentOnTop = if (generatableClassMetadata.description != null) """
/**
 * ${generatableClassMetadata.description}
 */""" + "\n" else ""
        return generatableClassMetadata.propertyMetadata.joinToString(
                prefix = "${commentOnTop}data class ${generatableClassMetadata.className.capitalize()}(\n",
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
    fun toKotlinProperty(propertyMetadata: GeneratablePropertyMetadata): String {
        val strBuilder = StringBuilder()
        if (!propertyMetadata.comment.isNullOrBlank()) {
            strBuilder.append("        /** ${propertyMetadata.comment} */\n")
        }
        strBuilder.append("        val ${propertyMetadata.propertyName.decapitalize()}: ${dataTypeToKotlin(propertyMetadata)}${mbNullable(propertyMetadata.isNullable)}")
        return strBuilder.toString()
    }

    fun dataTypeToKotlin(propertyMetadata: GeneratablePropertyMetadata): String {
        return when (propertyMetadata) {
            is PrimitivePropertyMetadata -> primitiveDataTypeToLanguage(propertyMetadata.dataType)
            is ObjectPropertyMetadata -> propertyMetadata.objectTypeName

            // TODO: Handle internal arrays
            is ArrayPropertyMetadata -> when (propertyMetadata.arrayGenericParameter) {
                is ArrayGenericParameter.Primitive -> "List<${primitiveDataTypeToLanguage(propertyMetadata.arrayGenericParameter.primitiveName)}>"
                is ArrayGenericParameter.Obj -> "List<${propertyMetadata.arrayGenericParameter.objectName}>"
                is ArrayGenericParameter.Holder -> TODO()
            }
        }
    }

    override fun mbNullable(nullable: Boolean): String {
        return if (nullable) "?" else ""
    }

    override fun primitiveDataTypeToLanguage(dataType: PrimitiveDataTypes): String {
        return when (dataType) {
            PrimitiveDataTypes.date -> "LocalDate"
            PrimitiveDataTypes.datetime -> "LocalDateTime"
            PrimitiveDataTypes.double -> "Double"
            PrimitiveDataTypes.integer -> "Int"
            PrimitiveDataTypes.string -> "String"
        }
    }


}
