package ru.schemator.printers

import ru.schemator.reader.JsonSchemaMetadataOutput
import ru.schemator.reader.PrimitiveDataTypes

class MetadataPrinterGo(schema: JsonSchemaMetadataOutput): LanguageSchemaPrinter(schema) {
    override fun toClasses(): List<String> {
        TODO()
    }

    override fun primitiveDataTypeToLanguage(dataType: PrimitiveDataTypes): String {
        TODO("Not yet implemented")
    }
}
