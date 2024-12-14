package bps.ipr.terms

object TptpFofTermParser {

    val tptpUpperAlpha = ('A'..'Z').toSet()
    val tptpLowerAlpha = ('a'..'z').toSet()
    val tptpNumeric = ('0'..'9').toSet()
    val tptpAlphaNumeric = (tptpUpperAlpha + tptpLowerAlpha + tptpNumeric).toSet()

    val delimiters = setOf(',', '(', ')')
    val endingDelimiters = setOf(',', ')')

    fun String.parseTptpFofTermOrNull(): Term? =
        trim()
            .let { trimmedString ->
                if (trimmedString.isEmpty()) {
                    null
                } else {
                    trimmedString.parseTptpVariableOrNull()
                        ?: trimmedString.parseTptpFofFunctionTermOrNull()
                }
            }

    // NOTE not dealing with defined or system terms, yet
    fun String.parseTptpFofFunctionTermOrNull(): Term? =
        parseTptpFofPlainTermOrNull()

    /**
     * A TPTP FOF plain term is a constant or function
     */
    fun String.parseTptpFofPlainTermOrNull(): Term? =
        trim()
            .let { trimmedString ->
                trimmedString
                    .indexOf('(')
                    .let { firstOpenParen: Int ->
                        if (firstOpenParen == -1) {
                            trimmedString.parseTptpConstantOrNull()
                        } else {
                            if (!trimmedString.endsWith(')')) {
                                // NOT WELL FORMED TERM
                                null
                            } else {
                                trimmedString.substring(0, firstOpenParen)
                                    .parseTptpFunctorOrNull()
                                    ?.let { functor: String ->
                                        if (trimmedString.endsWith(')')) {
                                            trimmedString.substring(
                                                firstOpenParen + 1,
                                                trimmedString.length - 1,
                                            )
                                                .parseTptpFofArgumentsOrNull()
                                                ?.let { (args: List<Term>, delimiterAndIndex: DelimiterAndIndex?) ->
                                                    if (delimiterAndIndex !== null) {
                                                        // there shouldn't be anything extra at the end since we cut
                                                        // the closed paren out
                                                        null
                                                    } else
                                                        when (args.size) {
                                                            0 -> {
                                                                // TPTP doesn't allow no arguments within parens
                                                                null
                                                            }
                                                            else -> ProperFunction(
                                                                FunctionSymbol(functor),
                                                                args.size,
                                                                args,
                                                            )
                                                        }
                                                }
                                        } else {
                                            // not well-formed because there's some stuff present or absent at the end
                                            null
                                        }
                                    }
                            }
                        }
                    }
            }

    data class DelimiterAndIndex(
        /**
         * Must be one of ')' or ','
         */
        val delimiter: Char,
        /**
         * Must not be negative
         */
        val indexOfDelimiter: Int,
    ) {
        init {
            require(delimiter == ')' || delimiter == ',')
            require(indexOfDelimiter >= 0)
        }
    }

    /**
     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms, possibly followed by a closed
     * paren the other stuff.
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the [DelimiterAndIndex] with [DelimiterAndIndex.delimiter] equal to `')'` after the parsed input if any.  `null` indicates input was exhausted.
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     * @param depthInOriginal the number of unmatched closed parens expected after the last term is parsed.
     */
    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
    fun String.parseTptpFofArgumentsOrNull(depthInOriginal: Int = 0): Pair<List<Term>, DelimiterAndIndex?>? =
        parseTptpFofArgumentsOrNullHelper(depthInOriginal)
            ?.let { (list, delimiterAndIndex) ->
                list.toList() to delimiterAndIndex
            }

