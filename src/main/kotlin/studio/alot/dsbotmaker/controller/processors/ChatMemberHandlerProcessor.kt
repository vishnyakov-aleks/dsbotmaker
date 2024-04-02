package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.ChatMemberHandler

internal class ChatMemberHandlerProcessor(private val chatMemberHandler: ChatMemberHandler?) : Processor {

    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {

        return if (chatMemberHandler == null && upd.hasMyChatMember()) {
            Processor.Result.ExitProcessingResult
        } else if (upd.hasMyChatMember() &&
            chatMemberHandler != null &&
            chatMemberHandler.processPacket(upd.myChatMember) != ChatMemberHandler.ProcessPacketResult.RESULT_OK
        ) {
            Processor.Result.ExitProcessingResult
        } else {
            Processor.Result.ContinueEmptyProcessingResult
        }

    }
}