package ru.schemator.readers

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.DataTypes
import ru.schemator.GeneratableClass
import ru.schemator.GeneratableProperty
import ru.schemator.JsonSchemaMetadataOutput
import java.io.File

class MetadataReaderKotlinTests {

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

    @Test
    fun `given simple datatypes schema, do generate kotlin, expect generated correctly`() {
        val metadataReader = MetadataReaderKotlin(simpleMetadata)
        val generatedResult = metadataReader.toClasses().trim()
        val expected = File(ClassLoader.getSystemResource("kotlinSimplePropertyTest.kt").file).readText().trim()

        Assertions.assertEquals(expected, generatedResult)
    }
}
