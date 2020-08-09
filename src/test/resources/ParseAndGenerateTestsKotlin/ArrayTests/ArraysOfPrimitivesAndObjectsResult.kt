/**
 * The best person
 */
data class Person(
        val work: Work?
)
data class Work(
        val stuff: List<String>?,
        val stuffArray: List<StuffArrayItem>?
)
data class StuffArrayItem(
        val top: String?,
        val test: Int?
)
