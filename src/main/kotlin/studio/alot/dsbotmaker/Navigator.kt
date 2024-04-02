package studio.alot.avitowheelsparser.presentation.telegram

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import studio.alot.avitowheelsparser.data.Step

abstract class Navigator(
    private val steps: List<TelegramBotStep>
) {

    abstract val mainStep: Step
    protected abstract val stepMap: Map<Step?, Set<Step>>

    fun getStepFromType(type: Step): TelegramBotStep {
        return steps.first { it.getType() == type }
    }

    fun getNavigateButtons(step: TelegramBotStep.ButtonsSupported?): List<KeyboardRow> {
        if (step is TelegramBotStep.NoSupportNavigateButtons) {
            return emptyList()
        }

        return listOf(KeyboardRow().apply {
            val stepType = (step as? TelegramBotStep)?.getType()
            if (stepType != mainStep) {
                if (step !is TelegramBotStep.NoSupportBackButton) {
                    add(NAV_BACK_BUTTON)
                }

                add(NAV_HOME_BUTTON)
            }

        })
    }


    fun getNavigateButtons(step: TelegramBotStep.InlineButtonsSupported?): List<List<InlineKeyboardButton>> {
        val stepType = (step as? TelegramBotStep)?.getType()
        return if (step is TelegramBotStep.NoSupportInlineNavigateButtons) {
            return emptyList()
        } else if (stepType != mainStep) {
            listOf(ArrayList<InlineKeyboardButton>().apply {
                if (step !is TelegramBotStep.NoSupportInlineBackButton) {
                    add(
                        InlineKeyboardButton
                            .builder()
                            .text(NAV_BACK_BUTTON)
                            .callbackData(NAV_BACK_BUTTON_CALLBACK)
                            .build()
                    )
                }

                add(InlineKeyboardButton.builder().text(NAV_HOME_BUTTON).callbackData(NAV_HOME_BUTTON_CALLBACK).build())
            })
        } else {
            emptyList()
        }
    }


    fun navButtonStep(upd: Update, userChatId: Long, currentStep: TelegramBotStep?): Step? {
        var navTo: Step? = null

        if (upd.hasCallbackQuery()) {
            val callbackData = upd.callbackQuery.data

            if (callbackData == NAV_HOME_BUTTON_CALLBACK) {
                navTo = mainStep
            } else if (currentStep != null && callbackData == NAV_BACK_BUTTON_CALLBACK) {
                navTo = previousStep(userChatId, currentStep.getType())
            }

        } else if (upd.hasMessage()) {
            val message = upd.message.text
            if (message == NAV_HOME_BUTTON) {
                navTo = mainStep
            } else if (currentStep != null && message == NAV_BACK_BUTTON) {
                navTo = previousStep(userChatId, currentStep.getType())
            }

        } else return null

        return navTo
    }

    fun previousStep(userChatId: Long, currentStepType: Step): Step? {
        var variants = stepMap.filter { it.value.contains(currentStepType) }.map { it.key }.toSet()
        var firstCase = true
        var skipNextStep: Boolean
        do {
            skipNextStep = false
            if (!firstCase) {
                variants = variants.mapNotNull { variant ->
                    stepMap
                        .filter { it.value.contains(variant) }
                        .map { it.key }
                }
                    .flatten()
                    .toSet()
            } else {
                firstCase = false
            }

            if (variants.contains(mainStep)) {
                variants = setOf(mainStep)
            } else if (variants.size == 1) {
                val processor = getStepFromType(variants.first()!!)
                if (processor is TelegramBotStep.CanBeSkippedInBackStep && processor.skipBackStep(userChatId)) {
                    skipNextStep = true
                }
            }

        } while (variants.size > 1 || skipNextStep)

        return variants.firstOrNull()
    }

    fun getStepFromColdAction(coldAction: TelegramBotStep.ColdActionInlineButtonsSupported.ColdAction): TelegramBotStep {
        return steps.filterIsInstance<TelegramBotStep.ColdActionInlineButtonsSupported>()
            .first { it.getColdAction() == coldAction }
    }

    fun getCandidatesForNextStep(userChatId: Long, currentStep: TelegramBotStep?): Set<Step>? {
        return stepMap[currentStep?.getType()] ?: let {
            if (currentStep is TelegramBotStep.MoveBackOnNext) {
                setOf(previousStep(userChatId, currentStep.getType()) ?: mainStep)
            } else {
                null
            }
        }
    }


    companion object {
        const val NAV_HOME_BUTTON_CALLBACK = "nav_callback:home"
        const val NAV_BACK_BUTTON_CALLBACK = "nav_callback:back"
        const val NAV_HOME_BUTTON = "На главную"
        const val NAV_BACK_BUTTON = "Вернуться назад"
    }
}