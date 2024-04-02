package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Message

interface StartChatHandler {
    fun processStartCommand(message: Message)
}