package studio.alot.dsbotmaker

/**
 * Абстрактный интерфейс логгера для фреймворка.
 * Пользователь может переопределить этот интерфейс для интеграции с любой системой логирования.
 * По умолчанию используется простой логгер с println и printStackTrace.
 */
interface BotLogger {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String)
    fun error(msg: String, throwable: Throwable)
    
    companion object {
        /**
         * Стандартная реализация логгера с println и printStackTrace.
         * Пользователь может заменить эту реализацию на свою через BotLogger.setLogger()
         */
        private var instance: BotLogger = DefaultBotLogger()
        
        fun setLogger(logger: BotLogger) {
            instance = logger
        }
        
        fun debug(msg: String) = instance.debug(msg)
        fun info(msg: String) = instance.info(msg)
        fun warn(msg: String) = instance.warn(msg)
        fun error(msg: String) = instance.error(msg)
        fun error(msg: String, throwable: Throwable) = instance.error(msg, throwable)
    }
}

/**
 * Стандартная реализация логгера с println и printStackTrace.
 */
internal class DefaultBotLogger : BotLogger {
    override fun debug(msg: String) {
        println("[DEBUG] $msg")
    }
    
    override fun info(msg: String) {
        println("[INFO] $msg")
    }
    
    override fun warn(msg: String) {
        println("[WARN] $msg")
    }
    
    override fun error(msg: String) {
        println("[ERROR] $msg")
    }
    
    override fun error(msg: String, throwable: Throwable) {
        println("[ERROR] $msg")
        throwable.printStackTrace()
    }
}
