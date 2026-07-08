package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.message.Message


interface StartChatHandler {
    fun processStartCommand(message: Message)
}