package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.TelegramBotStep
import studio.alot.dsbotmaker.exceptions.OnInlineButtonsMessageReasonException

internal class InlineButtonsProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId
        var result = dependency

        try {
            when {
                upd.hasMessage() && upd.message.hasText() && upd.message.text.startsWith("/") -> {}

                currentStep is TelegramBotStep.InlineButtonsSupported -> {
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

                }

                !upd.hasMessage() && !upd.hasMyChatMember() -> {
                    throw RuntimeException()
                }
            }
        } catch (e: OnInlineButtonsMessageReasonException) {
            result = Processor.Result.SendErrorMessageResult(userChatId, currentStep, e.reason, e.returnToMainStep)
        }

        return result
    }

}