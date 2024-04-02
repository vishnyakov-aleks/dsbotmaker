package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Message

internal class StartMessageHandler(
    private val userRepository: TgUserRepository
) {

    fun processStartCommand(mainStep: String, message: Message) {
        val user = message.from
        userRepository.save(
            TgUserEntity(
                user.id,
                user.firstName,
                user.lastName,
                user.userName,
                currentStep = mainStep
            )
        )
    }
}