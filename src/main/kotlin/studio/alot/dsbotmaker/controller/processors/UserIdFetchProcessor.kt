package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update

class UserIdFetchProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        val userId: Long
        val userChatId: Long

        if (upd.hasMyChatMember()) {
            userId = upd.myChatMember.from.id
            userChatId = userId
        } else if (upd.hasMessage()) {
            userChatId = upd.message.chatId
            userId = upd.message.from.id
        } else if (upd.hasCallbackQuery()) {
            userChatId = upd.callbackQuery.message.chatId
            userId = upd.callbackQuery.from.id
        } else {
            throw java.lang.RuntimeException("Not released")
        }

        return if (userId != userChatId) {
            // message from group or channel
            Processor.Result.ExitProcessingResult
        } else {
            Processor.Result.UserIdResult(userChatId)
        }
    }
}