package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
internal class ChatLogProcessor(
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        if (upd.message != null) {
//            chatLogHandler.saveMessage(upd.message.chatId, upd.message.messageId, upd.message.from.id, upd.message.text)
        }
        return Processor.Result.ContinueEmptyProcessingResult
    }
}