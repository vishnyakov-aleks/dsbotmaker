package studio.alot.avitowheelsparser.domain

import org.json.JSONObject
import org.springframework.stereotype.Component
import studio.alot.avitowheelsparser.data.Step
import studio.alot.avitowheelsparser.data.TgUserRepository

@Component
class TgStepHandler(
    private val userRepository: TgUserRepository
) {

    fun getStepRole(userChatId: Long): StepRole {
        return getCookie(userChatId, Cookie.INTERNAL_USER_ID)?.let { StepRole.USER } ?: StepRole.NOBODY
    }

    fun saveCookie(userChatId: Long, vararg pair: Pair<Cookie, String?>) {
        val json = userRepository.getCookies(userChatId) ?: "{}"
        JSONObject(json)
            .also { cookies ->
                pair.forEach { cookies.put(it.first.name, it.second) }
            }.let { userRepository.saveCookies(userChatId, it.toString()) }
    }

    fun getCookie(userChatId: Long, cookie: Cookie): String? {
        return userRepository.getCookies(userChatId)?.let {
            JSONObject(it).let {
                if (it.has(cookie.name)) {
                    it.getString(cookie.name)
                } else {
                    null
                }
            }
        }
    }

    fun getUserStep(userChatId: Long): Step? {
        return userRepository.getCurrentStep(userChatId)
    }

    fun updateStep(userChatId: Long, step: Step) {
        userRepository.updateStep(userChatId, step)
    }


    enum class StepRole {
        NOBODY,
        USER,
    }

    enum class Cookie {
        INTERNAL_USER_ID,
        CFG_ID,
        IS_AVITO_MANAGER,
    }
}