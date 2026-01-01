package bps.ipr.terms

import bps.kotlin.allIndexed

/**
 * A [List] with equality defined by its members.
 *
 * These are not suitable hash-map keys or members of sets.
 */
class ArgumentList(arguments: Iterable<Term>) : Iterable<Term> by arguments {

    // NOTE this will perform badly if arguments is not a List or Set
    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is ArgumentList &&
                        other.count() == this.count() &&
                        other.allIndexed { i, t ->
                            t == elementAt(i)
                        })

    // NOTE explicitly calling super here to highlight that this is what we want
    override fun hashCode(): Int = super.hashCode()

}
