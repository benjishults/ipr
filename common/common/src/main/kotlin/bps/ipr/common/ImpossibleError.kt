package bps.ipr.common

/**
 * Throw this when something impossible happens.
 */
class ImpossibleError(message: String? = null, cause: Throwable? = null) : Error(message, cause)
