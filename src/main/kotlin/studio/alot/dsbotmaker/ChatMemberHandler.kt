package studio.alot.avitowheelsparser.presentation.telegram

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated

@Component
class ChatMemberHandler {
    fun processPacket(myChatMember: ChatMemberUpdated): ProcessPacketResult {
        return ProcessPacketResult.RESULT_OK
    }


    enum class ProcessPacketResult {
        RESULT_OK,
    }
}