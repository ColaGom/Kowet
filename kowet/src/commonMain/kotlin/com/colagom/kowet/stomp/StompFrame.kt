package com.colagom.kowet.stomp


sealed interface StompEvent {
    data class Error(val message: String) : StompEvent
}

enum class StompCommand {
    SEND,
    SUBSCRIBE,
    UNSUBSCRIBE,
    BEGIN,
    COMMIT,
    ABORT,
    ACK,
    NACK,
    CONNECT,
    DISCONNECT,

    //server commands
    CONNECTED,
    MESSAGE,
    RECEIPT,
    ERROR;

    companion object {
        fun parse(raw: String): StompCommand? = values().find { it.name == raw.trim() }
    }
}

data class StompFrame(
    val command: StompCommand,
    private val headers: Map<String, String> = emptyMap(),
    private val body: String? = null
) : StompEvent {
    constructor(
        command: StompCommand,
        vararg pairs: Pair<String, String>
    ) : this(
        command,
        pairs.toMap(),
        null
    )

    /**
     * TODO
     * Escaping is needed to allow header keys and values to contain those frame header delimiting octets as values.
     * The CONNECT and CONNECTED frames do not escape the carriage return, line feed or colon octets in order to remain backward compatible with STOMP 1.0.
     */
    fun encode() = buildString {
        appendLine(command.name)
        headers.forEach {
            appendLine("${it.key}:${it.value}")
        }
        append('\n')
        body?.let(::append)
        append(NULL)
    }

    companion object {
        private const val NULL = "\u0000"

        fun parse(message: String): StompFrame {
            val lines = message.lines()
            val command =
                StompCommand.parse(lines[0])
                    ?: throw RuntimeException("Required stomp command in message, but : ${lines[0]}")
            val (headers, lastHeaderIndex) = extractHeader(lines)
            val body =
                if (lastHeaderIndex < lines.lastIndex) lines.last()
                else ""

            return StompFrame(
                command,
                headers,
                body
            )
        }

        private fun extractHeader(lines: List<String>): Pair<Map<String, String>, Int> {
            val headers = mutableMapOf<String, String>()
            for (i in 1 until lines.size) {
                val line = lines[i]

                if (line.isEmpty()) return headers to i
                val (key, value) = line.split(':')
                headers[key] = value
            }
            throw IllegalArgumentException("invalid STOMP header : ${lines.joinToString("\n")}")
        }
    }
}

object StompHeaders {
    const val ACCEPT_VERSION = "accept-version"
    const val ACK = "ack"
    const val CONTENT_LENGTH = "content-length"
    const val CONTENT_TYPE = "content-type"
    const val DESTINATION = "destination"
    const val HEART_BEAT = "heart-beat"
    const val HOST = "host"
    const val ID = "id"
    const val LOGIN = "login"
    const val MESSAGE = "message"
    const val MESSAGE_ID = "message-id"
    const val PASSCODE = "passcode"
    const val SUBSCRIPTION = "subscription"
    const val TRANSACTION = "transaction"
    const val VERSION = "version"
}

class FrameFactory(
    private val config: StompConfig
) {
    fun send(
        destination: String,
        message: String
    ): StompFrame {
        val headers = mapOf(
            StompHeaders.DESTINATION to destination,
            StompHeaders.CONTENT_TYPE to "text/plain"
        )
        return StompFrame(StompCommand.SEND, headers, message)
    }

    fun connect(): StompFrame {
        val headers = mapOf(
            StompHeaders.ACCEPT_VERSION to config.version,
            StompHeaders.HOST to config.host,
        )
        return StompFrame(StompCommand.CONNECT, headers)
    }

    fun subscribe(
        id: String,
        destination: String,
        ack: AckMode = AckMode.AUTO
    ): StompFrame {
        val headers = mapOf(
            StompHeaders.ID to id,
            StompHeaders.DESTINATION to destination,
            StompHeaders.ACK to ack.value
        )
        return StompFrame(StompCommand.SUBSCRIBE, headers)
    }

    fun unsubscribe(
        id: String
    ) = StompFrame(StompCommand.UNSUBSCRIBE, StompHeaders.ID to id)

    fun ack(id: String) = StompFrame(
        StompCommand.ACK, StompHeaders.ID to id
    )

    fun nack(id: String) = StompFrame(
        StompCommand.NACK, StompHeaders.ID to id
    )
}