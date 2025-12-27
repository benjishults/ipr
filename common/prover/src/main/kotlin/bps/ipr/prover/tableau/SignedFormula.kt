package bps.ipr.prover.tableau

import bps.ipr.formulas.And
import bps.ipr.formulas.Equivalence
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth

class SignedFormula<T : FolFormula<T>> private constructor(
    val formula: T,
    val sign: Boolean,
     val birthPlace: TableauNode,
) {

/*
    lateinit var formula: T
        private set
    var sign: Boolean? = null
        private set
    lateinit var birthPlace: TableauNode
        private set
*/

//    private fun validate() {
//        check(this::formula.isInitialized) { "formula must be initialized" }
//        check(this.sign !== null) { "sign must be initialized" }
//        check(this::birthPlace.isInitialized) { "birthPlace must be initialized" }
//    }

    val rule: Rule<*>? =
        if (sign) {
            when (formula) {
                is And -> PositiveAndRule
                is Or -> PositiveOrRule
                is Implies -> PositiveImpliesRule
                is Equivalence -> PositiveIffRule
                is ForAll -> PositiveForAllRule
                is ForSome -> PositiveForSomeRule
                is Not -> PositiveNotRule
                Falsity -> ClosingRule
                is Predicate -> null
                Truth -> null
            }
        } else {
            when (formula) {
                is And -> NegativeAndRule
                is Or -> NegativeOrRule
                is Implies -> NegativeImpliesRule
                is Equivalence -> NegativeIffRule
                is ForAll -> NegativeForAllRule
                is ForSome -> NegativeForSomeRule
                is Not -> NegativeNotRule
                Truth -> ClosingRule
                is Predicate -> null
                Falsity -> null
            }
        }

    companion object {
        fun <F : FolFormula<F>> create(
            formula: F,
            sign: Boolean,
            birthPlace: TableauNode,
        ): SignedFormula<F> =
            SignedFormula(formula, sign, birthPlace)
    }
//    companion object {
//        fun <T : FolFormula<T>> create(
//            formula: T,
//            sign: Boolean,
//            birthPlace: TableauNode,
//        ): SignedFormula<T> =
//            SignedFormula<T>()
//                .apply {
//                    this.formula = formula
//                    this.sign = sign
//                    this.birthPlace = birthPlace
//                }
//    }

}
