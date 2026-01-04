package bps.ipr.harness.formula

//const val numberOfFilesInResourcesFormulas = 3
//val filesInResourcesFormulas = listOf("problems.ipr", "slow-invalid.ipr", "slow-valid.ipr")
//
//class FormulaFileSelectionMenu: ScrollingSelectionMenu<String>(
//    header = { "" },
//    limit = numberOfFilesInResourcesFormulas,
//    baseList = filesInResourcesFormulas,
////    labelGenerator = { this },
//    actOnSelectedItem = { menuSession: MenuSession, formulaFileName: String ->
//        menuSession.push(
//            Menu(
//                header = { TODO() },
//                prompt = TODO(),
//                items = TODO()
//            ),
//        )
//    },
//    ) {
//}

//ScrollingSelectionMenu(
//header = { "Select a credit card" },
//limit = userConfig.numberOfItemsInScrollingList,
//baseList = budgetData.chargeAccounts,
//// TODO do we want to incorporate credit limits to determine the balance and max
//labelGenerator = { String.format("%,10.2f | %s", balance, name) },
//) { menuSession, chargeAccount: ChargeAccount ->
//    menuSession.push(
//        Menu {
//            add(
//                // NOTE these might ought to be takeActionAndPush.  The intermediateAction could collect the initial data and pass it on.
//                takeAction({ "Record spending on '${chargeAccount.name}'" }) {
//                    spendOnACreditCard(
//                        budgetData,
//                        clock,
//                        transactionDao,
//                        accountDao,
//                        userConfig,
//                        menuSession,
//                        chargeAccount,
//                    )
//                },
//            )
//            add(
//                // NOTE these might ought to be takeActionAndPush.  The intermediateAction could collect the initial data and pass it on.
//                takeAction({ "Pay '${chargeAccount.name}' bill" }) {
//                    payCreditCardBill(
//                        menuSession,
//                        userConfig,
//                        budgetData,
//                        clock,
//                        chargeAccount,
//                        transactionDao,
//                        accountDao,
//                    )
//                },
//            )
//            add(
//                pushMenu({ "View unpaid transactions on '${chargeAccount.name}'" }) {
//                    ViewTransactionsWithoutBalancesMenu(
//                        account = chargeAccount,
//                        transactionDao = transactionDao,
//                        budgetId = budgetData.id,
//                        accountIdToAccountMap = { budgetData.getAccountByIdOrNull(it) },
//                        timeZone = budgetData.timeZone,
//                        limit = userConfig.numberOfItemsInScrollingList,
//                        filter = { it.draftStatus === DraftStatus.outstanding.name },
//                        header = { "Unpaid transactions on '${chargeAccount.name}'" },
//                        prompt = { "Select transaction to view details: " },
//                        outPrinter = outPrinter,
//                        extraItems = listOf(), // TODO toggle cleared/outstanding
//                    ) { _, extendedTransactionItem: AccountTransactionEntity ->
//                        with(ViewTransactionFixture) {
//                            outPrinter.verticalSpace()
//                            outPrinter.showTransactionDetailsAction(
//                                transactionDao.getTransactionOrNull(
//                                    extendedTransactionItem.transactionId,
//                                    extendedTransactionItem.budgetId,
//                                )!!,
//                                budgetData.timeZone,
//                            ) { budgetData.getAccountByIdOrNull(it) }
//                        }
//                    }
//                },
//            )
//            add(backItem)
//            add(budgetQuitItem)
//        },
//    )
//}
