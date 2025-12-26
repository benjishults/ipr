package bps.ipr.parser.tptp

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Not
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.VariablesBindingFolFormula
import bps.ipr.parser.FolFormulaParser
import bps.ipr.terms.TermImplementation
import java.util.regex.Pattern

private val _tptpUpperAlpha = ('A'..'Z').toSet()
private val _tptpLowerAlpha = ('a'..'z').toSet() // predicates, propositions, functions, constants
private val _tptpNumeric = ('0'..'9').toSet()
private val _tptpAlphaNumeric = (_tptpUpperAlpha + _tptpLowerAlpha + _tptpNumeric).toSet()

//private val _delimiters = setOf(',', '(', ')', '[', ']')
private val _associativeConnective = setOf("|", "&")
private val _nonAssociativeConnectiveRegEx = setOf(
    Regex("<=>"),
    Regex("=>"),
    Regex("<="),
    Regex("<~>"),
    Regex("~\\|"),
    Regex("~&"),
)
private val _unaryConnective = setOf("~")
private val _infixInequality = setOf("!=")
private val _fofQuantifier = setOf("!", "?", "#")
private val _variableListMarker = setOf("[", "]")

/**
 * Parse according to https://tptp.org/UserDocs/TPTPLanguage/SyntaxBNF.html#fof_formula
 */
open class TptpFofFormulaParser(
    final override val formulaImplementation: FolFormulaImplementation = FolFormulaImplementation(),
    override val whitespaceParser: TptpWhitespaceParser = TptpWhitespaceParser,
    termParserFactory: (TermImplementation) -> TptpFofTermParser,
) : FolFormulaParser {

    override val termParser: TptpFofTermParser = termParserFactory(formulaImplementation.termImplementation)

    /**
     * Attempts to parse the string as a [FolFormula] and returns a pair containing the parsed formula
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a [FolFormula] and the index where parsing stopped, or null if parsing fails.
     */
    // NOTE just doing TPTP fof_logic_formula for now
    /*
     %----FOF formulae.
<fof_formula>          ::= <fof_logic_formula> // BPS excluding for now: | <fof_sequent>
<fof_logic_formula>    ::= <fof_binary_formula> | <fof_unary_formula> | <fof_unitary_formula>
<fof_binary_formula>   ::= <fof_binary_nonassoc> | <fof_binary_assoc>
%----Only some binary connectives are associative
%----There's no precedence among binary connectives
<fof_binary_nonassoc>  ::= <fof_unit_formula> <nonassoc_connective> <fof_unit_formula>
<fof_binary_assoc>     ::= <fof_or_formula> | <fof_and_formula>
<fof_or_formula>       ::= <fof_unit_formula> <vline> <fof_unit_formula> |
                           <fof_or_formula> <vline> <fof_unit_formula>
<fof_and_formula>      ::= <fof_unit_formula> & <fof_unit_formula> |
                           <fof_and_formula> & <fof_unit_formula>
<fof_unary_formula>    ::= <unary_connective> <fof_unit_formula> | <fof_infix_unary>
 <fof_infix_unary>      ::= <fof_term> <infix_inequality> <fof_term>
%----<fof_unitary_formula> are in ()s or do not have a connective
<fof_unit_formula>     ::= <fof_unitary_formula> | <fof_unary_formula>
<fof_unitary_formula>  ::= <fof_quantified_formula> | <fof_atomic_formula> | (<fof_logic_formula>)
 %----All variables must be quantified
<fof_quantified_formula> ::= <fof_quantifier> [<fof_variable_list>] : <fof_unit_formula>
<fof_variable_list>    ::= <variable> | <variable>,<fof_variable_list>
<fof_atomic_formula>   ::= <fof_plain_atomic_formula> | <fof_defined_atomic_formula> |
                           <fof_system_atomic_formula>
<fof_plain_atomic_formula> ::= <fof_plain_term>
<fof_plain_atomic_formula> :== <proposition> | <predicate>(<fof_arguments>)
 %----Arguments recurse back to terms (this is the FOF world here)
<fof_arguments>        ::= <fof_term> | <fof_term>,<fof_arguments>
 %----For all language types
<atom>                 ::= <untyped_atom> | <defined_constant>
<untyped_atom>         ::= <constant> | <system_constant>

<proposition>          :== <predicate>
<predicate>            :== <atomic_word> // BPS: this is a lower_word
     */
    /*
    so, what could I be looking at?
1. a functor (parse a predicate) ... but if the predicate is followed by one of many stuffs, then this would be a mistake, so ...
2. a
     */
    override fun String.parseFormulaOrNull(startIndex: Int): Pair<FolFormula<*>, Int>? = TODO()
    // parseBinaryFormula | parseUnaryFormula | parseUnitaryFormula

    fun String.parseBinaryFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()
    // parseBinaryNonAssoc | parseBinaryAssoc

    fun String.parseBinaryNonAssoc(): Pair<FolFormula<*>, Int>? = TODO()
    // unitFormula nonAssocConnective unitFormula

    fun String.parseUnitFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()
    // parseUnitaryFormula | parseUnaryFormula

    fun String.parseUnitaryFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()
    // parseQuantifiedFormula | parseAtomicFormula | ( parseFormulaOrNull )
    // if it starts with a '(' then parseFormulaOrNull then expect ')'

    fun String.parseQuantifiedFormulaOrNull(): Pair<VariablesBindingFolFormula<*>, Int>? = TODO()
    // fofQuantifier [variableList] : unitFormula

    fun String.parseAtomicFormulaOrNull(): Pair<Predicate, Int>? = TODO()
    // identical to parseTermOrNull except build a Predicate rather than a Term

    fun String.parseUnaryFormulaOrNull(): Pair<Not, Int>? = TODO()
    // not or an inequality
    // starts with '~'

    fun String.parseBinaryAssocOrNull(): Pair<AbstractMultiFolFormula<*>, Int>? = TODO()
    // parseOr | parseAnd

    /**
     * @return the pattern of the first found [Regex] and its first index or `null` if none are present.
     */
    fun String.firstOfOrNull(stopAtRegExp: Collection<Regex>): Pair<String, Int>? {
//        Pattern.compile(stopAtRegExp.joinToString("|"))
//            .matcher(this)
//            .let { matcher ->
//                if (matcher.find()) {
//                    val start = matcher.start()
//                    stopAtRegExp.find { regexp ->
//                        this.lookingAt(regexp)
//                    }
//                }
//
//            }
        return null
    }

}
