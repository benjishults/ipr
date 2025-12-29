package bps.ipr.parser.ipr

import bps.ipr.parser.WhitespaceParser
import java.util.regex.Pattern

// "\\s*(?:%.*|/\\*(?:\\n|\\r|.)*?\\*/|\\s+)\\s*"
private val iprWhitespacePattern = Pattern.compile("\\s*(?:;.*|/\\*(?:\\n|\\r|.)*?\\*/|\\s+)\\s*")

interface IprWhitespaceParser : WhitespaceParser {

    override val whitespace: Pattern get() = iprWhitespacePattern

    companion object : IprWhitespaceParser

}
