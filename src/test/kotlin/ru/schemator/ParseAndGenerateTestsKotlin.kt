package ru.schemator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.readers.MetadataReaderKotlin
import java.io.File

/**
 * Tests which read JSON, parse it and then assert parsed result is equal to expected.
 *
 */
class ParseAndGenerateTestsKotlin {

    @Test
    fun `given primitives datatypes schema, do generate kotlin, expect generated correctly`() {

        val jsonSchema = File(ClassLoader.getSystemResource("ParseAndGenerateTestsKotlin/PrimitivesAndOneObject.json").file).readText().trim()
        val expected = File(ClassLoader.getSystemResource("ParseAndGenerateTestsKotlin/PrimitivesAndOneObjectResult.kt").file).readText().trim()

        val jsonSchemaMetadataOutput = MetadataReader(jsonSchema, LaunchArguments(
                language = Languages.kotlin,
                input = "",
                output = ""
        )).readSchema()

        val generated = MetadataReaderKotlin(jsonSchemaMetadataOutput).toClasses()
        Assertions.assertEquals(expected.trim(), generated.joinToString("\n") { it.trim() })
    }
}
