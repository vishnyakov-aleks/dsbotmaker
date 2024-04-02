package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.TgStepHandler
import studio.alot.avitowheelsparser.presentation.telegram.Navigator
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class NavButtonProcessor(
    private val navigator: Navigator,
    private val stepHandler: TgStepHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val userChatId = dependency.userChatId
        val currentStep = dependency.currentStep

        navigator.navButtonStep(upd, userChatId, currentStep)?.let {
            stepHandler.updateStep(userChatId, it)

            var stepToMove = navigator.getStepFromType(it)

            while (stepToMove is TelegramBotStep.RedirectToAnotherStep) {
                stepToMove.getAnotherStep(userChatId)
                    ?.let {
                        stepHandler.updateStep(userChatId, it)
                        stepToMove = navigator.getStepFromType(it)
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