package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

interface Processor {
    fun process(upd: Update, dependency: Result): Result

    sealed class Result {
        object ExitProcessingResult : Result()
        object ContinueEmptyProcessingResult : Result()
        open class UserIdResult(val userChatId: Long) : Result()
        open class CurrentStepResult(userChatId: Long, val currentStep: TelegramBotStep) : UserIdResult(userChatId)
        class SendStepMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep,
            val stepToMove: TelegramBotStep,
            val supportMessageBeforeNext: Boolean
        ) :
            CurrentStepResult(userChatId, currentStep)

        class SendErrorMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep,
            val errorMsg: String,
            val returnToMainStep: Boolean
        ) :
            CurrentStepResult(userChatId, currentStep)

        class EditInlineButtonsMessageResult(
            userChatId: Long,
            currentStep: TelegramBotStep
        ) :
            CurrentStepResult(userChatId, currentStep)
    }
}