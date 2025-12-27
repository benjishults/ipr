package bps.ipr.prover.tableau

import bps.ipr.common.Queue

class RuleSet {
    val alphaRules: Queue<AlphaFormula<*>> = Queue()
    val betaRules: Queue<BetaFormula<*>> = Queue()
    // TODO create a special structure to store GammaRules
    val gammaRules: Queue<GammaFormula<*>> = Queue()
    val deltaRules: Queue<DeltaFormula<*>> = Queue()
    val closingRules: Queue<ClosingFormula<*>> = Queue()

    fun addRule(signedFormula: SignedFormula<*>) =
        signedFormula
            .also {
                when (signedFormula) {
                    is AlphaFormula<*> -> alphaRules.enqueue(signedFormula)
                    is BetaFormula -> betaRules.enqueue(signedFormula)
                    is DeltaFormula -> deltaRules.enqueue(signedFormula)
                    is GammaFormula -> gammaRules.enqueue(signedFormula)
                    is ClosingFormula -> closingRules.enqueue(signedFormula)
                    is NegativeAtomicFormula -> Unit
                    is PositiveAtomicFormula -> Unit
                    is NegativeWastedFormula -> Unit
                    is PositiveWastedFormula -> Unit
                }
            }

    fun getNextRule(): SignedFormula<*>? =
        closingRules.dequeue()
            ?: alphaRules.dequeue()
            ?: deltaRules.dequeue()
            ?: betaRules.dequeue()
            ?: gammaRules.dequeue()

}
