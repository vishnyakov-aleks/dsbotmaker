package studio.alot.avitowheelsparser.presentation.telegram

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import studio.alot.avitowheelsparser.data.Step
import studio.alot.avitowheelsparser.presentation.telegram.controller.processors.*

interface TelegramBotStep {

    fun getType(): Step

    fun getBody(userChatId: Long): String


    interface ButtonsSupported : TelegramBotStep {
        fun getButtons(userChatId: Long): List<KeyboardRow>
        fun getInputPlaceholder(userChatId: Long): String?
    }

    interface MessageReceiver : TelegramBotStep {
        @Throws(MessageProcessor.MessageReasonException::class)
        fun onMessageReceived(message: Message)
        fun getInputPlaceholder(userChatId: Long): String?
    }

    interface RouteSupported : TelegramBotStep {
        @Throws(GetNextStepProcessor.MessageReasonException::class)
        fun getNextStep(userChatId: Long, message: String): Step
    }

    interface NoSupportBackButton : ButtonsSupported
    interface NoSupportInlineBackButton : InlineButtonsSupported

    interface NoSupportNavigateButtons : ButtonsSupported
    interface NoSupportInlineNavigateButtons : InlineButtonsSupported

    interface MoveBackOnNext : TelegramBotStep

    interface SendMessageBeforeNext : TelegramBotStep {
        fun getBeforeNextMessage(userChatId: Long): String
    }

    interface InlineButtonsSupported : TelegramBotStep {
        fun getButtons(userChatId: Long): List<List<InlineKeyboardButton>>
        fun getNextStepButtons(userChatId: Long): List<List<InlineKeyboardButton>>

        @Throws(InlineButtonsProcessor.MessageReasonException::class)
        fun onCallbackDataReceived(callbackQuery: CallbackQuery)

        @Throws(InlineButtonsProcessor.MessageReasonException::class)
        fun onCallbackNextStepReceived(callbackQuery: CallbackQuery): Boolean
    }

    interface ColdActionInlineButtonsSupported : InlineButtonsSupported {
        companion object {
            const val DIVIDER = ":c_a:"
            const val CALLBACK_DATA_DIVIDER = "##"
        }

        fun getColdAction(): ColdAction
        fun getColdActionButtons(userChatId: Long): List<List<HoldActionInlineKeyboardButtonWrapper>>

        override fun getButtons(userChatId: Long): List<List<InlineKeyboardButton>> {
            return getColdActionButtons(userChatId)
                .map { row ->
                    row.map { button ->
                        button.value.callbackData = "${getColdAction().ordinal}$DIVIDER${button.value.callbackData}"
                        button.value
                    }
                }
        }

        class HoldActionInlineKeyboardButtonWrapper(val value: InlineKeyboardButton)

        enum class ColdAction {
            NEED_AUTH_AVITO
        }
    }

    interface ChatMemberUpdatesSupported : TelegramBotStep {
        @Throws(ChatMemberUpdatesProcessor.MessageReasonException::class)
        fun onUpdateReceived(userChatId: Long, chatMemberUpdated: ChatMemberUpdated): Boolean
    }

    interface CanBeSkipped : TelegramBotStep {
        fun skipStep(userChatId: Long): Boolean
    }

    interface CanBeSkippedInBackStep : TelegramBotStep {
        fun skipBackStep(userChatId: Long): Boolean
    }

    interface StayOnCurrentStep : MessageReceiver {
        fun doNextStep(userChatId: Long): Boolean
    }

    interface RedirectToAnotherStep : TelegramBotStep {
        fun getAnotherStep(userChatId: Long): Step?
    }
}