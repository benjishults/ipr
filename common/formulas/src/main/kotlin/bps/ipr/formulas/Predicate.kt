package bps.ipr.formulas

import bps.ipr.terms.ArgumentList
import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

class Predicate(
    override val symbol: String,
    val arguments: ArgumentList,
) : FolFormula() {
    //    override val variablesBoundIn: Set<BoundVariable> = emptySet()
    override val variablesFreeIn: Set<Variable> =
        arguments
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ): Predicate =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaImplementation.predicateOrNull(
                symbol,
                arguments
                    .map {
                        it.apply(substitution, formulaImplementation.termImplementation)
                    },
            )!!
        else
            this

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append(
                "$symbol${
                    arguments
                        .map { it.display() }
                        .joinToString(", ", "(", ")") { it }
                }",
            )
        }

}
