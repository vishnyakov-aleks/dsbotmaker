package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated

internal class ChatMemberHandler {
    fun processPacket(myChatMember: ChatMemberUpdated): ProcessPacketResult {
        return ProcessPacketResult.RESULT_OK
    }


    internal enum class ProcessPacketResult {
        RESULT_OK,
    }
}