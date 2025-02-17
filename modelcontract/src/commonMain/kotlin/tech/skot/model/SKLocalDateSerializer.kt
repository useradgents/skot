package tech.skot.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tech.skot.core.SKDateFormat

open class SKLocalDateSerializer(name: String, pattern: String) : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

    private val dateFormat = SKDateFormat(pattern)

    fun serialize(obj: LocalDate) = dateFormat.format(obj)

    fun parse(str: String) = dateFormat.parse(str).toLocalDateTime(TimeZone.currentSystemDefault()).date

    override fun serialize(
        encoder: Encoder,
        value: LocalDate,
    ) {
        encoder.encodeString(serialize(value))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val json = decoder.decodeString()
        return parse(json)
    }
}
