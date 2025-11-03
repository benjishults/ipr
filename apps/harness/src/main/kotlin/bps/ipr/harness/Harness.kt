package bps.ipr.harness

import bps.console.app.MenuApplicationWithQuit
import bps.console.io.DefaultInputReader
import bps.console.io.DefaultOutPrinter
import bps.console.menu.Menu
import bps.console.menu.NoopIntermediateAction
import bps.console.menu.quitItem
import bps.console.menu.takeActionAndPush

fun main() {
    MenuApplicationWithQuit(
        topLevelMenu = Menu({ "IPR!" }) {
            add(
                // TODO make this a pushMenu
                takeActionAndPush(
                    label = { "select problem set" },
                    shortcut = null,
                    intermediateAction = NoopIntermediateAction,
                ),
            )
            add(
                quitItem(
                    """
                        |Quitting
                    """.trimMargin(),
                ),
            )

        },
        inputReader = DefaultInputReader,
        outPrinter = DefaultOutPrinter,
    )
        .use { it.runApplication() }
}
