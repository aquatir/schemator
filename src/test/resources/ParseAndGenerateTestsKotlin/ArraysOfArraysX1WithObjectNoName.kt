/**
 * The best person
 */
data class Person(
        val work: Work?
)
data class Work(
        val arrayOfArrays: List<ArrayOfArraysItem>
)
data class ArrayOfArraysItem(
        val prop: String?
)
