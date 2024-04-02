package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class InlineButtonsProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId
        var result = dependency

        try {
            if (currentStep is TelegramBotStep.InlineButtonsSupported) {
                if (!upd.hasCallbackQuery()) {
                    result = Processor.Result.SendErrorMessageResult(
                        userChatId,
                        currentStep,
                        "Нажмите на кнопку под сообщением",
                        false
                    )

                } else if (!currentStep.onCallbackNextStepReceived(upd.callbackQuery)) {
                    currentStep.onCallbackDataReceived(upd.callbackQuery)
                    result = Processor.Result.EditInlineButtonsMessageResult(userChatId, currentStep)
                }

            } else if (!upd.hasMessage() && !upd.hasMyChatMember()) {
                throw RuntimeException()
            }
        } catch (e: MessageReasonException) {
            result = Processor.Result.SendErrorMessageResult(userChatId, currentStep, e.reason, e.returnToMainStep)
        }

        return result
    }

    class MessageReasonException(val returnToMainStep: Boolean, val reason: String) : Exception()
}