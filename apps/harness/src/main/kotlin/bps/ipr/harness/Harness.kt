package bps.ipr.harness

import bps.console.app.MenuApplicationWithQuit
import bps.console.io.DefaultInputReader
import bps.console.io.DefaultOutPrinter
import bps.console.menu.Menu
import bps.console.menu.quitItem

fun main() {
//    java.awt.Toolkit.getDefaultToolkit().beep()
    MenuApplicationWithQuit(
        topLevelMenu = Menu({ "IPR!" }) {
//            add(
//                // TODO make this a pushMenu
//                pushMenu(
//                    label = { "select problem set" },
//                    shortcut = null,
//                    to = ,
//                ),
//            )
            add(
                quitItem("I shouldn't need to pass this in, should I?  It's basically ignored.")
            )

        },
        inputReader = DefaultInputReader,
        outPrinter = DefaultOutPrinter,
    )
        .use { it.runApplication() }
}

/*
IPR
1. Select formula set
2. Select theory
3. Select problem set
Select: 1

Select a formula set: # these will be files from /formulas
1. problems.ipr
2. slow-invalid.ipr
3. slow-valid.ipr
Select: 1

Select options for proving 'problems.ipr':
1. [ ] DAG unification
2. [ ] Breadth-first unification
3. [ ] ... (interactive, q-limit, etc.)
4. Take Action
Select: 4



Consider beeping if the proof finishes: java.awt.Toolkit.getDefaultToolkit().beep()


 */
