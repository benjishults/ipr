package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

sealed class VariablesBindingFolFormula
/**
 * @throws IllegalArgumentException if [boundVariables] is empty or any of the [Variable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    val boundVariables: List<Variable>,
    val subFormula: FolFormula,
) : FolFormula() {

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
//        require(
//            boundVariables.all { bv ->
//                (bv in subFormula.variablesFreeIn)
//                    .also {
//                        if (!it)
//                            println("bound variable $bv not in subformula $subFormula with free variables ${subFormula.variablesFreeIn}")
//                    }
//            },
//        )
        // NOTE print warning about un-binding bound variable
        boundVariables.all { bv ->
            (bv in subFormula.variablesFreeIn) ||
                    false
                        .also {
                            println("bound variable $bv not in subformula $subFormula with free variables ${subFormula.variablesFreeIn}")
                        }
        }
    }

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormula.variablesBoundIn + boundVariables

    abstract val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula) -> VariablesBindingFolFormula

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

    // NOTE FOL language rules applied on formula creation do not permit the bound variables to occur anywhere
    //      outside this sub-formula.  Thus, unless someone is maliciously creating a substitution to screw things
    //      up, there's no way bound variable here can occur in the domain or range of an "outside" substitution.
    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ): VariablesBindingFolFormula =
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaConstructor(
                formulaImplementation,
                boundVariables,
                subFormula.apply(substitution, formulaImplementation),
            )
        else
            this

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append(
                boundVariables
                    .joinToString(
                        separator = " ",
                        prefix = "($symbol (",
                        postfix = ") ${subFormula.display(0)})",
                    ) { it.display() },
            )
        }

}

class ForAll(
    boundVariables: List<Variable>,
    subFormula: FolFormula,
) : VariablesBindingFolFormula(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula) -> ForAll =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forAllOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "FORALL"

}

class ForSome(
    boundVariables: List<Variable>,
    subFormula: FolFormula,
) : VariablesBindingFolFormula(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula) -> ForSome =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forSomeOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "EXISTS"

}
