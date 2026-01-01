package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.VariablesBindingFolFormula
import bps.ipr.prover.tableau.TableauNode
import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.substitution.SingletonIdempotentSubstitution
import bps.ipr.terms.FolTermImplementation
import bps.ipr.terms.Term
import bps.ipr.terms.Variable
import kotlin.collections.forEach

sealed interface DeltaFormula<T : VariablesBindingFolFormula> : SignedFormula<T> {
    override fun apply() =
        // NOTE we want a single new skolem function???
        createDeltaChildFormula()
            .let { childFormula: FolFormula ->
                birthPlace
                    .leaves()
                    .forEach { leaf: TableauNode ->
                        leaf.setChildren(
                            listOf(
                                createNodeForReducedFormulas { node: TableauNode ->
                                    SignedFormula.create(childFormula, sign, node, formulaImplementation)
                                        .reduceAlpha(node)
                                },
                            ),
                        )
                    }
            }

    fun createDeltaChildFormula(): FolFormula =
        with(formulaImplementation.termImplementation) {
            formula
                .variablesFreeIn
                .let { freeVariables: Set<Variable> ->
                    formula
                        .boundVariables
                        .firstOrNull()!!
                        .let { firstBv: Variable ->
                            createSubstitutionForBoundVariables(firstBv, freeVariables)
                                .let { substitution: IdempotentSubstitution ->
                                    // substitution substitutes the bound variables with skolem functions
                                    formula
                                        .subFormula
                                        .apply(substitution, formulaImplementation)
                                }
                        }
                }
        }

    private fun FolTermImplementation.createSubstitutionForBoundVariables(
        firstBv: Variable,
        freeVariables: Set<Variable>,
    ): IdempotentSubstitution =
        formula
            .boundVariables
            .asSequence()
            .drop(1)
            .fold(
                SingletonIdempotentSubstitution(
                    key = firstBv,
                    value = newSkolemTermFor(firstBv.symbol, freeVariables),
                ),
            ) { subst: IdempotentSubstitution, bv: Variable ->
                subst.composeIdempotent(
                    theta = SingletonIdempotentSubstitution(
                        key = bv,
                        value = newSkolemTermFor(bv.symbol, freeVariables)
                    ),
                    termImplementation = this,
                )
            }

    private fun FolTermImplementation.newSkolemTermFor(symbol: String, freeVariables: Set<Variable>): Term =
        newFunctorForSymbol(symbol, freeVariables.size)
            .let { functor ->
                if (freeVariables.isEmpty()) {
                    constantForSymbol(functor.symbol)
                } else
                    properFunction(
                        functor = functor,
                        arguments = freeVariables,
                    )
            }

}

data class NegativeForAllFormula(
    override val formula: ForAll,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : DeltaFormula<ForAll>, NegativeSignedFormula<ForAll>()

data class PositiveForSomeFormula(
    override val formula: ForSome,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : DeltaFormula<ForSome>, PositiveSignedFormula<ForSome>()
