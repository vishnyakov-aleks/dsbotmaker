package studio.alot.avitowheelsparser.presentation.telegram

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Message

@Component
class MediaMessageHandler {

    fun extractFileLinks(tgBot: TGBot, message: Message): List<String> {
        val fileIdList: List<String> = when {
            message.hasPhoto() -> listOf(message.photo.maxByOrNull { it.fileSize }!!.fileId)
            message.hasVoice() -> listOf(message.voice.fileId)
            message.hasDocument() -> listOf(message.document.fileId)
            message.hasAnimation() -> listOf(message.animation.fileId)
            message.hasAudio() -> listOf(message.audio.fileId)
            message.hasVideoNote() -> listOf(message.videoNote.fileId)
            message.hasVideo() -> listOf(message.video.fileId)
            message.hasSticker() -> listOf(message.sticker.fileId)
            message.hasText() -> emptyList()
            else -> throw RuntimeException(message.toString())
        }

        return fileIdList.map { getFileUrl(it, tgBot) }
    }

    private fun getFileUrl(fileId: String, tgBot: TGBot): String {
        return tgBot.execute(GetFile(fileId)).getFileUrl(tgBot.botToken)
    }
}