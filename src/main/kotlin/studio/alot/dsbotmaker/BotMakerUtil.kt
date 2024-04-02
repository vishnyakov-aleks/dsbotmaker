package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

object BotMakerUtil {
    fun newInlineBtn(text: String, callbackData: String) =

        InlineKeyboardButton.builder().text(text).callbackData(callbackData).build()
}