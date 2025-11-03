package bps.ipr.formulas

import bps.ipr.terms.FreeVariable
import bps.ipr.terms.Variable

abstract class VariablesBindingFolFormula<out F : VariablesBindingFolFormula<F>>
/**
 * @throws IllegalArgumentException if [boundVariables] is empty or any of the [Variable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    val boundVariables: List<Variable>,
    val subFormula: FolFormula<*>,
) : FolFormula<F> {

    init {
        require(boundVariables.isNotEmpty())
//        val freeVariablesInSubFormula =
//            subFormula.variablesFreeIn
//                .filterIsInstance<FreeVariable>()
//                .map { it.display() }
//        require(
//            boundVariables
//                .map { it.display() }
//                .find { it in freeVariablesInSubFormula }
//                    == null,
//        )
        require(boundVariables.all { it in subFormula.variablesFreeIn })
    }

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormula.variablesBoundIn + boundVariables

    abstract val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> F

    override val variablesFreeIn: Set<Variable> =
//        boundVariables
//            .map { it.display() }
//            .let { boundVariableDisplays ->
        subFormula.variablesFreeIn -
                boundVariables
//                            .filterIsInstance<BoundVariable>()
//                            .filter { it.display() in boundVariableDisplays }
                    .toSet()
//            }

    override fun display(): String =
        boundVariables
            .joinToString(
                ", ",
                "($symbol (",
                ") ${subFormula.display()})",
            ) { it.display() }

}

class ForAll
/**
 * @throws IllegalArgumentException if any of the [BoundVariable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [BoundVariable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    boundVariables: List<Variable>,
    subFormula: FolFormula<*>,
) : VariablesBindingFolFormula<ForAll>(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> ForAll =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forAllOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "FORALL"

}

class ForSome
/**
 * @throws IllegalArgumentException if any of the [Variable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [Variable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    boundVariables: List<Variable>,
    subFormula: FolFormula<*>,
) : VariablesBindingFolFormula<ForSome>(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> ForSome =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forSomeOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "FORSOME"

}
