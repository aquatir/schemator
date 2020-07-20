### Schemator

**NOTICE:** This is still work in progress!

Your ultimate tool for generating code from json schemas. 

Supported languages: **None!** But working on Kotlin support. Can generate from primitive schemas with no nesting for now. 

#### Example

```json
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
```

Will be turned into 

```kotlin
/**
 * The best person
 */
data class Person(
    /** The person's first name. */
    val firstName: String?,

    /** The person's last name. */
    val lastName: String,

    /** Age in years which must be equal to or greater than zero. */
    val age: Int
)
```

#### Basic approach

1. Parse json schema as json as a whole.
2. Create metadata from schema which can be used to generate classes/objects/structures in any language (done in `MetadataReader.kt`)
3. Call on of MetadataReaders to generate classes (e.g. `MetadataReaderKotlin.kt`)

TODO: explain how stuff works in more details 

#### TODOs:

- schema into metadata parser:
    - add recursive object read
    - add arrays handling
    - add `$ref` handling
    - graceful exception handling for wrong schema names/types/etc
- parse program command line parameters on startup
- logging, especial exceptional logging 
- remember specific types while parsing metadata which require `import` headers for generated classes
- add support for Json libraries annotations
- handle 'object' type in metadata reader

Long plans: 
- generify to support adding other languages apart from kotlin
- handle all json schema syntax. Generate appropriate json library annotations when possible and warn if not possible.
