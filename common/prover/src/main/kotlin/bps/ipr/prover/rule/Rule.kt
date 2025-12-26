package bps.ipr.prover.rule

import bps.ipr.prover.SignedFormula
import bps.ipr.prover.Tableau
import bps.ipr.prover.TableauNode

sealed class Rule {
   abstract fun apply(signedFormula: SignedFormula, node: TableauNode, tableau: Tableau): Unit
}

sealed class AlphaRule : Rule()

data object PositiveNotRule: AlphaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object NegativeNotRule: AlphaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object NegativeImpliesRule: AlphaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object NegativeOrRule: AlphaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object PositiveAndRule: AlphaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

sealed class BetaRule : Rule()

data object PositiveOrRule: BetaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {}
}

data object PositiveImpliesRule: BetaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {}
}

data object NegativeAndRule: BetaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {}
}

data object NegativeIffRule: BetaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {}
}

data object PositiveIffRule: BetaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {}
}

sealed class DeltaRule : Rule()

data object NegativeForAllRule: DeltaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object PositiveForSomeRule: DeltaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

sealed class GammaRule : Rule()

data object NegativeForSomeRule: GammaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object PositiveForAllRule: GammaRule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}

data object ClosingRule : Rule() {
    override fun apply(
        signedFormula: SignedFormula,
        node: TableauNode,
        tableau: Tableau,
    ) {
        TODO("Not yet implemented")
    }
}
