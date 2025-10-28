package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Update

interface DeepStateBotConfig {
    fun rewriteStepOnDeeplink(tgUserId: Long, message: String): TelegramBotStep?

    val mainStepType: String
    val botUsername: String
    val botToken: String

    /**
     * Опциональный глобальный обработчик ошибок.
     * Если предоставлен, будет вызываться при любой непойманной ошибке в фреймворке.
     * Пользователь может делать с ошибкой что угодно: логировать, отправлять уведомления, изменять поведение бота.
     */
    val globalErrorHandler: ((Throwable, Update, TelegramBotSender) -> Unit)?
        get() = null

    /**
     * Опциональный кастомный текст ошибки для пользователя.
     * Если предоставлен, будет использоваться вместо стандартного текста ошибки.
     */
    val customErrorMessage: String?
        get() = null
}
