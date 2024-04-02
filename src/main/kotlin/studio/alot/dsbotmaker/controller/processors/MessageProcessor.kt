package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.exceptions.OnReceiveMessageReasonException
import studio.alot.dsbotmaker.TelegramBotStep

internal class MessageProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult

        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId
        var result = dependency
        try {
            if (currentStep is TelegramBotStep.MessageReceiver && upd.hasMessage()) {
                currentStep.onMessageReceived(upd.message)
            }
        } catch (e: OnReceiveMessageReasonException) {
            result = Processor.Result.SendErrorMessageResult(
                userChatId,
                currentStep,
                e.reasonMsg,
                false
            )
        }

        return result
    }
}