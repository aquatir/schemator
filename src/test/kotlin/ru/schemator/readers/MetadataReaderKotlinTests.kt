package ru.schemator.readers

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.DataTypes
import ru.schemator.GeneratableClass
import ru.schemator.GeneratableProperty
import ru.schemator.JsonSchemaMetadataOutput
import java.io.File

class MetadataReaderKotlinTests {

    @Test
    fun `given primitives datatypes schema, do generate kotlin, expect generated correctly`() {

        val simpleMetadata = JsonSchemaMetadataOutput(
                listOf(GeneratableClass(
                        className = "Test",
                        properties = listOf(
                                GeneratableProperty("strNotNull", DataTypes.string, false),
                                GeneratableProperty("strNull", DataTypes.string, true),
                                GeneratableProperty("integerNotNull", DataTypes.integer, false),
                                GeneratableProperty("integerNull", DataTypes.integer, true),
                                GeneratableProperty("doubleNotNull", DataTypes.double, false),
                                GeneratableProperty("doubleNull", DataTypes.double, true),
                                GeneratableProperty("datetimeNotNull", DataTypes.datetime, false),
                                GeneratableProperty("datetimeNull", DataTypes.datetime, true),
                                GeneratableProperty("dateNotNull", DataTypes.date, false),
                                GeneratableProperty("dateNull", DataTypes.date, true)
                        )
                ))
        )

        val metadataReader = MetadataReaderKotlin(simpleMetadata)
        val generatedResult = metadataReader.toClasses().joinToString("\n") { it.trim() }
        val expected = File(ClassLoader.getSystemResource("MetadataReaderKotlinTests/kotlinSimplePropertyTestData.kt").file).readText().trim()

        Assertions.assertEquals(expected, generatedResult)
    }

    @Test
    fun `given schema with objects, do generate kotlin, expect generated correctly`() {
        val metaDataWithObjects = JsonSchemaMetadataOutput(
                listOf(GeneratableClass(
                        className = "Test",
                        properties = listOf(
                                GeneratableProperty("otherPros", DataTypes.obj,  false, "OtherProps")
                        )
                ),
                GeneratableClass(
                        className = "OtherProps",
                        properties = listOf(
                                GeneratableProperty("simple", DataTypes.string,  true)
                        )
                ))
        )

        val metadataReader = MetadataReaderKotlin(metaDataWithObjects)
        val generatedResult = metadataReader.toClasses().joinToString("\n") { it.trim() }
        val expected = File(ClassLoader.getSystemResource("MetadataReaderKotlinTests/kotlinObjectPropertyTestData.kt").file).readText().trim()

        Assertions.assertEquals(expected, generatedResult)
    }
}
