import java.lang.RuntimeException

class NotJsonSchemaException(str: String): RuntimeException(str)

class SchemaValidationException(str: String): RuntimeException(str)
