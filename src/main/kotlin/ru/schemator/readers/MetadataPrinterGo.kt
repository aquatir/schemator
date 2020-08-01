package ru.schemator.readers

import ru.schemator.JsonSchemaMetadataOutput

class MetadataPrinterGo(schema: JsonSchemaMetadataOutput): LanguageSchemaPrinter(schema) {
    override fun toClasses(): List<String> {
        TODO()
    }
}
