package ru.schemator.readers

import ru.schemator.JsonSchemaMetadataOutput
import ru.schemator.PrimitiveDataTypes

class MetadataPrinterGo(schema: JsonSchemaMetadataOutput): LanguageSchemaPrinter(schema) {
    override fun toClasses(): List<String> {
        TODO()
    }

    override fun primitiveDataTypeToLanguage(dataType: PrimitiveDataTypes): String {
        TODO("Not yet implemented")
    }
}
