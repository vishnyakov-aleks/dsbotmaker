package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.StartMessageHandler

class StartChatProcessor(
    private val botStartMessageHandler: StartMessageHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        if (upd.message?.text?.startsWith("/start") == true) {
            botStartMessageHandler.processStartCommand(upd.message)
        }

        return dependency
    }
}