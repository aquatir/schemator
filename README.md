### Schemator

**NOTICE:** This is still work in progress!

Your ultimate tool for generating code from json schemas. 

Supported languages: Limited Kotlin support.

Supported functionality:
- Schemas with primitives, objects and arrays. No array nesting. 

#### TODOs:

- schema into metadata parser:
    - add `$ref` handling
    - put arrays in the same place in generated classes as where they were in parsed schema
    - support enums
    - array type inside another array
    - graceful exception handling for wrong schema names/types/etc
- metadata into classes:
    - validate schema before work
- quality of codebase:
    - metadata for properties should be sealed classes
- parse program command line parameters on startup
- logging, especial exceptional logging 
- remember specific types while parsing metadata which require `import` headers for generated classes
- add support for Json libraries annotations
- make testing more intuitive. E.g. maybe try class-loading created class to make sure it is correct for kotlin
- create conscious documentation for usage
- add javadocs everywhere
- create contribution guide


Long plans: 
- generify to make adding other languages apart from kotlin much easier.
- handle all json schema syntax. Generate appropriate json library annotations when possible and warn if not possible.


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
