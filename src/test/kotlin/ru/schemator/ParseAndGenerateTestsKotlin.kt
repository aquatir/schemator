package ru.schemator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.readers.MetadataPrinterKotlin
import java.io.File

/**
 * Tests which read JSON, parse it and then assert parsed result is equal to expected.
 *
 */
class ParseAndGenerateTestsKotlin {

    @Test
    fun `given primitives datatypes with one object schema, do generate kotlin, expect generated correctly`() {

        schemaReadAndValidateTest(
                readSchemaFrom = "ParseAndGenerateTestsKotlin/PrimitivesAndOneObject.json",
                readExpectedClassFrom = "ParseAndGenerateTestsKotlin/PrimitivesAndOneObjectResult.kt"
        )
    }

    @Test
    fun `given arrays of primitive and of object schema, do generate kotlin, expect generated correctly`() {

        schemaReadAndValidateTest(
                readSchemaFrom = "ParseAndGenerateTestsKotlin/ArraysOfPrimitivesAndObjects.json",
                readExpectedClassFrom = "ParseAndGenerateTestsKotlin/ArraysOfPrimitivesAndObjectsResult.kt"
        )
    }


    private fun schemaReadAndValidateTest(readSchemaFrom: String, readExpectedClassFrom: String) {

        val jsonSchema = File(ClassLoader.getSystemResource(readSchemaFrom).file).readText().trim()
        val expected = File(ClassLoader.getSystemResource(readExpectedClassFrom).file).readText().trim()

        val jsonSchemaMetadataOutput = MetadataReader(jsonSchema, LaunchArguments(
                language = Languages.kotlin,
                input = "",
                output = ""
        )).readSchema()

        val generated = MetadataPrinterKotlin(jsonSchemaMetadataOutput).toClasses()
        Assertions.assertEquals(expected.trim(), generated.joinToString("\n") { it.trim() })
    }
}
