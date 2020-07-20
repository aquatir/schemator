### Schemator

**NOTICE:** This is still work in progress!

Your ultimate tool for generating code from json schemas. 

Supported languages: **None!** But working on Kotlin support. Can generate primitive schemas with no nesting for now. 


TODOs:

- json schema parser into metadata
    - add recursive object read
    - add arrays handling
- parse program command line parameters on startup
- add logging 
- remember specific types while parsing metadata which require `import` headers for generated classes
- add support for Json libraries annotations
- handle 'object' type in metadata reader

Long plans: 
- generify to support other languages
- generify some more to support other schema tools 
