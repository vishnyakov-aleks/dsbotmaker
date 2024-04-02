package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.TelegramBotStep

internal interface Processor {
    fun process(upd: Update, dependency: Result): Result

    sealed class Result {
        data object ExitProcessingResult : Result()
        data object ContinueEmptyProcessingResult : Result()
        internal open class UserIdResult(val userChatId: Long) : Result()
        internal open class CurrentStepResult(userChatId: Long, val currentStep: TelegramBotStep) : UserIdResult(userChatId)
        internal class SendStepMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep,
            val stepToMove: TelegramBotStep,
            val supportMessageBeforeNext: Boolean
        ) :
            CurrentStepResult(userChatId, currentStep)

        internal class SendErrorMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep,
            val errorMsg: String,
            val returnToMainStep: Boolean
        ) :
            CurrentStepResult(userChatId, currentStep)

        internal class EditInlineButtonsMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep
        ) :
            CurrentStepResult(userChatId, currentStep)
    }
}