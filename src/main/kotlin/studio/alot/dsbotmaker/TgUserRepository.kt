package studio.alot.dsbotmaker


//TODO to builder
interface TgUserRepository  {

    fun save(userEntity: TgUserEntity)

    fun getCookies(userChatId: Long): String?


    fun saveCookies(userChatId: Long, cookiesJson: String)


    fun getCurrentStep(userChatId: Long): String?


    fun updateStep(userChatId: Long, stepType: String)
}