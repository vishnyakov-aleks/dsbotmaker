package studio.alot.dsbotmaker

import java.io.Serializable

abstract class Cookie<T: Serializable> (val klass: Class<T>,
    val key: String) {

    fun mapValue(valueStr: String): T {
        if (klass.isAssignableFrom(valueStr.javaClass)) {
            true
        } else {
            false
        }
    }
}