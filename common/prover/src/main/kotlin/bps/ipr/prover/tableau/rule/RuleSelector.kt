package bps.ipr.prover.tableau.rule

import bps.ipr.common.ImpossibleError
import bps.ipr.common.Queue
import bps.ipr.common.queue

interface RuleSelector {

    /**
     * register a rule as applicable
     */
    fun addRule(rule: Rule)

    fun addRuleAddedListener(listener: RuleAddedListener)

    /**
     * selects what is deemed to be the best rule to apply at the moment
     */
    fun dequeueNextRuleOrNull(): Rule?

    fun addRuleDequeueListener(listener: RuleDequeueListener)

}

fun interface RuleAddedListener {
    fun onRuleAdded(rule: Rule)
}

fun interface RuleDequeueListener {
    fun onRuleDequeued(rule: Rule)
}

/**
 * Returns rules in order: closing, alpha, delta, beta, gamma.
 *
 * The gamma rules are returned in the order they were added.  It does not prioritize gamma rules according to
 * how often they have been applied.  Of course, it will not give a gamma rule that has already been applied
 * [qLimit] times.
 *
 * Calling [incrementQLimit] increases the [qLimit].  After calling this, [gammaRules] that had been added after
 * reaching the previous [qLimit] will become available for [dequeueNextRuleOrNull] to return.
 */
open class FolRuleSelector(
    initialQLimit: Int = 1,
) : RuleSelector {

    private val ruleAddedListeners: MutableList<RuleAddedListener> = mutableListOf()
    private val ruleDequeueListeners: MutableList<RuleDequeueListener> = mutableListOf()
    private var _qLimit: Int = initialQLimit
    val qLimit: Int
        get() = _qLimit

    //    val alphaRules: Queue<AlphaFormula<*>> = queue()
    val betaRules: Queue<BetaFormula<*>> = queue()

    val gammaRules: Queue<GammaFormula<*>> = queue()
    val deltaRules: Queue<DeltaFormula<*>> = queue()
    val closingRules: Queue<ClosingFormula<*>> = queue()

    val spentGammaRules: MutableList<GammaFormula<*>> = mutableListOf()

    override fun addRuleAddedListener(listener: RuleAddedListener) {
        ruleAddedListeners.add(listener)
    }

    override fun addRuleDequeueListener(listener: RuleDequeueListener) {
        ruleDequeueListeners.add(listener)
    }

    private fun notifyRuleAdded(rule: Rule) =
        ruleAddedListeners.forEach {
            try {
                it.onRuleAdded(rule)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun notifyRuleDequeued(rule: Rule) =
        ruleDequeueListeners.forEach {
            try {
                it.onRuleDequeued(rule)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    /**
     * This increases the [qLimit] by [by].  After calling this, [gammaRules] that had been added after reaching the
     * previous [qLimit] will become available for [dequeueNextRuleOrNull] to return.
     */
    fun incrementQLimit(by: Int = 1) {
        require(by > 0) { "Cannot increment QLimit by a negative amount" }
        _qLimit += by
        spentGammaRules
            .forEach { gammaRules.enqueue(it) }
    }

    override fun addRule(rule: Rule) {
        when (rule) {
            is SignedFormula<*> ->
                when (rule) {
                    is AlphaFormula<*> -> throw ImpossibleError("Should be impossible")
                    is BetaFormula<*> -> betaRules.enqueue(rule)
                    is DeltaFormula<*> -> deltaRules.enqueue(rule)
                    is GammaFormula<*> ->
                        if (rule.applications < qLimit)
                            gammaRules.enqueue(rule)
                        else
                            spentGammaRules.add(rule)
                    is ClosingFormula<*> -> closingRules.enqueue(rule)
                    is NegativeAtomicFormula -> Unit
                    is PositiveAtomicFormula -> Unit
                    is NegativeWastedFormula -> Unit
                    is PositiveWastedFormula -> Unit
                }
            else -> throw IllegalArgumentException("This rule selector only accepts SignedFormulas: $rule")
        }
        notifyRuleAdded(rule)
    }

    override fun dequeueNextRuleOrNull(): SignedFormula<*>? =
        (closingRules.dequeueOrNull()
//            ?: alphaRules.dequeueOrNull()
//                ?.also { throw ImpossibleError("Should be impossible") }
            ?: deltaRules.dequeueOrNull()
            ?: betaRules.dequeueOrNull()
            ?: gammaRules.dequeueOrNull()
                )
            ?.also { notifyRuleDequeued(it) }
}
