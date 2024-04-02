package studio.alot.avitowheelsparser.domain

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import studio.alot.avitowheelsparser.data.TgUserEntity
import studio.alot.avitowheelsparser.data.TgUserRepository

@Component
class StartMessageHandler(
    private val userRepository: TgUserRepository
)  {

    fun processStartCommand(message: Message) {
        val user = message.from
        userRepository.save(
            TgUserEntity(
                user.id,
                user.firstName,
                user.lastName,
                user.userName,
                null
            )
        )
    }
}