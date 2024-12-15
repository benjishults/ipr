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
                    .let { indexOfOpenParen: Int ->
                        if (indexOfOpenParen == -1) {
                            trimmedString.parseTptpConstantOrNull()
                        } else {
                            if (!trimmedString.endsWith(')')) {
                                // short circuit
                                // NOT WELL FORMED TERM
                                null
                            } else {
                                trimmedString.substring(0, indexOfOpenParen)
                                    .parseTptpFunctorOrNull()
                                    ?.let { functor: String ->
                                        val startIndexOfArgumentInput = indexOfOpenParen + 1
                                        trimmedString.substring(startIndexOfArgumentInput)
                                            .let { argumentsInput: String ->
                                                argumentsInput
                                                    .parseTptpFofArgumentsOrNull()
                                                    ?.let { (args: List<Term>, closeParenAndIndex: DelimiterAndIndex?) ->
                                                        if (closeParenAndIndex !== null) {
                                                            // there should have been a trailing closed paren
                                                            determineWhatHappensAfterThatCloseParen(
                                                                indexOfCloseParenWithinArgumentInput = closeParenAndIndex.indexOfDelimiter,
                                                                argumentsInput = argumentsInput,
                                                                functor = functor,
                                                                parsedArguments = args,
                                                                externalStartIndexOfArgumentInput = startIndexOfArgumentInput,
                                                            )
                                                                ?.let { (term, nextDelimiterAndIndex) ->
                                                                    term.takeIf { nextDelimiterAndIndex === null }
                                                                }
                                                        } else {
                                                            // we should not have exhausted the input as there should have
                                                            // been a closed paren at the end
                                                            null
                                                        }
                                                    }
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
     * 2. the [DelimiterAndIndex] with [DelimiterAndIndex.delimiter] equal to `')'` after the parsed input if any.
     *    `null` indicates input was exhausted (which really shouldn't happen but it's up to the caller to decide that.)
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     */
    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
    fun String.parseTptpFofArgumentsOrNull(): Pair<List<Term>, DelimiterAndIndex?>? =
        parseTptpFofArgumentsOrNullHelper()
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
     */
    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
    private fun String.parseTptpFofArgumentsOrNullHelper(
        list: MutableList<Term> = mutableListOf(),
    ): Pair<MutableList<Term>, DelimiterAndIndex?>? =
        parseTptpFirstFofTermOrNull()
            ?.let { (firstTerm: Term, delimiterAndIndex: DelimiterAndIndex?) ->
                list.add(firstTerm)
                delimiterAndIndex
                    ?.let { (delimiter, indexOfDelimiter) ->
                        when (delimiter) {
                            ',' -> {
                                substring(indexOfDelimiter + 1)
                                    .parseTptpFofArgumentsOrNullHelper(list)
                                    ?.let { (_, delimiterAndIndexAfterAllArguments) ->
                                        delimiterAndIndexAfterAllArguments
                                            ?.let { (closeParenAfterAllArguments, indexOfCloseParenAfterAllArguments) ->
                                                list to DelimiterAndIndex(
                                                    closeParenAfterAllArguments,
                                                    indexOfCloseParenAfterAllArguments + (indexOfDelimiter + 1),
                                                )
                                            }
                                            ?: run {
                                                // exhausted input
                                                list to null
                                            }
                                    }
                            }
                            ')' -> {
                                list to delimiterAndIndex
                            }
                            else -> {
                                // not possible
                                throw IllegalStateException()
                            }
                        }
                    }
                    ?: run {
                        // exhausted input on first term
                        list to null
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
     * 2. the [DelimiterAndIndex] within the receiver occurring after the parsed input if any.  Will be either ')' or ','
     *    or `null` which indicates receiver was exhausted.
     *
     * `null` if the receiver doesn't start with a TPTP FOF term possibly followed by a comma, or closed paren
     * and other stuff.
     */
    private fun String.parseTptpFirstFofTermOrNull(): Pair<Term, DelimiterAndIndex?>? =
        firstOfOrNull(delimiters)
            ?.let { (firstDelimiter, indexOfFirstDelimiter) ->
                when (firstDelimiter) {
                    ',', ')' -> {
                        // constant or variable or invalid
                        substring(0, indexOfFirstDelimiter)
                            .let { functor ->
                                (functor.parseTptpVariableOrNull() ?: functor.parseTptpConstantOrNull())
                                    ?.let { it to DelimiterAndIndex(firstDelimiter, indexOfFirstDelimiter) }
                            }
                    }
                    '(' -> {
                        // proper function
                        substring(0, indexOfFirstDelimiter)
                            .parseTptpFunctorOrNull()
                            ?.let { functor ->
                                val startIndexOfArgumentInput = indexOfFirstDelimiter + 1
                                substring(startIndexOfArgumentInput)
                                    .let { argumentsInput ->
                                        argumentsInput
                                            .parseTptpFofArgumentsOrNull()
                                            ?.let { (args: List<Term>, delimiterAndIndex: DelimiterAndIndex?) ->
                                                // we've parsed the internal arguments
                                                delimiterAndIndex
                                                    ?.let { (_, internalIndexOfDelimiter) ->
                                                        // NOTE _ == ')' always when non-`null` delimiterAndIndex is returned from parseTptpFofArgumentsOrNull
                                                        // determine what happens after that close paren
                                                        determineWhatHappensAfterThatCloseParen(
                                                            internalIndexOfDelimiter,
                                                            argumentsInput,
                                                            functor,
                                                            args,
                                                            startIndexOfArgumentInput,
                                                        )
                                                    }
                                                    ?: run {
                                                        // should not exhaust input after parsing internal arguments
                                                        null
                                                    }
                                            }
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

    /**
     * @param indexOfCloseParenWithinArgumentInput The index of the close parenthesis within the [argumentsInput] string.
     * @param argumentsInput The input string representing function arguments.
     * @param functor The name of the function being processed.
     * @param parsedArguments A list of already-parsed arguments of type `Term`.
     * @param externalStartIndexOfArgumentInput The starting index of the `argumentsInput` in the external string.
     * @return A [Pair] of
     * 1. the constructed [ProperFunction]
     * 2. a [DelimiterAndIndex] with either a `','` or a `')'` or `null` indicating the end of input was reached.
     *
     * Returns `null` if the input following the close parenthesis is not valid.
     */
    private fun determineWhatHappensAfterThatCloseParen(
        indexOfCloseParenWithinArgumentInput: Int,
        argumentsInput: String,
        functor: String,
        parsedArguments: List<Term>,
        externalStartIndexOfArgumentInput: Int,
    ): Pair<Term, DelimiterAndIndex?>? {
        val internalIndexAfterClosedParen = indexOfCloseParenWithinArgumentInput + 1
        return argumentsInput.substring(internalIndexAfterClosedParen)
            .let { remainingInputAfterClosedParen ->
                remainingInputAfterClosedParen
                    .firstOfOrNull(endingDelimiters)
                    ?.let { (afterClosedParenDelimiter, indexOfEndDelimiterAfterClosedParen) ->
                        if (remainingInputAfterClosedParen
                                .substring(0, indexOfEndDelimiterAfterClosedParen)
                                .isBlank()
                        ) {
                            // after the close paren, we find a comma or another close paren
                            // we're all good, tell the caller that we got a comma or a close paren
                            ProperFunction(
                                FunctionSymbol(functor),
                                parsedArguments.size,
                                parsedArguments,
                            ) to DelimiterAndIndex(
                                afterClosedParenDelimiter,
                                externalStartIndexOfArgumentInput + internalIndexAfterClosedParen + indexOfEndDelimiterAfterClosedParen,
                            )
                        } else {
                            // there's some non-ending-delimiter character between the close paren and the following ending delimiter
                            null
                        }
                    }
                    ?: run {
                        // after the close paren, there are no more commas or close parens
                        if (remainingInputAfterClosedParen.isBlank()) {
                            // after the close paren, we've exhausted the input
                            ProperFunction(
                                FunctionSymbol(functor),
                                parsedArguments.size,
                                parsedArguments,
                            ) to null
                        } else {
                            // after the close paren, there's something other than an expected delimiter
                            null
                        }
                    }
            }
    }

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
