package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.TgStepHandler
import studio.alot.avitowheelsparser.presentation.telegram.Navigator
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class StepToRedirectProcessor(
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
                    stepToMove = navigator.getStepFromType(it)
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