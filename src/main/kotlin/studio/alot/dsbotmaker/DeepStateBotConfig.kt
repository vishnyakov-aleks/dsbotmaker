package studio.alot.dsbotmaker

interface DeepStateBotConfig {
    fun rewriteStepOnDeeplink(tgUserId: Long, message: String): TelegramBotStep

    val mainStepType: String
    val botUsername: String
    val botToken: String

}