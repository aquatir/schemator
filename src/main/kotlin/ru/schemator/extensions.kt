package ru.schemator

import com.google.gson.JsonElement
import com.google.gson.JsonObject


fun JsonObject.type(): String = this.getAsJsonPrimitive(Schema.type).asString

fun JsonObject.titleNullable(): String? = this.getAsJsonPrimitive(Schema.title)?.asString
fun JsonObject.descriptionNullable(): String? = this.getAsJsonPrimitive(Schema.description)?.asString

fun JsonObject.items(): JsonObject = this.get(Schema.items).asJsonObject
fun JsonObject.properties(): JsonElement = this.get(Schema.properties)

fun JsonObject.requiredNullable(): JsonElement? = this.get(Schema.required)
