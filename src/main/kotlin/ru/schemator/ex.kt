import java.lang.RuntimeException

class NotJsonSchemaException(str: String): RuntimeException(str)

class NotActionableSchemaException(str: String): RuntimeException(str)
