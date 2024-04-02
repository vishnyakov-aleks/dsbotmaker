package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.TelegramBotStep


internal class StayOnThisStepProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId

        val stayOnThisStep = currentStep is TelegramBotStep.StayOnCurrentStep && !currentStep.doNextStep(userChatId)

        return if (stayOnThisStep) {
            Processor.Result.ExitProcessingResult
        } else {
            dependency
        }
    }
}