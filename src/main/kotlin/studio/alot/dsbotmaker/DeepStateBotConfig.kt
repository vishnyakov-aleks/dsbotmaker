package studio.alot.dsbotmaker

interface DeepStateBotConfig {
    fun doOnDeeplink(tgUserId: Long, message: String)

    val mainStepType: String
    val botUsername: String
    val botToken: String

}