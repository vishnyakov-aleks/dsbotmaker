package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.Navigator
import studio.alot.dsbotmaker.TelegramBotStep
import studio.alot.dsbotmaker.TgStepHandler

internal class NavButtonProcessor(
    private val navigator: Navigator,
    private val stepHandler: TgStepHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val userChatId = dependency.userChatId
        val currentStep = dependency.currentStep

        navigator.navButtonStep(upd, userChatId, currentStep)?.let {
            stepHandler.updateStep(userChatId, it)

            var stepToMove = navigator.getStepFromString(it)

            while (stepToMove is TelegramBotStep.RedirectToAnotherStep) {
                stepToMove.getAnotherStep(userChatId)
                    ?.let {
                        stepHandler.updateStep(userChatId, it)
                        stepToMove = navigator.getStepFromString(it)
                    }
                    ?: break
            }

            return Processor.Result.SendStepMessageResult(
                userChatId,
                currentStep,
                stepToMove,
                supportMessageBeforeNext = false
            )
        }

        return dependency
    }
}