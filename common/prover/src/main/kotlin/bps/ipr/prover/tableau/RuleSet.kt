package bps.ipr.prover.tableau

import bps.ipr.common.Queue

class RuleSet {
    val alphaRules: Queue<AlphaRule<*>> = Queue()
    val betaRules: Queue<BetaRule<*>> = Queue()
    // TODO create a special structure to store GammaRules
    val gammaRules: Queue<GammaRule<*>> = Queue()
    val deltaRules: Queue<DeltaRule<*>> = Queue()
    val closingRules: Queue<ClosingRule> = Queue()

    fun addRule(rule: Rule<*>?) =
        rule
            .also {
                when (rule) {
                    is AlphaRule -> alphaRules.enqueue(rule)
                    is BetaRule -> betaRules.enqueue(rule)
                    is DeltaRule -> deltaRules.enqueue(rule)
                    is GammaRule -> gammaRules.enqueue(rule)
                    is ClosingRule -> closingRules.enqueue(rule)
                    null -> Unit
                }
            }

    fun getNextRule(): Rule<*>? =
        closingRules.dequeue()
            ?: alphaRules.dequeue()
            ?: deltaRules.dequeue()
            ?: betaRules.dequeue()
            ?: gammaRules.dequeue()

}
