package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Message

internal class StartMessageHandler(
    private val userRepository: TgUserRepository,
    private val deepStateBotConfig: DeepStateBotConfig
) {

    fun processStartCommand(mainStep: String, message: Message): TelegramBotStep? {
        val user = message.from
        val referId = (userRepository.getReferId(user.id)
            ?: message.text.substringAfter("/start ref").toLongOrNull()
                ?.let { if (userRepository.getCurrentStep(it) != null) it else null }
                )?.let { if (it == user.id) null else it }

        userRepository.save(
            TgUserEntity(
                user.id,
                user.firstName,
                user.lastName,
                user.userName,
                currentStep = mainStep,
                referId = referId
            )
        )

        return if (message.text.substringAfter("/start").isNotEmpty()) {
            try {
                val newStep = deepStateBotConfig.rewriteStepOnDeeplink(message.from.id, message.text)
                if (newStep != null) {
                    userRepository.updateStep(user.id, newStep.getType())
                }

                newStep
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }

    }
}