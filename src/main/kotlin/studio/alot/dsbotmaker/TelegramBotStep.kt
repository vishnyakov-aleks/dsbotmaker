package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import studio.alot.dsbotmaker.controller.processors.ChatMemberUpdatesProcessor
import studio.alot.dsbotmaker.controller.processors.GetNextStepProcessor
import studio.alot.dsbotmaker.controller.processors.InlineButtonsProcessor
import studio.alot.dsbotmaker.controller.processors.MessageProcessor
import studio.alot.dsbotmaker.exceptions.OnInlineButtonsMessageReasonException
import studio.alot.dsbotmaker.exceptions.OnNextStepMessageReasonException
import studio.alot.dsbotmaker.exceptions.OnReceiveMessageReasonException

interface TelegramBotStep {
    val nextStepVariantTypes: Set<String>
    fun getType(): String

    fun getBody(userChatId: Long): String


    interface ButtonsSupported : TelegramBotStep {
        fun getButtons(userChatId: Long): List<KeyboardRow>
        fun getInputPlaceholder(userChatId: Long): String?
    }

    interface MessageReceiver : TelegramBotStep {
        @Throws(OnReceiveMessageReasonException::class)
        fun onMessageReceived(message: Message)
        fun getInputPlaceholder(userChatId: Long): String?
    }

    interface RouteSupported : TelegramBotStep {
        @Throws(OnNextStepMessageReasonException::class)
        fun getNextStep(userChatId: Long, message: String): String
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

        @Throws(OnInlineButtonsMessageReasonException::class)
        fun onCallbackDataReceived(callbackQuery: CallbackQuery)

        @Throws(OnInlineButtonsMessageReasonException::class)
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
        fun getAnotherStep(userChatId: Long): String?
    }

    interface StepDataHolder<T : Any> : TelegramBotStep {
        // Хранилище данных - должен быть реализован в шаге
        val dataStorage: MutableMap<Long, T>
        
        // Фабричный метод для создания нового экземпляра данных
        fun createStepData(userChatId: Long): T
        
        // Основной метод получения данных с автоматической инициализацией
        fun getStepData(userChatId: Long): T {
            return dataStorage[userChatId] ?: run {
                val newData = createStepData(userChatId)
                dataStorage[userChatId] = newData
                newData
            }
        }
        
        // Установка данных
        fun setStepData(userChatId: Long, data: T) {
            dataStorage[userChatId] = data
        }
        
        // Default реализация очистки
        fun clearStepData(userChatId: Long) {
            dataStorage.remove(userChatId)
        }
        
        // Вспомогательный метод для обновления данных
        fun updateStepData(userChatId: Long, update: T.() -> Unit) {
            val data = getStepData(userChatId)
            data.update()
            setStepData(userChatId, data)
        }
    }
}
