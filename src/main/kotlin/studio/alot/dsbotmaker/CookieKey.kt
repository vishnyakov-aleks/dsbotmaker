package studio.alot.dsbotmaker

import java.io.Serializable

@Suppress("UNCHECKED_CAST")
abstract class CookieKey<T: Serializable> (
    private val klass: Class<T>,
    val key: String) {

    fun mapValue(valueStr: String): T {
       return if (klass.isAssignableFrom(String::class.java)) {
            valueStr as T
        } else if (klass.isAssignableFrom(Int::class.java)) {
            valueStr.toInt() as T
        } else if (klass.isAssignableFrom(Boolean::class.java)) {
            valueStr.toBoolean() as T
       } else if (klass.isAssignableFrom(Long::class.java)) {
           valueStr.toLong() as T
       } else if (klass.isAssignableFrom(Float::class.java)) {
           valueStr.toLong() as T
       } else TODO()
    }
}