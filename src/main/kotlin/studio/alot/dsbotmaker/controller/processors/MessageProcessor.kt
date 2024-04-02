package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class MessageProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult

        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId
        var result = dependency
        try {
            if (currentStep is TelegramBotStep.MessageReceiver) {
                currentStep.onMessageReceived(upd.message)
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

    class MessageReasonException(val reasonMsg: String) : Exception()
}