package ru.schemator.readers

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.schemator.PrimitiveDataTypes
import ru.schemator.GeneratableClassMetadata
import ru.schemator.GeneratablePropertyMetadata
import ru.schemator.JsonSchemaMetadataOutput
import ru.schemator.ObjectPropertyMetadata
import ru.schemator.PrimitivePropertyMetadata
import java.io.File

class MetadataPrinterKotlinTests {

    @Test
    fun `given primitives datatypes schema, do generate kotlin, expect generated correctly`() {

        val simpleMetadata = JsonSchemaMetadataOutput(
                listOf(GeneratableClassMetadata(
                        className = "Test",
                        propertyMetadata = listOf(
                                PrimitivePropertyMetadata("strNotNull", false, "", PrimitiveDataTypes.string),
                                PrimitivePropertyMetadata("strNull", true, "", PrimitiveDataTypes.string),
                                PrimitivePropertyMetadata("integerNotNull",  false, "", PrimitiveDataTypes.integer),
                                PrimitivePropertyMetadata("integerNull", true, "", PrimitiveDataTypes.integer),
                                PrimitivePropertyMetadata("doubleNotNull", false, "", PrimitiveDataTypes.double),
                                PrimitivePropertyMetadata("doubleNull",  true, "", PrimitiveDataTypes.double),
                                PrimitivePropertyMetadata("datetimeNotNull", false, "", PrimitiveDataTypes.datetime),
                                PrimitivePropertyMetadata("datetimeNull", true, "", PrimitiveDataTypes.datetime),
                                PrimitivePropertyMetadata("dateNotNull", false, "", PrimitiveDataTypes.date),
                                PrimitivePropertyMetadata("dateNull", true, "", PrimitiveDataTypes.date)
                        )
                ))
        )

        val metadataReader = MetadataPrinterKotlin(simpleMetadata)
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
                                ObjectPropertyMetadata("otherPros",   false, "", "OtherProps")
                        )
                ),
                GeneratableClassMetadata(
                        className = "OtherProps",
                        propertyMetadata = listOf(
                                PrimitivePropertyMetadata("simple", true, "", PrimitiveDataTypes.string)
                        )
                ))
        )

        val metadataReader = MetadataPrinterKotlin(metaDataWithObjects)
        val generatedResult = metadataReader.toClasses().joinToString("\n") { it.trim() }
        val expected = File(ClassLoader.getSystemResource("MetadataReaderKotlinTests/kotlinObjectPropertyTestData.kt").file).readText().trim()

        Assertions.assertEquals(expected, generatedResult)
    }
}
