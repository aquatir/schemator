package ru.schemator.readers

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.DataTypes
import ru.schemator.GeneratableClassMetadata
import ru.schemator.GeneratablePropertyMetadata
import ru.schemator.JsonSchemaMetadataOutput
import java.io.File

class MetadataReaderKotlinTests {

    @Test
    fun `given primitives datatypes schema, do generate kotlin, expect generated correctly`() {

        val simpleMetadata = JsonSchemaMetadataOutput(
                listOf(GeneratableClassMetadata(
                        className = "Test",
                        propertyMetadata = listOf(
                                GeneratablePropertyMetadata("strNotNull", DataTypes.string, false),
                                GeneratablePropertyMetadata("strNull", DataTypes.string, true),
                                GeneratablePropertyMetadata("integerNotNull", DataTypes.integer, false),
                                GeneratablePropertyMetadata("integerNull", DataTypes.integer, true),
                                GeneratablePropertyMetadata("doubleNotNull", DataTypes.double, false),
                                GeneratablePropertyMetadata("doubleNull", DataTypes.double, true),
                                GeneratablePropertyMetadata("datetimeNotNull", DataTypes.datetime, false),
                                GeneratablePropertyMetadata("datetimeNull", DataTypes.datetime, true),
                                GeneratablePropertyMetadata("dateNotNull", DataTypes.date, false),
                                GeneratablePropertyMetadata("dateNull", DataTypes.date, true)
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
                listOf(GeneratableClassMetadata(
                        className = "Test",
                        propertyMetadata = listOf(
                                GeneratablePropertyMetadata("otherPros", DataTypes.obj,  false, "OtherProps")
                        )
                ),
                GeneratableClassMetadata(
                        className = "OtherProps",
                        propertyMetadata = listOf(
                                GeneratablePropertyMetadata("simple", DataTypes.string,  true)
                        )
                ))
        )

        val metadataReader = MetadataReaderKotlin(metaDataWithObjects)
        val generatedResult = metadataReader.toClasses().joinToString("\n") { it.trim() }
        val expected = File(ClassLoader.getSystemResource("MetadataReaderKotlinTests/kotlinObjectPropertyTestData.kt").file).readText().trim()

        Assertions.assertEquals(expected, generatedResult)
    }
}
