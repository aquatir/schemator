package ru.schemator

import TinyLogger
import ru.schemator.readers.MetadataPrinterGo
import ru.schemator.readers.MetadataPrinterKotlin


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
            "work": {
              "type": "object",
              "properties": {
                  "stuff": {
                    "type": "array",
                    "items": {
                      "type": "string"
                    }
                  },
                  "stuffArray": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                         "top": {
                           "type": "datetime"
                         },
                         "test": {
                           "type": "integer"
                         }
                       }
                    }
                  }
              },
              "required": ["place"]
            }
          }
        }
    """.trimIndent()

    logger.debug("input schema:\n $schema")

    val jsonSchemaMetadataOutput = MetadataReader(schema, parsedArgs).readSchema()

    val generated = when (parsedArgs.language) {
        Languages.kotlin -> MetadataPrinterKotlin(jsonSchemaMetadataOutput).toClasses()
        Languages.go -> MetadataPrinterGo(jsonSchemaMetadataOutput).toClasses()
    }

    println("\n==================\n===== result =====\n==================")
    println(generated.joinToString("\n"))
}


enum class Languages {
    kotlin, go
}

data class LaunchArguments(

        /** Generated language. Possible values [Languages]*/
        val language: Languages = Languages.kotlin,

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

