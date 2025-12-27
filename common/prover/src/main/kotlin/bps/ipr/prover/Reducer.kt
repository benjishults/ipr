package bps.ipr.prover

//import bps.ipr.formulas.FolFormula
//import bps.ipr.formulas.Implies
//import bps.ipr.formulas.Not
//import bps.ipr.formulas.Or
//import bps.ipr.prover.tableau.SignedFormula
//
//interface Reducer {
//    fun reduce(formula: FolFormula<*>): List<SignedFormula> =
//        when (formula) {
//            is Or -> formula.subFormulas.flatMap { reduce(it) }
//            is Implies -> reduce(formula.antecedent) + reduce(formula.consequent)
//            is Not -> reduce(formula.subFormula).map { SignedFormula(it.formula, !it.sign) }
//            else -> listOf(SignedFormula(formula, false))
//        }
//}
