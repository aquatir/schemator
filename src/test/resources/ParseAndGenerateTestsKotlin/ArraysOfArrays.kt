/**
 * The best person
 */
data class Person(
        val work: Work?
)
data class Work(
        val arrayOfArrays: List<List<List<InternalObject>>>
)
data class InternalObject(
        val prop: String?
)
