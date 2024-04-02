package studio.alot.avitowheelsparser.presentation.telegram

import org.telegram.telegrambots.meta.api.objects.Message

interface StartChatHandler {
    fun processStartCommand(message: Message)
}