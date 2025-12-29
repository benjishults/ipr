package bps.ipr.prover.tableau

import bps.ipr.common.Queue
import bps.ipr.common.queue

class RuleSet {

    private var _qLimit: Int = 1
    val qLimit: Int = _qLimit

    val alphaRules: Queue<AlphaFormula<*>> = queue()
    val betaRules: Queue<BetaFormula<*>> = queue()

    // TODO create a special structure to store GammaRules
    val gammaRules: Queue<GammaFormula<*>> = queue()
    val deltaRules: Queue<DeltaFormula<*>> = queue()
    val closingRules: Queue<ClosingFormula<*>> = queue()

    val spentGammaRules: MutableList<GammaFormula<*>> = mutableListOf()

    fun incrementQLimit(by: Int = 1) {
        _qLimit += by
        spentGammaRules
            .forEach { gammaRules.enqueue(it) }
    }

    fun addRule(signedFormula: SignedFormula<*>) =
        signedFormula
            .also {
                when (signedFormula) {
                    is AlphaFormula<*> -> alphaRules.enqueue(signedFormula)
                    is BetaFormula -> betaRules.enqueue(signedFormula)
                    is DeltaFormula -> deltaRules.enqueue(signedFormula)
                    is GammaFormula ->
                        if (signedFormula.applications < qLimit)
                            gammaRules.enqueue(signedFormula)
                        else
                            spentGammaRules.add(signedFormula)
                    is ClosingFormula -> closingRules.enqueue(signedFormula)
                    is NegativeAtomicFormula -> Unit
                    is PositiveAtomicFormula -> Unit
                    is NegativeWastedFormula -> Unit
                    is PositiveWastedFormula -> Unit
                }
            }

    fun dequeueNextRuleOrNull(): SignedFormula<*>? =
        closingRules.dequeueOrNull()
            ?: alphaRules.dequeueOrNull()?.also { throw IllegalStateException("Should be impossible") }
            ?: deltaRules.dequeueOrNull()
            ?: betaRules.dequeueOrNull()
            ?: gammaRules.dequeueOrNull()

}
