package tech.skot.model

import kotlinx.datetime.TimeZone
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tech.skot.core.SKDateFormat
import kotlin.time.Instant

open class SKInstantSerializer(name: String, pattern: String, locale: TimeZone?=null) : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

    private val dateFormat = SKDateFormat(pattern, locale)

    fun serialize(obj: Instant) = dateFormat.format(obj)

    fun parse(str: String) = dateFormat.parse(str)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(serialize(value))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val json = decoder.decodeString()
        return parse(json)
    }
}
