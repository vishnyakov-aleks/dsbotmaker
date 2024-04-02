package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class Navigator(
    private val steps: List<TelegramBotStep>,
    deepStateBotConfig: DeepStateBotConfig,
) {

    internal val mainStepType = deepStateBotConfig.mainStepType

    fun getStepFromString(type: String): TelegramBotStep {
        return steps.first { it.getType() == type }
    }

    fun getNavigateButtons(step: TelegramBotStep.ButtonsSupported?): List<KeyboardRow> {
        if (step == null || step.getType() == mainStepType || step is TelegramBotStep.NoSupportNavigateButtons) {
            return emptyList()
        }

        return listOf(KeyboardRow().apply {
            val stepType = (step as? TelegramBotStep)?.getType()
            if (stepType != mainStepType) {
                if (step !is TelegramBotStep.NoSupportBackButton) {
                    add(NAV_BACK_BUTTON)
                }

                add(NAV_HOME_BUTTON)
            }

        })
    }


    fun getNavigateButtons(step: TelegramBotStep.InlineButtonsSupported?): List<List<InlineKeyboardButton>> {
        val stepType = (step as? TelegramBotStep)?.getType()
        return if (step == null || stepType == mainStepType || step is TelegramBotStep.NoSupportInlineNavigateButtons) {
            return emptyList()
        } else if (stepType != mainStepType) {
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


    fun navButtonStep(upd: Update, userChatId: Long, currentStep: TelegramBotStep?): String? {
        var navTo: String? = null

        if (upd.hasCallbackQuery()) {
            val callbackData = upd.callbackQuery.data

            if (callbackData == NAV_HOME_BUTTON_CALLBACK) {
                navTo = mainStepType
            } else if (currentStep != null && callbackData == NAV_BACK_BUTTON_CALLBACK) {
                navTo = previousStep(userChatId, currentStep)
            }

        } else if (upd.hasMessage()) {
            val message = upd.message.text
            if (message == NAV_HOME_BUTTON) {
                navTo = mainStepType
            } else if (currentStep != null && message == NAV_BACK_BUTTON) {
                navTo = previousStep(userChatId, currentStep)
            }

        } else return null

        return navTo
    }

    fun previousStep(userChatId: Long, currentStep: TelegramBotStep): String? {
        val currentStepType = currentStep.getType()
        var variants = steps.filter { it.nextStepVariantTypes.contains(currentStepType) }.toSet()
        var firstCase = true
        var skipNextStep: Boolean
        do {
            skipNextStep = false
            if (!firstCase) {
                variants = variants.map { variant ->
                    steps.filter { it.nextStepVariantTypes.contains(variant.getType()) }
                }
                    .flatten()
                    .toSet()
            } else {
                firstCase = false
            }

            if (variants.any {it.getType() == mainStepType}) {
                variants = setOf(steps.first { it.getType() == mainStepType })
            } else if (variants.size == 1) {
                val processor = variants.first()
                if (processor is TelegramBotStep.CanBeSkippedInBackStep && processor.skipBackStep(userChatId)) {
                    skipNextStep = true
                }
            }

        } while (variants.size > 1 || skipNextStep)

        return variants.firstOrNull()?.getType()
    }

    fun getStepFromColdAction(coldAction: TelegramBotStep.ColdActionInlineButtonsSupported.ColdAction): TelegramBotStep {
        return steps.filterIsInstance<TelegramBotStep.ColdActionInlineButtonsSupported>()
            .first { it.getColdAction() == coldAction }
    }

    fun getCandidatesForNextStep(userChatId: Long, currentStep: TelegramBotStep?): Set<String>? {
        val variants = currentStep?.nextStepVariantTypes

        return if (!variants.isNullOrEmpty()) {
            variants
        } else if (currentStep is TelegramBotStep.MoveBackOnNext) {
            setOf(previousStep(userChatId, currentStep) ?: mainStepType)
        } else {
            null
        }
    }


    companion object {
        const val NAV_HOME_BUTTON_CALLBACK = "nav_callback:home"
        const val NAV_BACK_BUTTON_CALLBACK = "nav_callback:back"
        const val NAV_HOME_BUTTON = "На главную"
        const val NAV_BACK_BUTTON = "Вернуться назад"
    }
}