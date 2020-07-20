package ru.schemator

import TinyLogger
import ru.schemator.readers.MetadataReaderGo
import ru.schemator.readers.MetadataReaderKotlin


fun main(args: Array<String>) {

    val parsedArgs = parseArgs(args)
    val logger = TinyLogger("[SCHEMATOR_MAIN]", parsedArgs.debug)

    logger.info("parsed args: $parsedArgs")
    logger.debug("debug logs activated")

    // TODO: Read file here
    val schema = """
        {
          "title": "Person",
          "type": "object",
          "description": "The best person",
          "properties": {
            "firstName": {
              "type": "string",
              "description": "The person's first name."
            },
            "lastName": {
              "type": "string",
              "description": "The person's last name."
            },
            "age": {
              "description": "Age in years which must be equal to or greater than zero.",
              "type": "integer",
              "minimum": 0
            }
          },
          "required": ["lastName", "age"]
        }
    """.trimIndent()

    logger.debug("input schema:\n $schema")
    val jsonSchemaMetadataOutput = MetadataReader(schema, parsedArgs).readSchema()

    val resultClass = when (parsedArgs.language) {
        Languages.kotlin -> MetadataReaderKotlin(jsonSchemaMetadataOutput).toClasses()
        Languages.go -> MetadataReaderGo(jsonSchemaMetadataOutput).toClasses()
    }

    println("result:\n $resultClass")
}



enum class Languages {
    kotlin, go
}

data class LaunchArguments(

        /** Generated language. Possible values [Languages]*/
        val language: Languages,

        /** Input schema file to generate */
        val input: String,

        /** output code filename */
        val output: String,

        val debug: Boolean = false
)

/** Parse command line parameters */
fun parseArgs(args: Array<String>): LaunchArguments {
    // TODO: Fix parsing
    return LaunchArguments(
            language = Languages.kotlin,
            debug = true,
            input = "a",
            output = "b"
    )
}