    /**
     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms,
     * possibly followed by a closed paren then other stuff.
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the [DelimiterAndIndex] with [DelimiterAndIndex.delimiter] equal to `')'` after the parsed input if any.
     *    `null` indicates input was exhausted.
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     * @param depthInOriginal the number of unmatched closed parens expected after the last term is parsed.
     */
    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
    private fun String.parseTptpFofArgumentsOrNullHelper(
        depthInOriginal: Int = 0,
        list: MutableList<Term> = mutableListOf(),
    ): Pair<MutableList<Term>, DelimiterAndIndex?>? =
        parseTptpFirstFofTermOrNull(depthInOriginal)
            ?.let { (firstTerm: Term, delimiterAndIndex: DelimiterAndIndex?) ->
                list.add(firstTerm)
                delimiterAndIndex
                    ?.let { (delimiter, indexOfDelimiter) ->
                        when (delimiter) {
                            ',' -> {
                                substring(indexOfDelimiter + 1)
                                    .parseTptpFofArgumentsOrNullHelper(depthInOriginal, list)
                                    ?.let { (_, internalDelimiterAndIndex) ->
                                        // fixme
                                        internalDelimiterAndIndex
                                            ?.let { (internalDelimiter, internalIndexOfDelimiter) ->
                                                // FIXME
                                                when (internalDelimiter) {
                                                    ',' -> {
                                                        // not possible
                                                        throw IllegalStateException()
                                                    }
                                                    ')' -> {
                                                        if (depthInOriginal == 0) {
                                                            null
                                                        } else {
                                                            list to DelimiterAndIndex(
                                                                internalDelimiter,
                                                                internalIndexOfDelimiter + (indexOfDelimiter + 1),
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        // impossible
                                                        throw IllegalStateException()
                                                    }
                                                }
                                            }
                                            ?: run {
                                                // exhausted input
                                                if (depthInOriginal == 0) {
                                                    // as expected
                                                    list to null
                                                } else {
                                                    // should not have done so
                                                    null
                                                }
                                            }
                                    }
                            }
                            ')' -> {
                                if (depthInOriginal == 0) {
                                    // should not have a closed paren here
                                    null
                                } else {
                                    list to delimiterAndIndex
                                }
                            }
                            else -> {
                                // not possible
                                throw IllegalStateException()
                            }
                        }
                    }
                    ?: run {
                        // exhausted input on first term
                        if (depthInOriginal == 0) {
                            // as expected
                            list to null
                        } else {
                            // should not have exhausted input
                            null
                        }
                    }
            }

    /**
     * @return the first found [Char] and its first index or `null` if none are present.
     */
    fun String.firstOfOrNull(stopAt: Collection<Char>): Pair<Char, Int>? {
        forEachIndexed { index: Int, char: Char ->
            if (char in stopAt) {
                return char to index
            }
        }
        return null
    }

    /**
     * @return the [Pair] of
     * 1. the first term and
     * 2. the [DelimiterAndIndex] within the receiver occurring after the parsed input if any.  `null` indicates input was exhausted.
     *
     * `null` if the receiver doesn't start with a TPTP FOF term possibly followed by a comma, or closed paren
     * and other stuff.
     * @param depthInOriginal the number of unmatched closed parens expected after the last term in the list.
     */
    private fun String.parseTptpFirstFofTermOrNull(depthInOriginal: Int): Pair<Term, DelimiterAndIndex?>? =
        firstOfOrNull(delimiters)
            ?.let { (firstDelimiter, indexOfFirstDelimiter) ->
                when (firstDelimiter) {
                    ',' -> {
                        // constant or variable
                        substring(0, indexOfFirstDelimiter)
                            .let { functor ->
                                (functor.parseTptpVariableOrNull() ?: functor.parseTptpConstantOrNull())
                                    ?.let { it to DelimiterAndIndex(firstDelimiter, indexOfFirstDelimiter) }
                            }
                    }
                    '(' -> {
                        // proper function
                        substring(0, indexOfFirstDelimiter)
                            .let { functorInput ->
                                functorInput
                                    .parseTptpFunctorOrNull()
                                    ?.let { functor ->
                                        val startIndexOfArgumentInput = indexOfFirstDelimiter + 1
                                        substring(startIndexOfArgumentInput)
                                            .let { argumentsInput ->
                                                argumentsInput
                                                    .parseTptpFofArgumentsOrNull(depthInOriginal + 1)
                                                    ?.let { (args: List<Term>, delimiterAndIndex: DelimiterAndIndex?) ->
                                                        delimiterAndIndex
                                                            ?.let { (internalDelimiter, internalIndexOfDelimiter) ->
                                                                // NOTE internalDelimiter == ')'
                                                                argumentsInput.substring(internalIndexOfDelimiter + 1)
                                                                    .let { remainingInputAfterCloseParen ->
                                                                        remainingInputAfterCloseParen
                                                                            .firstOfOrNull(endingDelimiters)
                                                                            ?.let { (afterFunctionDelimiter, indexOfAfterFunctionDelimiter) ->
                                                                                if (remainingInputAfterCloseParen
                                                                                        .substring(
                                                                                            0,
                                                                                            indexOfAfterFunctionDelimiter,
                                                                                        )
                                                                                        .isBlank()
                                                                                ) {
                                                                                    ProperFunction(
                                                                                        FunctionSymbol(functor),
                                                                                        args.size,
                                                                                        args,
                                                                                    ) to DelimiterAndIndex(
                                                                                        afterFunctionDelimiter,
                                                                                        startIndexOfArgumentInput + internalIndexOfDelimiter + 1 + indexOfAfterFunctionDelimiter,
                                                                                    )
                                                                                } else {
                                                                                    null
                                                                                }
                                                                            }
                                                                            ?: (
                                                                                    ProperFunction(
                                                                                        FunctionSymbol(functor),
                                                                                        args.size,
                                                                                        args,
                                                                                    ) to null
                                                                                    )
                                                                    }
                                                            }
                                                            ?: run {
                                                                // should not exhaust input while parsing internal arguments
                                                                null
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
                    ')' -> {
                        if (depthInOriginal == 0) {
                            null
                        } else {
                            // constant or variable
                            substring(0, indexOfFirstDelimiter)
                                .let { functor ->
                                    (functor.parseTptpVariableOrNull() ?: functor.parseTptpConstantOrNull())
                                        ?.let { it to DelimiterAndIndex(firstDelimiter, indexOfFirstDelimiter) }
                                }
                        }
                    }
                    else -> {
                        // should be impossible
                        throw IllegalStateException()
                    }
                }
            }
            ?: (parseTptpVariableOrNull() ?: parseTptpConstantOrNull())
                ?.let { it to null }

    fun String.isTptpLowerWord(): Boolean =
        isNotEmpty() &&
                get(0) in tptpLowerAlpha &&
                substring(1)
                    .all { it in tptpAlphaNumeric }

    fun String.isTptpUpperWord(): Boolean =
        isNotEmpty() &&
                get(0) in tptpUpperAlpha &&
                substring(1)
                    .all { it in tptpAlphaNumeric }

    fun String.isTptpAtomicWord(): Boolean = isTptpLowerWord()
    fun String.isTptpFunctor(): Boolean = isTptpAtomicWord()
    fun String.isTptpConstant(): Boolean = isTptpFunctor()

    fun String.isTptpVariable(): Boolean =
        isNotEmpty() &&
                get(0) in tptpUpperAlpha &&
                substring(1)
                    .all { it in tptpAlphaNumeric }

//    fun String.isTptpFofFunctionTerm() =

//    fun String.isTptpFofTerm() = isTptpVariable() || isTptpFofFunctionTerm()

    fun String.parseTptpConstantOrNull(): Constant? =
        trim()
            .takeIf { it.isTptpLowerWord() }
            ?.let { Constant(it) }

    fun String.parseTptpVariableOrNull(): Variable? =
        trim()
            .takeIf { it.isTptpUpperWord() }
            ?.let { FreeVariable(it) }

    // NOTE should check that it's a lower word
    fun String.parseTptpFunctorOrNull(): String? =
        trim()
            .takeIf { it.isTptpLowerWord() }

}
