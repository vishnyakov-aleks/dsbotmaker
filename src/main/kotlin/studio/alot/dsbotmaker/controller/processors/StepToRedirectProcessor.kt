package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.Navigator
import studio.alot.dsbotmaker.TelegramBotStep
import studio.alot.dsbotmaker.TgStepHandler

internal class StepToRedirectProcessor(
    private val navigator: Navigator,
    private val stepHandler: TgStepHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        if (dependency.currentStep !is TelegramBotStep.RedirectToAnotherStep) {
            return dependency
        }

        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId

        var stepToMove: TelegramBotStep = currentStep

        while (stepToMove is TelegramBotStep.RedirectToAnotherStep) {
            stepToMove.getAnotherStep(userChatId)
                ?.let {
                    stepHandler.updateStep(userChatId, it)
                    stepToMove = navigator.getStepFromString(it)
                }
                ?: break
        }

        return if (stepToMove == currentStep) {
            dependency
        } else {
            Processor.Result.SendStepMessageResult(
                userChatId,
                currentStep,
                stepToMove,
                supportMessageBeforeNext = false
            )
        }
    }
}