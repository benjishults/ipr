package bps.ipr.terms

/**
 * A [List] with equality defined by its members.
 *
 * These are not suitable hash-map keys or members of sets.
 */
class ArgumentList(arguments: List<Term>) : List<Term> by arguments {

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is ArgumentList &&
                        other.size == this.size &&
                        // kotlin doesn't seem to have an allIndexed function.
                        other.foldIndexed(true) { i, _, t ->
                            if (t != get(i))
                                return false
                            else
                                true
                        })

    // NOTE explicitly calling super here to highlight that this is what we want
    override fun hashCode(): Int = super.hashCode()

}
