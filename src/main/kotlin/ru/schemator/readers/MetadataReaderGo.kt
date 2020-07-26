package ru.schemator.readers

import ru.schemator.JsonSchemaMetadataOutput

class MetadataReaderGo(schema: JsonSchemaMetadataOutput): LanguageSchemaReader(schema) {
    override fun toClasses(): List<String> {
        TODO()
    }
}
