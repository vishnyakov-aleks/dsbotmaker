package studio.alot.dsbotmaker

import org.json.JSONObject
import java.io.Serializable

internal class TgStepHandler(
    private val userRepository: TgUserRepository
) : StepHandler {


    override fun <T: Serializable> saveCookie(userChatId: Long, vararg pair: Pair<CookieKey<T>, T?>) {
        val json = userRepository.getCookies(userChatId) ?: "{}"
        JSONObject(json)
            .also { cookies ->
                pair.forEach { cookies.put(it.first.key, it.second?.toString()) }
            }.let { userRepository.saveCookies(userChatId, it.toString()) }
    }

    override fun <T : Serializable> getCookie(userChatId: Long, cookieKey: CookieKey<T>): T? {
        return userRepository.getCookies(userChatId)?.let { cookieStr ->
            JSONObject(cookieStr).let { jsonObject ->
                if (jsonObject.has(cookieKey.key)) {
                    jsonObject.getString(cookieKey.key).let { cookieKey.mapValue(it) }
                } else {
                    null
                }
            }
        }
    }

    override fun getUserStep(userChatId: Long): String? {
        return userRepository.getCurrentStep(userChatId)
    }

    override fun updateStep(userChatId: Long, stepType: String) {
        userRepository.updateStep(userChatId, stepType)
    }
}