package ru.schemator

enum class DataTypes {
    string, integer, double, obj, datetime, date
}

/** Can also generate  */
class GeneratableClass(
        val className: String,
        val properties: List<GeneratableProperty>
)

class GeneratableProperty(
        val propertyName: String,
        val propertyDataType: DataTypes,
        val isNullable: Boolean,
        val comment: String? = ""
)

// metadata is a list of generatable classes
class JsonSchemaMetadataOutput(
        val entries: List<GeneratableClass>
)

/** Create metedata by parsing json schema which can be used to generate code directly */
class MetadataReader(val jsonSchema: String, val launchArguments: LaunchArguments) {
    fun readSchema(): JsonSchemaMetadataOutput = TODO()
}

