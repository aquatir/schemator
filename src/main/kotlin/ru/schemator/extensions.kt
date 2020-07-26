package ru.schemator

import com.google.gson.JsonElement

fun JsonElement.asPrimitiveString() = this.asJsonPrimitive.asString
