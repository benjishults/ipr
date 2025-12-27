package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula

class Tableau(
//    formula: FolFormula<*>,
) {

    var root: TableauNode? = null
    val applicableRules: RuleSet = RuleSet()

//    init {
//        root = TableauNode(this)
//            .also { root ->
//                SignedFormula(
//                    formula = formula as FolFormula<*>,
//                    sign = false,
//                    birthPlace = root,
//                )
//                    .let { signedFormula: SignedFormula<*> ->
//                        applicableRules.addRule(signedFormula.rule)
//                        root.newGoals.add(signedFormula)
//                    }
//            }
//    }

    companion object {
        // NOTE had to do this outside a constructor because I have to have the generic function
        operator fun <T : FolFormula<T>> invoke(formula: T): Tableau {
            return Tableau()
                .apply {
                    root = TableauNode(this)
                        .also { root ->
                            SignedFormula.create(
                                formula = formula,
                                sign = false,
                                birthPlace = root,
                            )
                                .let { signedFormula: SignedFormula<*> ->
                                    applicableRules.addRule(signedFormula.rule)
                                    root.newGoals.add(signedFormula)
                                }
                        }

                }
        }
    }

}
