package studio.alot.dsbotmaker

import java.io.Serializable


class DeepStateBotInstance(
    private val stepHandler: StepHandler,
    val sender: TelegramBotSender
) {
    suspend fun forceChangeStep(
        userChatId: Long,
        stepType: String,
        errorMsg: String? = null
    ) {
        stepHandler.updateStep(userChatId, stepType)
        sender.sendStepMessage(userChatId, stepType, errorMsg)
    }

    fun <T: Serializable> saveCookie(userChatId: Long, vararg pair: Pair<CookieKey<T>, T?>) {
        stepHandler.saveCookie(userChatId, *pair)
    }

    fun <T : Serializable> getCookie(userChatId: Long, cookieKey: CookieKey<T>): T? {
        return stepHandler.getCookie(userChatId, cookieKey)
    }

}