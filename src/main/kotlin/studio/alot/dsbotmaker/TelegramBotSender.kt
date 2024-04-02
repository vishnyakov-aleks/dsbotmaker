package studio.alot.dsbotmaker

import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.Serializable
import java.lang.Exception

interface TelegramBotSender {
    @Throws(TelegramApiException::class)
    fun <T : Serializable?, Method : BotApiMethod<T>?> execute(method: Method): T

    @Throws(TelegramApiException::class)
    fun execute(method: SendVideo): Message

    @Throws(TelegramApiException::class)
    fun execute(method: SendPhoto): Message

    @Throws(TelegramApiException::class)
    fun execute(method: SendDocument): Message

    @Throws(TelegramApiException::class)
    fun execute(method: SendMediaGroup): List<Message>

    @Throws(TelegramApiException::class)
    fun execute(method: SendVoice): Message

    suspend fun sendHtmlMessage(
        chatId: Long,
        textBody: String,
        replyKeyboard: ReplyKeyboard?,
        notify: Boolean = true
    ): Message? {
        val message = SendMessage(
            chatId.toString(),
            textBody
        ).also {
            it.parseMode = ParseMode.HTML
            it.replyMarkup = replyKeyboard
            it.disableNotification = !notify
        }


        return execute(message).also {
            delay(1000)
        }
    }

    suspend fun editHtmlMessage(chatId: Long, messageId: Int, textBody: String, replyKeyboard: InlineKeyboardMarkup?) {
        val message = EditMessageText(
            chatId.toString(),
            messageId,
            null,
            textBody,
            ParseMode.HTML,
            true,
            replyKeyboard,
            null,
        )

        execute(message)
    }

    suspend fun getChatMemberCount(groupChatId: Long): Int {
        return execute(GetChatMemberCount(groupChatId.toString()))
    }

    suspend fun sendStepMessage(
        userChatId: Long,
        stepType: String,
        errorMsg: String? = null,
    ) {
        sendStepMessage(userChatId, navigator().getStepFromString(stepType), errorMsg)
    }

    suspend fun sendStepMessage(
        userChatId: Long,
        step: TelegramBotStep,
        errorMsg: String? = null,
    ) {
            errorMsg?.let { sendHtmlMessage(userChatId, it, null) }

            val keyboardMarkup = createKeyboardMarkup(userChatId, step)
            sendHtmlMessage(userChatId, step.getBody(userChatId), keyboardMarkup)

    }

    suspend fun editStepMessage(
        chatId: Long,
        messageId: Int,
        step: TelegramBotStep,
        removeInlineKeyboard: Boolean
    ) {
        val keyboardMarkup = if (!removeInlineKeyboard) {
            createKeyboardMarkup(chatId, step) as InlineKeyboardMarkup
        } else null

        editHtmlMessage(chatId, messageId, step.getBody(chatId), keyboardMarkup)
    }


    private fun createKeyboardMarkup(userChatId: Long, step: TelegramBotStep): ReplyKeyboard {
        return when (step) {
            is TelegramBotStep.InlineButtonsSupported -> {
                InlineKeyboardMarkup.builder().clearKeyboard().keyboard(getAllButtons(step, userChatId)).build()
            }

            is TelegramBotStep.ButtonsSupported -> {
                val buttons = getAllButtons(step, userChatId)
                ReplyKeyboardMarkup(
                    buttons,
                    buttons.size < 4,
                    true,
                    false,
                    step.getInputPlaceholder(userChatId),
                    true
                )
            }

            else -> {
                val buttons = getAllButtons(null, userChatId)
                ReplyKeyboardMarkup(buttons, buttons.size < 4, true, false, null, true)
            }
        }
    }

    private fun getAllButtons(
        step: TelegramBotStep.InlineButtonsSupported,
        chatId: Long
    ): List<List<InlineKeyboardButton>> {
        return step.getButtons(chatId) + step.getNextStepButtons(chatId) + navigator().getNavigateButtons(step)

    }

    fun getAllButtons(step: TelegramBotStep.ButtonsSupported?, chatId: Long): List<KeyboardRow> =
        (step?.getButtons(chatId) ?: emptyList()) + navigator().getNavigateButtons(step)

    fun navigator(): Navigator
}