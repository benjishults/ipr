package bps.ipr.formulas

import bps.ipr.terms.ArgumentList
import bps.ipr.terms.Substitution
import bps.ipr.terms.Variable

class Predicate(
    override val symbol: String,
    val arguments: ArgumentList,
) : FolFormula {
    //    override val variablesBoundIn: Set<BoundVariable> = emptySet()
    override val variablesFreeIn: Set<Variable> =
        arguments
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): Predicate =
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

    override fun display(): String {
        return "$symbol${
            arguments
                .map { it.display() }
                .joinToString(", ", "(", ")") { it }
        }"
    }

}
