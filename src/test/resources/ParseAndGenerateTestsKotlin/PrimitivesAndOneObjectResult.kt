data class Work(
        val place: String,
        val starttime: String?
)

/**
 * The best person
 */
data class Person(
        /** The person's first name. */
        val firstName: String?,
        /** The person's last name. */
        val lastName: String,
        /** Age in years which must be equal to or greater than zero. */
        val age: Int,
        val work: Work
)

