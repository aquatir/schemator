package ru.schemator.printers

import ru.schemator.reader.JsonSchemaMetadataOutput
import ru.schemator.reader.PrimitiveDataTypes

/** Generic class for schema readers. */
abstract class LanguageSchemaPrinter(private val schema: JsonSchemaMetadataOutput) {
    /** Read [schema], parse and return generated code as list of strings  */
    abstract fun toClasses(): List<String>

    abstract fun primitiveDataTypeToLanguage(dataType: PrimitiveDataTypes): String

    open fun mbNullable(nullable: Boolean): String {
        return ""
    }
}
