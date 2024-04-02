package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.TelegramBotStep

internal class ChatMemberUpdatesProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId
        var result = dependency

        try {
            if (upd.hasMyChatMember() && currentStep is TelegramBotStep.ChatMemberUpdatesSupported) {
                val updateForceNextStep = currentStep.onUpdateReceived(userChatId, upd.myChatMember)

                if (!updateForceNextStep) {
                    result = Processor.Result.ExitProcessingResult
                }

            } else if (upd.hasMyChatMember()) {
                result = Processor.Result.ExitProcessingResult
            } else if (currentStep is TelegramBotStep.ChatMemberUpdatesSupported) {
                result = Processor.Result.SendErrorMessageResult(
                    userChatId,
                    currentStep,
                    "Выполните действие по инструкции",
                    false
                )
            }
        } catch (e: MessageReasonException) {
            result = Processor.Result.SendErrorMessageResult(
                userChatId,
                currentStep,
                e.reasonMsg,
                false
            )
        }

        return result
    }

    internal class MessageReasonException(val reasonMsg: String) : Exception()
}