import studio.alot.dsbotmaker.BotLogger

fun main(args: Array<String>) {
    BotLogger.info("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    BotLogger.info("Program arguments: ${args.joinToString()}")
}
