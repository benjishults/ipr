package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.display.dot.DotDisplayNodeListener
import bps.ipr.prover.tableau.display.text.ReadableDisplayNodeListener
import kotlin.reflect.KClass

object DisplayNodeTechRegistry {

    private val registry: MutableMap<String, KClass<out DisplayNodeListener>> = mutableMapOf()

    fun <T : DisplayNodeListener> register(key: String, listenerType: KClass<T>) {
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
        register("readable", ReadableDisplayNodeListener::class)
        register("dot", DotDisplayNodeListener::class)
    }

}
