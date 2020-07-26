package ru.schemator.readers

import ru.schemator.JsonSchemaMetadataOutput

/** Generic class for schema readers. */
abstract class LanguageSchemaReader(private val schema: JsonSchemaMetadataOutput) {
    /** Read [schema], parse and return generated code as list of strings  */
    abstract fun toClasses(): List<String>
}
