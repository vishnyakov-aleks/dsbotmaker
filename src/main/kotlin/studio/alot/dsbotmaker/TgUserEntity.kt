package studio.alot.dsbotmaker

data class TgUserEntity(
    val userChatId: Long,
    val firstName: String,
    val lastName: String?,
    val username: String?,
    val cookies: String = "{}",
    val currentStep: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TgUserEntity

        return userChatId == other.userChatId
    }

    override fun hashCode(): Int {
        return userChatId.hashCode()
    }
}
