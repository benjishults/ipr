package bps.ipr.parser.tptp

import bps.ipr.parser.WhitespaceParser
import java.util.regex.Pattern

private val tptpWhitespacePattern = Pattern.compile("\\s*(?:%.*|/\\*(?:\\n|\\r|.)*?\\*/|\\s+)\\s*")

interface TptpWhitespaceParser : WhitespaceParser {

    override val whitespace: Pattern get() = tptpWhitespacePattern

    companion object : TptpWhitespaceParser

}
