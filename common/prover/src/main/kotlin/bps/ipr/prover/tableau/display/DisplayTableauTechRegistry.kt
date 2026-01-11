package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.display.dot.DotDisplayTableauListener
import bps.ipr.prover.tableau.display.text.ReadableDisplayTableauListener
import kotlin.reflect.KClass

object DisplayTableauTechRegistry {

    private val registry: MutableMap<String, KClass<out DisplayTableauListener>> = mutableMapOf()

    fun <T : DisplayTableauListener> register(key: String, listenerType: KClass<T>) {
        if (registry.containsKey(key)) {
            throw IllegalArgumentException("Listener for key $key already registered")
        } else {
            registry[key] = listenerType
        }
    }

    fun getClassForKeyOrNull(key: String): KClass<*>? = registry[key]

    fun getKeys(): Set<String> = registry.keys

    fun getRegisteredClasses(): Set<KClass<*>> = registry.values.toSet()

    init {
        register("readable", ReadableDisplayTableauListener::class)
        register("dot", DotDisplayTableauListener::class)
    }

}
