package bps.ipr.prover.tableau

import bps.ipr.formulas.And
import bps.ipr.formulas.Equivalence
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or

sealed interface Rule<T : FolFormula<T>> {
    fun apply(signedFormula: SignedFormula<T>): List<TableauNode>

    /**
     * @param signedFormula the formula to be reduced
     * @param parents the nodes that will be the birthplaces of the child formulas
     */
    fun childFormulas(
        signedFormula: SignedFormula<T>,
        parents: List<TableauNode>,
    )
}

sealed interface AlphaRule<T : FolFormula<T>> : Rule<T> {
    /**
     * @param signedFormula the formula to be reduced.  Should be a [SignedFormula]<[T]>
     */
    fun reduce(
        signedFormula: SignedFormula<*>,
        parent: TableauNode,
    ): List<SignedFormula<*>>
}

fun List<SignedFormula<*>>.reduce(parent: TableauNode): List<SignedFormula<*>> =
    flatMap { signedFormula: SignedFormula<*> ->
        signedFormula.rule
            .let {
                when (it) {
                    is AlphaRule<*> -> it.reduce(signedFormula, parent)
                    else -> listOf(signedFormula)
                }
            }

    }

sealed interface NotRule : AlphaRule<Not> {
    override fun childFormulas(
        signedFormula: SignedFormula<Not>,
        parents: List<TableauNode>,
    ) =
        parents.forEach { parent: TableauNode ->
            listOf(SignedFormula.create(signedFormula.formula.subFormula, !signedFormula.sign, parent))
                // TODO take advantage of the fact that formulas are immutable
                //      make those first and copy references between the various SignedFormulas
                .flatMap { signedFormula ->
                    when (signedFormula.rule) {
                        is AlphaRule<*> -> signedFormula.rule.reduce(signedFormula, parent)
                        else -> listOf(signedFormula)
                    }
                }
                .forEach { parent.tableau.applicableRules.addRule(it.rule) }
        }
}

data object PositiveNotRule : NotRule {
    override fun apply(signedFormula: SignedFormula<Not>): List<TableauNode> {
        TODO("Not yet implemented")
    }
}

data object NegativeNotRule : NotRule {
    override fun apply(signedFormula: SignedFormula<Not>): List<TableauNode> {
        // TODO make a child with a Positive of the arg
        TODO("Not yet implemented")
    }
}

data object NegativeImpliesRule : AlphaRule<Implies> {
    override fun apply(signedFormula: SignedFormula<Implies>): List<TableauNode> {
        TODO("Not yet implemented")
    }

    override fun childFormulas(
        signedFormula: SignedFormula<Implies>,
        parents: List<TableauNode>,
    ) =
        listOf(
            SignedFormula(signedFormula.formula.antecedent, true, tableauNode),
            SignedFormula(signedFormula.formula.consequent, false, tableauNode),
        )
}

data object NegativeOrRule : AlphaRule<Or> {
    override fun apply(signedFormula: SignedFormula<Or>): List<TableauNode> {
        TODO("Not yet implemented")
    }

    override fun childFormulas(
        signedFormula: SignedFormula<Or>,
        parents: List<TableauNode>,
    ) =
        signedFormula.formula.subFormulas.map { SignedFormula(it, false, tableauNode) }
}

data object PositiveAndRule : AlphaRule<And> {
    override fun apply(signedFormula: SignedFormula<And>): List<TableauNode> {
        TODO("Not yet implemented")
    }

    override fun childFormulas(
        signedFormula: SignedFormula<And>,
        parents: List<TableauNode>,
    ) =
        signedFormula.formula.subFormulas.map { SignedFormula(it, true, tableauNode) }
}

sealed interface BetaRule<T : FolFormula<T>> : Rule<T>

data object PositiveOrRule : BetaRule<Or> {
    override fun apply(signedFormula: SignedFormula<Or>): List<TableauNode> = TODO()
    override fun childFormulas(
        signedFormula: SignedFormula<Or>,
        parents: List<TableauNode>,
    ) =
        signedFormula.formula.subFormulas.map { SignedFormula(it, true, tableauNode) }
}

data object PositiveImpliesRule : BetaRule<Implies> {
    override fun apply(signedFormula: SignedFormula<Implies>): List<TableauNode> = TODO()
    override fun childFormulas(
        signedFormula: SignedFormula<Implies>,
        parents: List<TableauNode>,
    ) =
        listOf(
            SignedFormula(signedFormula.formula.antecedent, false, tableauNode),
            SignedFormula(signedFormula.formula.consequent, true, tableauNode),
        )
}

data object NegativeAndRule : BetaRule<And> {
    override fun apply(signedFormula: SignedFormula<And>): List<TableauNode> = TODO()
    override fun childFormulas(
        signedFormula: SignedFormula<And>,
        parents: List<TableauNode>,
    ) =
        signedFormula.formula.subFormulas.map { SignedFormula(it, false, tableauNode) }
}

data object NegativeIffRule : BetaRule<Equivalence> {
    override fun apply(signedFormula: SignedFormula<Equivalence>): List<TableauNode> = TODO()
    override fun childFormulas(
        signedFormula: SignedFormula<Equivalence>,
        parents: List<TableauNode>,
    ) =

}

data object PositiveIffRule : BetaRule() {
    override fun apply(signedFormula: SignedFormula): List<TableauNode> = TODO()
}

sealed interface DeltaRule<T : FolFormula<T>> : Rule<T>

data object NegativeForAllRule : DeltaRule() {
    override fun apply(signedFormula: SignedFormula): List<TableauNode> = TODO()
}

data object PositiveForSomeRule : DeltaRule() {
    override fun apply(signedFormula: SignedFormula): List<TableauNode> = TODO()
}

sealed interface GammaRule<T : FolFormula<T>> : Rule<T>

data object NegativeForSomeRule : GammaRule() {
    override fun apply(signedFormula: SignedFormula): List<TableauNode> = TODO()
}

data object PositiveForAllRule : GammaRule() {
    override fun apply(signedFormula: SignedFormula): List<TableauNode> = TODO()
}

data object ClosingRule : Rule<FolFormula<*>> {
    override fun apply(signedFormula: SignedFormula<FolFormula<*>>): List<TableauNode> =
        emptyList()

    override fun childFormulas(
        signedFormula: SignedFormula<FolFormula<*>>,
        parents: List<TableauNode>,
    ) =
        emptyList()
}
