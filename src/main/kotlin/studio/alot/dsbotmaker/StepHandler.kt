package studio.alot.dsbotmaker

import java.io.Serializable


interface StepHandler {

    fun getUserStep(userChatId: Long): String?
    fun updateStep(userChatId: Long, stepType: String)

    fun <T: Serializable> saveCookie(userChatId: Long, vararg pair: Pair<CookieKey<T>, T?>)

    fun <T : Serializable> getCookie(userChatId: Long, cookieKey: CookieKey<T>): T?
}